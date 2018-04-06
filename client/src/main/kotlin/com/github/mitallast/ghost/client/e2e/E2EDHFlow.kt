package com.github.mitallast.ghost.client.e2e

import com.github.mitallast.ghost.client.common.await
import com.github.mitallast.ghost.client.common.toArrayBuffer
import com.github.mitallast.ghost.client.common.toByteArray
import com.github.mitallast.ghost.client.crypto.*
import com.github.mitallast.ghost.client.ecdh.ECDHAuthStore
import com.github.mitallast.ghost.client.ecdh.ECDHController
import com.github.mitallast.ghost.client.messages.MessagesController
import com.github.mitallast.ghost.client.profile.ProfileController
import com.github.mitallast.ghost.client.prompt.PromptView
import com.github.mitallast.ghost.common.codec.Codec
import com.github.mitallast.ghost.common.codec.CodecMessage
import com.github.mitallast.ghost.e2e.E2EEncrypted
import com.github.mitallast.ghost.e2e.E2ERequest
import com.github.mitallast.ghost.e2e.E2EResponse
import com.github.mitallast.ghost.message.Message
import com.github.mitallast.ghost.profile.UserProfile
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Uint8Array

object E2EDHFlow {
    suspend fun request(from: ByteArray, to: ByteArray): E2ERequest {
        val ecdh = ECDH.generateKey(CurveP521).await()
        val ecdsa = ECDSA.generateKey(CurveP521).await()

        val request = E2EAuthRequest(
            to,
            ecdh.publicKey, ecdh.privateKey,
            ecdsa.publicKey, ecdsa.privateKey
        )

        E2EAuthStore.storeRequest(request)

        val ecdhPublicKey = ECDH.exportPublicKey(ecdh.publicKey).await()
        val ecdsaPublicKey = ECDSA.exportPublicKey(ecdsa.publicKey).await()

        val buffer = toArrayBuffer(from, to, ecdhPublicKey, ecdsaPublicKey)
        val sign = ECDSA.sign(HashSHA512, ecdsa.privateKey, buffer).await()

        return E2ERequest(
            from,
            to,
            toByteArray(ecdhPublicKey),
            toByteArray(ecdsaPublicKey),
            toByteArray(sign)
        )
    }

    suspend fun response(request: E2ERequest): E2EResponse {
        val auth = ECDHAuthStore.loadAuth()!!
        val fromECDHPublicBuffer = toArrayBuffer(request.ecdhPublicKey)
        val fromECDSAPublicBuffer = toArrayBuffer(request.ecdsaPublicKey)
        val fromECDHPublicKey = ECDH.importPublicKey(CurveP521, fromECDHPublicBuffer).await()
        val fromECDSAPublicKey = ECDSA.importPublicKey(CurveP521, fromECDSAPublicBuffer).await()

        val fromSign = toArrayBuffer(request.sign)
        val buffer = toArrayBuffer(request.from, request.to, fromECDHPublicBuffer, fromECDSAPublicBuffer)

        if (!ECDSA.verify(HashSHA512, fromECDSAPublicKey, fromSign, buffer).await()) {
            console.error("e2e request sign not verified")
            val ex = IllegalArgumentException("e2e request: sign not verified")
            throw ex
        }

        // 528 = 66 bytes
        val ecdh = ECDH.generateKey(CurveP521).await()
        val ecdsa = ECDSA.generateKey(CurveP521).await()
        val ecdhPublicKey = ECDH.exportPublicKey(ecdh.publicKey).await()
        val ecdsaPublicKey = ECDSA.exportPublicKey(ecdsa.publicKey).await()
        val secret = ECDH.deriveBits(CurveP521, fromECDHPublicKey, ecdh.privateKey, 528).await()

        // use PBKDF2 as KDF function
        val pwd = PromptView.prompt("e2e: " + HEX.toHex(request.from)).await()
        val pwdKey = PBKDF2.importKey(pwd).await()
        val secretKey = PBKDF2.deriveKeyAES(Uint8Array(secret), 1024, HashSHA512, pwdKey, AESKeyLen256).await()

        val fromAuth = E2EAuth(
            request.from,
            secretKey,
            fromECDSAPublicKey,
            ecdsa.privateKey
        )

        console.log("store e2e auth", HEX.toHex(fromAuth.auth))

        E2EAuthStore.storeAuth(fromAuth)

        console.log("e2e by request removeRequest", HEX.toHex(fromAuth.auth))

        val buffer2 = toArrayBuffer(auth.auth, request.from, ecdhPublicKey, ecdsaPublicKey)
        val sign = ECDSA.sign(HashSHA512, ecdsa.privateKey, buffer2).await()

        return E2EResponse(
            auth.auth,
            request.from,
            toByteArray(ecdhPublicKey),
            toByteArray(ecdsaPublicKey),
            toByteArray(sign)
        )
    }

