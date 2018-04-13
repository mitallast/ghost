package com.github.mitallast.ghost.client.e2e

import com.github.mitallast.ghost.client.common.await
import com.github.mitallast.ghost.client.common.toArrayBuffer
import com.github.mitallast.ghost.client.common.toByteArray
import com.github.mitallast.ghost.client.crypto.*
import com.github.mitallast.ghost.e2e.E2EAuthRequest
import com.github.mitallast.ghost.e2e.E2EAuthResponse
import com.github.mitallast.ghost.e2e.E2EEncrypted
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Uint8Array

object E2EDHFlow {
    suspend fun cancelRequest(to: ByteArray) {
        E2EIncomingRequestStore.remove(to)
        E2EOutgoingRequestStore.remove(to)
        E2EResponseStore.remove(to)
    }

    suspend fun request(to: ByteArray): E2EAuthRequest {
        val ecdh = ECDH.generateKey(CurveP384).await()
        val ecdsa = ECDSA.generateKey(CurveP384).await()

        val request = E2EOutgoingRequest(
            to,
            ecdh.publicKey, ecdh.privateKey,
            ecdsa.publicKey, ecdsa.privateKey
        )

        E2EOutgoingRequestStore.store(request)

        val ecdhPublicKey = ECDH.exportPublicKey(ecdh.publicKey).await()
        val ecdsaPublicKey = ECDSA.exportPublicKey(ecdsa.publicKey).await()

        val buffer = toArrayBuffer(ecdhPublicKey, ecdsaPublicKey)
        val sign = ECDSA.sign(HashSHA512, ecdsa.privateKey, buffer).await()

        return E2EAuthRequest(
            toByteArray(ecdhPublicKey),
            toByteArray(ecdsaPublicKey),
            toByteArray(sign)
        )
    }

    suspend fun validateRequest(from: ByteArray, request: E2EAuthRequest): Boolean {
        val fromECDHPublicBuffer = toArrayBuffer(request.ecdhPublicKey)
        val fromECDSAPublicBuffer = toArrayBuffer(request.ecdsaPublicKey)
        val fromECDHPublicKey = ECDH.importPublicKey(CurveP384, fromECDHPublicBuffer).await()
        val fromECDSAPublicKey = ECDSA.importPublicKey(CurveP384, fromECDSAPublicBuffer).await()

        val fromSign = toArrayBuffer(request.sign)
        val buffer = toArrayBuffer(fromECDHPublicBuffer, fromECDSAPublicBuffer)

        if (!ECDSA.verify(HashSHA512, fromECDSAPublicKey, fromSign, buffer).await()) {
            console.error("e2e request sign not verified")
            return false
        }
        val incoming = E2EIncomingRequest(
            from,
            fromECDHPublicKey,
            fromECDSAPublicKey
        )
        E2EIncomingRequestStore.store(incoming)
        return true
    }

    suspend fun answerRequest(address: ByteArray, password: String, data: ArrayBuffer): E2EAuthResponse {
        val request = E2EIncomingRequestStore.load(address)!!
        val ecdh = ECDH.generateKey(CurveP384).await()
        val ecdsa = ECDSA.generateKey(CurveP384).await()
        val ecdhPublicKey = ECDH.exportPublicKey(ecdh.publicKey).await()
        val ecdsaPublicKey = ECDSA.exportPublicKey(ecdsa.publicKey).await()

        val secret = ECDH.deriveBits(CurveP384, request.ecdhPublicKey, ecdh.privateKey, 384).await()

        val pwdKey = PBKDF2.importKey(password).await()
        val secretKey = PBKDF2.deriveKeyAES(Uint8Array(secret), 1024, HashSHA512, pwdKey, AESKeyLen256).await()

        val fromAuth = E2EAuth(
            address,
            secretKey,
            request.ecdsaPublicKey,
            ecdsa.privateKey
        )

        E2EAuthStore.store(fromAuth)
        E2EIncomingRequestStore.remove(address)

        val (encrypted, iv) = AES.encrypt(secretKey, data).await()

        val buffer = toArrayBuffer(ecdhPublicKey, ecdsaPublicKey, iv, data)
        val sign = ECDSA.sign(HashSHA512, ecdsa.privateKey, buffer).await()

        return E2EAuthResponse(
            toByteArray(ecdhPublicKey),
            toByteArray(ecdsaPublicKey),
            toByteArray(sign),
            toByteArray(iv),
            toByteArray(encrypted)
        )
    }

    suspend fun storeResponse(from: ByteArray, response: E2EAuthResponse) {
        val fromECDHPublicBuffer = toArrayBuffer(response.ecdhPublicKey)
        val fromECDSAPublicBuffer = toArrayBuffer(response.ecdsaPublicKey)
        val fromECDHPublicKey = ECDH.importPublicKey(CurveP384, fromECDHPublicBuffer).await()
        val fromECDSAPublicKey = ECDSA.importPublicKey(CurveP384, fromECDSAPublicBuffer).await()

        val r = E2EResponse(
            from,
            fromECDHPublicKey,
            fromECDSAPublicKey,
            response.sign,
            response.iv,
            response.encrypted
        )
        E2EResponseStore.store(r)
    }

