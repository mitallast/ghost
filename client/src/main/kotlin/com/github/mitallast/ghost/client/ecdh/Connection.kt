package com.github.mitallast.ghost.client.ecdh

import com.github.mitallast.ghost.client.common.launch
import com.github.mitallast.ghost.client.common.toArrayBuffer
import com.github.mitallast.ghost.client.common.toByteArray
import com.github.mitallast.ghost.common.codec.Codec
import com.github.mitallast.ghost.common.codec.Message
import org.w3c.dom.ARRAYBUFFER
import org.w3c.dom.BinaryType
import org.w3c.dom.WebSocket

interface IConnection {
    fun isConnected(): Boolean
    fun send(message: Message)
    fun close()
}

interface ConnectionListener {
    suspend fun connected(connection: IConnection)
    suspend fun disconnected(connection: IConnection)
    suspend fun handle(message: Message)
}

object ConnectionFactory {
    private const val url = "ws://localhost:8800/ws/"

    fun connect(listener: ConnectionListener) {
        console.log("connect to", url)
        val socket = WebSocket(url)
        val connection = object : IConnection {
            private var connected: Boolean = true

            override fun isConnected(): Boolean = connected

            override fun send(message: Message) {
                if (isConnected()) {
                    val encoded = Codec.anyCodec<Message>().write(message)
                    console.info("socket", socket.readyState)
                    socket.send(toArrayBuffer(encoded))
                    console.info("message sent")
                } else {
                    console.warn("send message to closed connection")
                }
            }

            override fun close() {
                if (connected) {
                    connected = false
                    socket.close()
                }
            }
        }
        socket.binaryType = BinaryType.ARRAYBUFFER
        socket.onopen = {
            console.log("connection opened")
            launch { listener.connected(connection) }
        }
        socket.onmessage = { e ->
            val bytes = toByteArray(e.asDynamic().data)
            val message = Codec.anyCodec<Message>().read(bytes)
            console.log("received", message)
            launch { listener.handle(message) }
        }
        socket.onerror = {
            console.log("connection error", it)
        }
        socket.onclose = {
            console.log("connection closed", it)
            if (connection.isConnected()) {
                connection.close()
                launch { listener.disconnected(connection) }
            }
        }
    }
}