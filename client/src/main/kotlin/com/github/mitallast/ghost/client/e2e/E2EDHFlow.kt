package com.github.mitallast.ghost.client.e2e

import com.github.mitallast.ghost.client.common.await
import com.github.mitallast.ghost.client.common.launch
import com.github.mitallast.ghost.client.common.toArrayBuffer
import com.github.mitallast.ghost.client.common.toByteArray
import com.github.mitallast.ghost.client.crypto.*
import com.github.mitallast.ghost.client.ecdh.*
import com.github.mitallast.ghost.e2ee.E2EEncrypted
import com.github.mitallast.ghost.e2ee.E2ERequest
import com.github.mitallast.ghost.e2ee.E2EResponse
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Uint8Array
import kotlin.js.Promise

private typealias Resolver = (E2EAuth) -> Unit
private typealias Reject = (Throwable) -> Unit

object E2EDHFlow {
    private val promises = HashMap<String, Pair<Resolver, Reject>>()

    suspend fun connect(to: ByteArray): Promise<E2EAuth> {
        return Promise({ resolve, reject ->
            launch {
                val connection = ConnectionService.connection()
                promises[HEX.toHex(to)] = Pair(resolve, reject)
                val request = request(connection.auth(), to)
                ConnectionService.send(request)
            }
        })
    }

    suspend fun request(auth: ECDHAuth, to: ByteArray): E2ERequest {
        val ecdh = ECDH.generateKey(CurveP521).await()
        val ecdsa = ECDSA.generateKey(CurveP521).await()

        E2EAuthStore.storeECDH(to, ecdh.publicKey, ecdh.privateKey)
        E2EAuthStore.storeECDSA(to, ecdsa.publicKey, ecdsa.privateKey)

        val ecdhPublicKey = ECDH.exportPublicKey(ecdh.publicKey).await()
        val ecdsaPublicKey = ECDSA.exportPublicKey(ecdsa.publicKey).await()

        val buffer = toArrayBuffer(auth.auth, to, ecdhPublicKey, ecdsaPublicKey)
        val sign = ECDSA.sign(HashSHA512, ecdsa.privateKey, buffer).await()

        return E2ERequest(
            auth.auth,
            to,
            toByteArray(ecdhPublicKey),
            toByteArray(ecdsaPublicKey),
            toByteArray(sign)
        )
    }

    suspend fun response(auth: ECDHAuth, request: E2ERequest): E2EResponse {
        val fromECDHPublicBuffer = toArrayBuffer(request.ecdhPublicKey)
        val fromECDSAPublicBuffer = toArrayBuffer(request.ecdsaPublicKey)
        val fromECDHPublicKey = ECDH.importPublicKey(CurveP521, fromECDHPublicBuffer).await()
        val fromECDSAPublicKey = ECDSA.importPublicKey(CurveP521, fromECDSAPublicBuffer).await()

        val fromSign = toArrayBuffer(request.sign)
        val buffer = toArrayBuffer(request.from, request.to, fromECDHPublicBuffer, fromECDSAPublicBuffer)

        if (!ECDSA.verify(HashSHA512, fromECDSAPublicKey, fromSign, buffer).await()) {
            console.error("e2e request sign not verified")
            val ex = IllegalArgumentException("e2e request: sign not verified")
            promises[HEX.toHex(auth.auth)]?.second?.invoke(ex)
            throw ex
        }

        // 528 = 66 bytes
        val ecdh = ECDH.generateKey(CurveP521).await()
        val ecdsa = ECDSA.generateKey(CurveP521).await()
        val ecdhPublicKey = ECDH.exportPublicKey(ecdh.publicKey).await()
        val ecdsaPublicKey = ECDSA.exportPublicKey(ecdsa.publicKey).await()
        val secret = ECDH.deriveBits(CurveP521, fromECDHPublicKey, ecdh.privateKey, 528).await()
        // use SHA-256 as KDF function
        val hash = SHA256.digest(secret).await()
        val secretKey = AES.importKey(hash).await()

        val fromAuth = E2EAuth(
            request.from,
            secretKey,
            fromECDSAPublicKey,
            ecdsa.privateKey
        )

        E2EAuthStore.storeAuth(fromAuth)

        console.log("e2e by request complete")

        val promise = promises[HEX.toHex(auth.auth)]
        if(promise != null) {
            promise.first.invoke(fromAuth)
        }
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
        val (_, ecdhPrivateKey) = E2EAuthStore.loadECDH(response.from)
        val (_, ecdsaPrivateKey) = E2EAuthStore.loadECDSA(response.from)

        E2EAuthStore.complete(response.from)

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
        val secret = ECDH.deriveBits(CurveP521, fromECDHPublicKey, ecdhPrivateKey, 528).await()
        // use SHA-256 as KDF function
        val hash = SHA256.digest(secret).await()
        val secretKey = AES.importKey(hash).await()

        val auth = E2EAuth(
            response.from,
            secretKey,
            fromECDSAPublicKey,
            ecdsaPrivateKey
        )

        E2EAuthStore.storeAuth(auth)

        return auth
    }

    suspend fun encrypt(to: ByteArray, data: ArrayBuffer): ECDHEncrypted {
        val auth = E2EAuthStore.loadAuth(to)
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

    suspend fun decrypt(encrypted: E2EEncrypted): ArrayBuffer {
        val auth = E2EAuthStore.loadAuth(encrypted.from)
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