    suspend fun keyAgreement(response: E2EResponse): E2EAuth {
        val request = E2EAuthStore.loadRequest(response.from)

        val fromECDHPublicBuffer = toArrayBuffer(response.ecdhPublicKey)
        val fromECDSAPublicBuffer = toArrayBuffer(response.ecdsaPublicKey)
        val fromECDHPublicKey = ECDH.importPublicKey(CurveP521, fromECDHPublicBuffer).await()
        val fromECDSAPublicKey = ECDSA.importPublicKey(CurveP521, fromECDSAPublicBuffer).await()

        val fromSign = toArrayBuffer(response.sign)
        val buffer = toArrayBuffer(response.from, response.to, fromECDHPublicBuffer, fromECDSAPublicBuffer)

        if (!ECDSA.verify(HashSHA512, fromECDSAPublicKey, fromSign, buffer).await()) {
            throw IllegalArgumentException("e2e key aagreement: sign not verified")
        }

        // 528 = 66 bytes
        val secret = ECDH.deriveBits(CurveP521, fromECDHPublicKey, request.ecdhPrivateKey, 528).await()

        // use PBKDF2 as KDF function
        val pwd = PromptView.prompt("e2e: " + HEX.toHex(response.from)).await()
        val pwdKey = PBKDF2.importKey(pwd).await()
        val secretKey = PBKDF2.deriveKeyAES(Uint8Array(secret), 1024, HashSHA512, pwdKey, AESKeyLen256).await()

        val auth = E2EAuth(
            response.from,
            secretKey,
            fromECDSAPublicKey,
            request.ecdsaPrivateKey
        )

        E2EAuthStore.storeAuth(auth)
        E2EAuthStore.removeRequest(response.from)

        return auth
    }

    suspend fun encrypt(from: ByteArray, to: ByteArray, data: ArrayBuffer): E2EEncrypted {
        console.log("load e2e auth", HEX.toHex(to))
        val auth = E2EAuthStore.loadAuth(to)!!
        console.log("e2e aes encrypt")
        val (encrypted, iv) = AES.encrypt(auth.secretKey, data).await()
        val buffer = toArrayBuffer(from, iv.buffer, data)
        console.log("e2e ecdsa sign")
        val sign = ECDSA.sign(HashSHA512, auth.privateKey, buffer).await()
        return E2EEncrypted(
            from,
            to,
            toByteArray(sign),
            toByteArray(iv.buffer),
            toByteArray(encrypted)
        )
    }

    suspend fun decrypt(encrypted: E2EEncrypted): ArrayBuffer {
        val auth = E2EAuthStore.loadAuth(encrypted.from)!!
        val data = toArrayBuffer(encrypted.encrypted)
        val iv = Uint8Array(toArrayBuffer(encrypted.iv))
        val sign = toArrayBuffer(encrypted.sign)
        val decrypted = AES.decrypt(auth.secretKey, data, iv).await()
        val buffer = toArrayBuffer(encrypted.from, encrypted.iv, decrypted)
        val verified = ECDSA.verify(HashSHA512, auth.publicKey, sign, buffer).await()
        if (verified) {
            return decrypted
        } else {
            throw IllegalArgumentException("e2e decrypt: sign not verified")
        }
    }
}

object E2EFlow {
    suspend fun connect(to: ByteArray) {
        console.log("search e2e auth")
        val e2eAuth = E2EAuthStore.loadAuth(to)
        if (e2eAuth == null) {
            console.log("no e2e auth found")
            val auth = ECDHController.auth()
            console.log("prepare e2e request")
            val request = E2EDHFlow.request(auth, to)
            console.log("send e2e request")
            ECDHController.send(request)
        } else {
            console.log("e2e auth loaded")
        }
    }

    suspend fun send(to: ByteArray, message: CodecMessage) {
        console.log("send e2e", HEX.toHex(to), message)
        val encoded = Codec.anyCodec<CodecMessage>().write(message)
        console.log("get auth for e2e encryption")
        val auth = ECDHController.auth()
        console.log("encrypt e2e")
        val encrypted = E2EDHFlow.encrypt(auth, to, toArrayBuffer(encoded))
        console.log("send e2e encrypted", encrypted)
        ECDHController.send(encrypted)
    }

    suspend fun handle(update: CodecMessage) {
        when (update) {
            is E2ERequest -> {
                console.log("e2e request received")
                val response = E2EDHFlow.response(update)
                ECDHController.send(response)
                ProfileController.newContact(update.from)
            }
            is E2EResponse -> {
                console.log("e2e response received")
                E2EDHFlow.keyAgreement(update)
                ProfileController.newContact(update.from)
            }
            is E2EEncrypted -> {
                console.log("e2e encrypted received")
                val decrypted = E2EDHFlow.decrypt(update)
                val decoded = Codec.anyCodec<CodecMessage>().read(toByteArray(decrypted))
                console.log("e2e received", decoded)
                when (decoded) {
                    is UserProfile -> ProfileController.updateProfile(decoded)
                    is Message -> MessagesController.handle(update.from, decoded)
                }
            }
        }
    }
}