package com.github.mitallast.ghost.dh

import com.github.mitallast.ghost.client.crypto.AES
import com.github.mitallast.ghost.client.crypto.ECDSA
import com.github.mitallast.ghost.client.crypto.HashSHA512
import com.github.mitallast.ghost.common.codec.Codec
import com.github.mitallast.ghost.common.codec.Message
import com.github.mitallast.ghost.message.TextMessage
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Uint8Array
import org.khronos.webgl.get
import org.khronos.webgl.set
import org.w3c.dom.ARRAYBUFFER
import org.w3c.dom.BinaryType
import org.w3c.dom.WebSocket

class Connection {
    private val url = "ws://localhost:8800/ws/"
    private val socket = WebSocket(url)

    private var echd: ECDHFlow? = null
    private var auth: Auth? = null

    init {
        socket.binaryType = BinaryType.ARRAYBUFFER
        socket.onopen = { e ->
            launch {
                console.log(e)
                auth = AuthStore.loadAuth()
                if (auth == null) {
                    echd = ECDHFlow.start()
                    val request = echd!!.request()
                    val out = Codec.anyCodec<ECDHRequest>().write(request)
                    console.log("ecdh request")
                    socket.send(toArrayBuffer(out))
                } else {
                    val reconnect = ECDHFlow.reconnect(auth!!)
                    val out = Codec.anyCodec<ECDHReconnect>().write(reconnect)
                    console.log("ecdh reconnect")
                    socket.send(toArrayBuffer(out))
                    send(TextMessage("hello world"))
                }
            }
        }
        socket.onmessage = { e ->
            console.log(e)
            val bytes = toByteArray(e.asDynamic().data)
            val msg = Codec.anyCodec<Message>().read(bytes)
            when (msg) {
                is ECDHResponse -> {
                    launch {
                        console.log("ecdh response received")
                        auth = echd!!.response(msg)
                        AuthStore.storeAuth(auth!!)
                        console.log("send hello world")
                        send(TextMessage("hello world"))
                    }
                }
                is ECDHEncrypted -> {
                    launch {
                        console.log("ecdh encrypted received")
                        val decrypted = decrypt(auth!!, msg)
                        console.log("decrypted", decrypted)
                        val decoded = Codec.anyCodec<Message>().read(toByteArray(decrypted))
                        console.log("decoded", decoded)
                    }
                }
                else ->
                    console.log("received", msg)
            }
        }
        socket.onerror = { console.log(it) }
        socket.onclose = { console.log(it) }
    }

    fun send(message: Message) {
        val coded = Codec.anyCodec<Message>().write(message)
        launch {
            val encrypted = encrypt(auth!!, toArrayBuffer(coded))
            val data = Codec.anyCodec<Message>().write(encrypted)
            socket.send(toArrayBuffer(data))
        }
    }

    companion object {
        suspend fun decrypt(auth: Auth, encrypted: ECDHEncrypted): ArrayBuffer {
            val serverKey = ECDHFlow.serverPublicKey()
            val data = toArrayBuffer(encrypted.encrypted)
            val iv = Uint8Array(toArrayBuffer(encrypted.iv))
            val sign = toArrayBuffer(encrypted.sign)
            val decrypted = AES.decrypt(auth.secretKey, data, iv).await()
            val buffer = toArrayBuffer(encrypted.auth, encrypted.iv, toByteArray(decrypted))
            val verified = ECDSA.verify(HashSHA512, serverKey, sign, buffer).await()
            if (verified) {
                return decrypted
            } else {
                throw IllegalArgumentException("sign not verified")
            }
        }

        suspend fun encrypt(auth: Auth, data: ArrayBuffer): ECDHEncrypted {
            val (encrypted, iv) = AES.encrypt(auth.secretKey, data).await()
            val buffer = toArrayBuffer(auth.auth, toByteArray(iv.buffer), toByteArray(data))
            val sign = ECDSA.sign(HashSHA512, auth.privateKey, buffer).await()
            return ECDHEncrypted(
                auth.auth,
                toByteArray(sign),
                toByteArray(iv.buffer),
                toByteArray(encrypted)
            )
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
}