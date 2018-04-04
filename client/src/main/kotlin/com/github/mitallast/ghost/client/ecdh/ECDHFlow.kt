package com.github.mitallast.ghost.client.ecdh

import com.github.mitallast.ghost.client.common.await
import com.github.mitallast.ghost.client.common.toArrayBuffer
import com.github.mitallast.ghost.client.common.toByteArray
import com.github.mitallast.ghost.client.crypto.*
import com.github.mitallast.ghost.ecdh.*
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Uint8Array

class ECDHFlow {
    companion object {
        fun start(): ECDHFlow {
            return ECDHFlow()
        }

        suspend fun reconnect(auth: ECDHAuth): ECDHReconnect {
            val exported = ECDSA.exportPublicKey(auth.publicKey).await()
            val buffer = toArrayBuffer(auth.auth, exported)
            val sign = ECDSA.sign(HashSHA512, auth.privateKey, buffer).await()
            return ECDHReconnect(auth.auth, toByteArray(sign))
        }
    }

    private var ecdsa: ECDSAKeyPair? = null
    private var ecdh: ECDHKeyPair? = null

    suspend fun request(): ECDHRequest {
        ecdsa = ECDSA.generateKey(CurveP521).await()
        ecdh = ECDH.generateKey(CurveP521).await()
        val ecdhPublicKey = ECDH.exportPublicKey(ecdh!!.publicKey).await()
        val ecdsaPublicKey = ECDSA.exportPublicKey(ecdsa!!.publicKey).await()

        val b = ArrayBuffer(ecdhPublicKey.byteLength + ecdsaPublicKey.byteLength)
        Uint8Array(b).set(Uint8Array(ecdhPublicKey), 0)
        Uint8Array(b).set(Uint8Array(ecdsaPublicKey), ecdhPublicKey.byteLength)
        val sign = ECDSA.sign(HashSHA512, ecdsa!!.privateKey, b).await()

        return ECDHRequest(toByteArray(ecdhPublicKey), toByteArray(ecdsaPublicKey), toByteArray(sign))
    }

    suspend fun response(response: ECDHResponse): ECDHAuth {
        requireNotNull(ecdh)
        requireNotNull(ecdsa)
        val serverKeyBuffer = toArrayBuffer(response.ecdhPublicKey)
        val sign = toArrayBuffer(response.sign)
        val buffer = toArrayBuffer(response.auth, response.ecdhPublicKey)
        val verified = ECDSA.verify(HashSHA512, ServerKeys.publicKey(), sign, buffer).await()
        if (verified) {
            val serverKey = ECDH.importPublicKey(CurveP521, serverKeyBuffer).await()
            // 528 = 66 bytes
            val secret = ECDH.deriveBits(CurveP521, serverKey, ecdh!!.privateKey, 528).await()
            // use SHA-256 as KDF function
            val hash = SHA256.digest(secret).await()
            val secretKey = AES.importKey(hash).await()
            val auth = ECDHAuth(response.auth, secretKey, ecdsa!!.publicKey, ecdsa!!.privateKey)
            ECDHAuthStore.storeAuth(auth)
            return auth
        } else {
            throw Exception("not verified")
        }
    }
}

object ECDHCipher {
    suspend fun encrypt(auth: ECDHAuth, data: ArrayBuffer): ECDHEncrypted {
        val (encrypted, iv) = AES.encrypt(auth.secretKey, data).await()
        val buffer = toArrayBuffer(auth.auth, iv.buffer, data)
        val sign = ECDSA.sign(HashSHA512, auth.privateKey, buffer).await()
        return ECDHEncrypted(
            auth.auth,
            toByteArray(sign),
            toByteArray(iv.buffer),
            toByteArray(encrypted)
        )
    }

    suspend fun decrypt(auth: ECDHAuth, encrypted: ECDHEncrypted): ArrayBuffer {
        val serverKey = ServerKeys.publicKey()
        val data = toArrayBuffer(encrypted.encrypted)
        val iv = Uint8Array(toArrayBuffer(encrypted.iv))
        val sign = toArrayBuffer(encrypted.sign)
        val decrypted = AES.decrypt(auth.secretKey, data, iv).await()
        val buffer = toArrayBuffer(encrypted.auth, encrypted.iv, decrypted)
        val verified = ECDSA.verify(HashSHA512, serverKey, sign, buffer).await()
        if (verified) {
            return decrypted
        } else {
            throw IllegalArgumentException("sign not verified")
        }
    }
}