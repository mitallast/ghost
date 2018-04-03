package com.github.mitallast.ghost.client.connection

import com.github.mitallast.ghost.client.common.await
import com.github.mitallast.ghost.client.common.launch
import com.github.mitallast.ghost.client.common.toArrayBuffer
import com.github.mitallast.ghost.client.common.toByteArray
import com.github.mitallast.ghost.client.crypto.HEX
import com.github.mitallast.ghost.client.ecdh.*
import com.github.mitallast.ghost.client.updates.UpdatesFlow
import com.github.mitallast.ghost.common.codec.Codec
import com.github.mitallast.ghost.common.codec.Message
import com.github.mitallast.ghost.message.TextMessage
import com.github.mitallast.ghost.updates.InstallUpdate
import com.github.mitallast.ghost.updates.Update
import org.w3c.dom.ARRAYBUFFER
import org.w3c.dom.BinaryType
import org.w3c.dom.WebSocket
import kotlin.browser.window
import kotlin.js.Promise

object ConnectionService {
    private const val url = "ws://localhost:8800/ws/"
    private var connectionId: Int = 0

    private fun connect(): Promise<Connection> = Promise({ resolve, _ ->
        console.log("connect to", url)
        var echd: ECDHFlow? = null
        var auth: ECDHAuth? = null
        val socket = WebSocket(url)
        val connection = object : Connection {
            private var id = connectionId++
            private var connected: Boolean = true
            override fun id(): Int = id
            override fun auth(): ECDHAuth = auth!!
            override fun isConnected(): Boolean = connected
            override fun send(message: Message) {
                val coded = Codec.anyCodec<Message>().write(message)
                launch {
                    val encrypted = ECDHCipher.encrypt(auth!!, toArrayBuffer(coded))
                    val data = Codec.anyCodec<Message>().write(encrypted)
                    socket.send(toArrayBuffer(data))
                }
            }

            fun close() {
                connected = false
            }
        }
        socket.binaryType = BinaryType.ARRAYBUFFER
        socket.onopen = {
            launch {
                console.log("connection opened")
                auth = ECDHAuthStore.loadAuth()
                if (auth == null) {
                    console.log("start ECDH")
                    echd = ECDHFlow.start()
                    val request = echd!!.request()
                    val out = Codec.anyCodec<ECDHRequest>().write(request)
                    console.log("ecdh request")
                    socket.send(toArrayBuffer(out))
                } else {
                    console.log("reconnect ECDH", HEX.toHex(auth!!.auth))
                    val reconnect = ECDHFlow.reconnect(auth!!)
                    val out = Codec.anyCodec<ECDHReconnect>().write(reconnect)
                    socket.send(toArrayBuffer(out))
                    resolve.invoke(connection)
                }
            }
        }
        socket.onmessage = { e ->
            console.log("received", e)
            val bytes = toByteArray(e.asDynamic().data)
            val msg = Codec.anyCodec<Message>().read(bytes)
            when (msg) {
                is ECDHResponse -> {
                    launch {
                        console.log("ecdh response received")
                        auth = echd!!.response(msg)
                        console.log("send hello world")
                        send(TextMessage("hello world"))
                        resolve.invoke(connection)
                    }
                }
                is ECDHEncrypted -> {
                    launch {
                        console.log("ecdh encrypted received")
                        val decrypted = ECDHCipher.decrypt(auth!!, msg)
                        console.log("decrypted", decrypted)
                        val decoded = Codec.anyCodec<Message>().read(toByteArray(decrypted))
                        console.log("decoded", decoded)
                        handle(decoded)
                    }
                }
                else ->
                    console.warn("unexpected message from server", msg)
            }
        }
        socket.onerror = {
            console.log("connection error", it)
        }
        socket.onclose = {
            console.log("connection closed", it)
            if (connection.isConnected()) {
                connection.close()
                reconnect()
                resolve.invoke(connection)
            }
        }
    })

    private var connectPromise: Promise<Connection> = connect()
    private var connection: Connection? = null

    private fun reconnect() {
        console.log("reconnect")
        connection = null
        connectPromise = Promise({ resolve, reject ->
            window.setTimeout({

                connect().then(resolve, reject)
            }, 5000)
        })
    }

    suspend fun connection(): Connection {
        var connection: Connection? = connection
        while (connection == null || !connection.isConnected()) {
            console.log("await connection...")
            connection = connectPromise.await()
            console.log("connected!")
        }
        return connection
    }

    suspend fun send(message: Message) {
        console.log("send", message)
        connection().send(message)
    }

    private suspend fun handle(message: Message) {
        when (message) {
            is Update -> {
                UpdatesFlow.handle(message)
            }
            is InstallUpdate -> {
                UpdatesFlow.handle(message)
            }
            else ->
                console.warn("unexpected", message)
        }
    }
}

interface Connection {
    fun id(): Int
    fun auth(): ECDHAuth
    fun isConnected(): Boolean
    fun send(message: Message)
}
