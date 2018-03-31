package com.github.mitallast.ghost.dh

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

    init {
        socket.binaryType = BinaryType.ARRAYBUFFER
        socket.onopen = { e ->
            launch {
                console.log(e)
                echd = ECDHFlow.start()
                val request = echd!!.request()
                val out = Codec.anyCodec<ECDHRequest>().write(request)
                console.log("ecdh request")
                socket.send(toArrayBuffer(out))
            }
        }
        socket.onmessage = { e ->
            console.log(e)
            val bytes = toByteArray(e.asDynamic()["data"])
            val msg = Codec.anyCodec<Message>().read(bytes)
            when (msg) {
                is ECDHResponse -> {
                    launch {
                        console.log("ecdh response")
                        if (echd!!.response(msg)) {
                            console.log("send hello world")
                            send(TextMessage("hello world"))
                        }
                    }
                }
                is ECDHEncrypted -> {
                    launch {
                        console.log("ecdh encrypted")
                        val decrypted = echd!!.decrypt(msg)
                        console.log("decrypted", decrypted)
                        val decoded = Codec.anyCodec<Message>().read(toByteArray(decrypted))
                        console.log("decoded", decoded)
                    }
                }
                else ->
                    console.log("received", msg)
            }
        }
        socket.onerror = { e ->
            console.log(e)
        }
        socket.onclose = { e ->
            console.log(e)
        }
    }

    fun send(message: Message) {
        requireNotNull(echd)
        val coded = Codec.anyCodec<Message>().write(message)
        launch {
            val encrypted = echd!!.encrypt(toArrayBuffer(coded))
            val data = Codec.anyCodec<Message>().write(encrypted)
            socket.send(toArrayBuffer(data))
        }
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
}