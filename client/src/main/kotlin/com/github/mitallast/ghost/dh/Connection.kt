package com.github.mitallast.ghost.dh

import com.github.mitallast.ghost.common.codec.Codec
import com.github.mitallast.ghost.common.codec.Message
import com.github.mitallast.ghost.message.TextMessage
import kotlinx.io.ByteArrayInputStream
import kotlinx.io.ByteArrayOutputStream
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Uint8Array
import org.khronos.webgl.get
import org.khronos.webgl.set
import org.w3c.dom.ARRAYBUFFER
import org.w3c.dom.BinaryType
import org.w3c.dom.WebSocket

class Connection {
    val url = "ws://localhost:8800/ws/"
    val socket = WebSocket(url)

    var echd: ECDHFlow? = null

    init {
        socket.binaryType = BinaryType.ARRAYBUFFER
        socket.onopen = { e ->
            launch {
                console.log(e)
                echd = ECDHFlow.start()
                val request = echd!!.request()
                val out = ByteArrayOutputStream()
                Codec.anyCodec<ECDHRequest>().write(out, request)
                console.log("ecdh request")
                socket.send(toArrayBuffer(out.toByteArray()))
            }
        }
        socket.onmessage = { e ->
            console.log(e)
            val bytes = toByteArray(e.asDynamic()["data"])
            val input = ByteArrayInputStream(bytes)
            val msg = Codec.anyCodec<Message>().read(input)
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
        var out = ByteArrayOutputStream()
        Codec.anyCodec<Message>().write(out, message)
        val coded = out.toByteArray()
        launch {
            val encrypted = echd!!.encrypt(toArrayBuffer(coded))
            out = ByteArrayOutputStream()
            Codec.anyCodec<Message>().write(out, encrypted)
            val data = out.toByteArray()
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