    suspend fun completeResponse(address: ByteArray, password: String): ArrayBuffer {
        val request = E2EOutgoingRequestStore.load(address)!!
        val response = E2EResponseStore.load(address)!!

        val secret = ECDH.deriveBits(CurveP384, response.ecdhPublicKey, request.ecdhPrivateKey, 384).await()

        // use PBKDF2 as KDF function
        val pwdKey = PBKDF2.importKey(password).await()
        val secretKey = PBKDF2.deriveKeyAES(Uint8Array(secret), 1024, HashSHA512, pwdKey, AESKeyLen256).await()

        val iv = Uint8Array(toArrayBuffer(response.iv))
        val encrypted = toArrayBuffer(response.encrypted)
        val decrypted = AES.decrypt(secretKey, encrypted, iv).await()

        val ecdh = ECDH.exportPublicKey(response.ecdhPublicKey).await()
        val ecdsa = ECDSA.exportPublicKey(response.ecdsaPublicKey).await()
        val buffer = toArrayBuffer(ecdh, ecdsa, response.iv, decrypted)
        val sign = toArrayBuffer(response.sign)
        val verified = ECDSA.verify(HashSHA512, response.ecdsaPublicKey, sign, buffer).await()
        if(!verified) {
            throw IllegalArgumentException("e2e decrypt: sign not verified")
        }

        val auth = E2EAuth(
            address,
            secretKey,
            response.ecdsaPublicKey,
            request.ecdsaPrivateKey
        )

        E2EAuthStore.store(auth)
        E2EOutgoingRequestStore.remove(address)
        E2EResponseStore.remove(address)

        return decrypted
    }

    suspend fun encrypt(to: ByteArray, data: ArrayBuffer): E2EEncrypted {
        console.log("load e2e address", HEX.toHex(to))
        val auth = E2EAuthStore.load(to)!!
        console.log("e2e aes encrypt")
        val (encrypted, iv) = AES.encrypt(auth.secretKey, data).await()
        val buffer = toArrayBuffer(iv.buffer, data)
        console.log("e2e ecdsa sign")
        val sign = ECDSA.sign(HashSHA512, auth.privateKey, buffer).await()
        return E2EEncrypted(
            toByteArray(sign),
            toByteArray(iv.buffer),
            toByteArray(encrypted)
        )
    }

    suspend fun encryptRaw(to: ByteArray, data: ArrayBuffer): Pair<Pair<ByteArray, ByteArray>, ArrayBuffer> {
        console.log("load e2e address", HEX.toHex(to))
        val auth = E2EAuthStore.load(to)!!
        console.log("e2e aes encrypt")
        val (encrypted, iv) = AES.encrypt(auth.secretKey, data).await()
        val buffer = toArrayBuffer(iv.buffer, data)
        console.log("e2e ecdsa sign")
        val sign = ECDSA.sign(HashSHA512, auth.privateKey, buffer).await()
        return Pair(
            Pair(toByteArray(sign), toByteArray(iv.buffer)),
            encrypted
        )
    }

    suspend fun decrypt(from: ByteArray, encrypted: E2EEncrypted): ArrayBuffer {
        val auth = E2EAuthStore.load(from)!!
        val data = toArrayBuffer(encrypted.encrypted)
        val iv = Uint8Array(toArrayBuffer(encrypted.iv))
        val sign = toArrayBuffer(encrypted.sign)
        val decrypted = AES.decrypt(auth.secretKey, data, iv).await()
        val buffer = toArrayBuffer(encrypted.iv, decrypted)
        val verified = ECDSA.verify(HashSHA512, auth.publicKey, sign, buffer).await()
        if (verified) {
            return decrypted
        } else {
            throw IllegalArgumentException("e2e decrypt: sign not verified")
        }
    }

    suspend fun decrypt(from: ByteArray,
                        to: ByteArray,
                        sign: ByteArray,
                        iv: ByteArray,
                        data: ArrayBuffer,
                        own: Boolean): ArrayBuffer {
        val auth = E2EAuthStore.load(from)!!
        val ivB = Uint8Array(toArrayBuffer(iv))
        val signB = toArrayBuffer(sign)
        val decrypted = AES.decrypt(auth.secretKey, data, ivB).await()
        val verified = if (own) {
            val buffer = toArrayBuffer(to, iv, decrypted)
            val publicKey = ECDSA.toPublicKey(CurveP384, auth.privateKey)
            ECDSA.verify(HashSHA512, publicKey, signB, buffer).await()
        } else {
            val buffer = toArrayBuffer(from, iv, decrypted)
            ECDSA.verify(HashSHA512, auth.publicKey, signB, buffer).await()
        }
        if (verified) {
            return decrypted
        } else {
            throw IllegalArgumentException("e2e decrypt: sign not verified")
        }
    }
}