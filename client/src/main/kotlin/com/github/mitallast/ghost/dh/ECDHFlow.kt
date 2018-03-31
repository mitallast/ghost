package com.github.mitallast.ghost.dh

import com.github.mitallast.ghost.client.crypto.*
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Uint8Array
import org.khronos.webgl.get
import org.khronos.webgl.set
import kotlin.coroutines.experimental.*
import kotlin.js.Promise

suspend fun <T> Promise<T>.await(): T = suspendCoroutine { cont ->
    then({ cont.resume(it) }, { cont.resumeWithException(it) })
}

fun launch(block: suspend () -> Unit) {
    block.startCoroutine(object : Continuation<Unit> {
        override val context: CoroutineContext get() = EmptyCoroutineContext
        override fun resume(value: Unit) {}
        override fun resumeWithException(exception: Throwable) {
            println("Coroutine failed: $exception")
        }
    })
}

class ECDHFlow(
    private val ecdsaKeyPair: ECDSAKeyPair,
    private val ecdhKeyPair: ECDHKeyPair,
    private val ecdsaServerPublicKey: ECDSAPublicKey
) {
    companion object {
        suspend fun start(): ECDHFlow {
            val ecdsa = ECDSA.generateKey(CurveP521).await()
            val ecdh = ECDH.generateKey(CurveP521).await()
            val serverPublicKey = HEX.parseHex(ServerKeys.ECDSA.publicKey).buffer
            val serverKey = ECDSA.importPublicKey(CurveP521, serverPublicKey).await()
            return ECDHFlow(ecdsa, ecdh, serverKey)
        }

        private fun toByteArray(array: ArrayBuffer): ByteArray {
            val view = Uint8Array(array)
            return ByteArray(view.length, { view[it] })
        }

        private fun toArrayBuffer(bytes: ByteArray): ArrayBuffer {
            val view = Uint8Array(bytes.size)
            for (i in 0 until bytes.size) {
                view[i] = bytes[i]
            }
            return view.buffer
        }

        private fun toArrayBuffer(vararg bytes: ByteArray): ArrayBuffer {
            val size = bytes.fold(0, { a, b -> a + b.size })
            val view = Uint8Array(size)
            var offset = 0
            for (a in bytes) {
                for (b in a) {
                    view[offset] = b
                    offset++
                }
            }
            return view.buffer
        }
    }

    private var secretKey: AESKey? = null
    private var auth: ByteArray? = null

    suspend fun request(): ECDHRequest {
        val ecdhPublicKey = ECDH.exportPublicKey(ecdhKeyPair.publicKey).await()
        val ecdsaPublicKey = ECDSA.exportPublicKey(ecdsaKeyPair.publicKey).await()

        val b = ArrayBuffer(ecdhPublicKey.byteLength + ecdsaPublicKey.byteLength)
        Uint8Array(b).set(Uint8Array(ecdhPublicKey), 0)
        Uint8Array(b).set(Uint8Array(ecdsaPublicKey), ecdhPublicKey.byteLength)
        val sign = ECDSA.sign(HashSHA512, ecdsaKeyPair.privateKey, b).await()

        return ECDHRequest(toByteArray(ecdhPublicKey), toByteArray(ecdsaPublicKey), toByteArray(sign))
    }

    suspend fun reconnect(): ECDHReconnect {
        val buffer = toArrayBuffer(auth!!)
        val sign = ECDSA.sign(HashSHA512, ecdsaKeyPair.privateKey, buffer).await()
        return ECDHReconnect(auth!!, toByteArray(sign))
    }

    suspend fun response(response: ECDHResponse): Boolean {
        val serverKeyBuffer = toArrayBuffer(response.ecdhPublicKey)
        val sign = toArrayBuffer(response.sign)
        val buffer = toArrayBuffer(response.auth, response.ecdhPublicKey)
        val verified = ECDSA.verify(HashSHA512, ecdsaServerPublicKey, sign, buffer).await()
        return if (verified) {
            val serverKey = ECDH.importPublicKey(CurveP521, serverKeyBuffer).await()
            // 528 = 66 bytes
            val secret = ECDH.deriveBits(CurveP521, serverKey, ecdhKeyPair.privateKey, 528).await()
            // use SHA-256 as KDF function
            val hash = SHA256.digest(secret).await()
            secretKey = AES.importKey(hash).await()
            this.auth = response.auth
            true
        } else {
            false
        }
    }

    suspend fun decrypt(encrypted: ECDHEncrypted): ArrayBuffer {
        requireNotNull(secretKey)
        val data = toArrayBuffer(encrypted.encrypted)
        val iv = Uint8Array(toArrayBuffer(encrypted.iv))
        val sign = toArrayBuffer(encrypted.sign)
        val decrypted = AES.decrypt(secretKey!!, data, iv).await()
        val buffer = toArrayBuffer(encrypted.auth, encrypted.iv, toByteArray(decrypted))
        val verified = ECDSA.verify(HashSHA512, ecdsaServerPublicKey, sign, buffer).await()
        if (verified) {
            return decrypted
        } else {
            throw IllegalArgumentException("sign not verified")
        }
    }

    suspend fun encrypt(data: ArrayBuffer): ECDHEncrypted {
        requireNotNull(secretKey)
        val (encrypted, iv) = AES.encrypt(secretKey!!, data).await()
        val buffer = toArrayBuffer(auth!!, toByteArray(iv.buffer), toByteArray(data))
        val sign = ECDSA.sign(HashSHA512, ecdsaKeyPair.privateKey, buffer).await()
        return ECDHEncrypted(
            auth!!,
            toByteArray(sign),
            toByteArray(iv.buffer),
            toByteArray(encrypted)
        )
    }
}