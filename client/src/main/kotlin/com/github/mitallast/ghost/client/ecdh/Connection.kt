package com.github.mitallast.ghost.client.ecdh

import com.github.mitallast.ghost.client.common.await
import com.github.mitallast.ghost.client.common.launch
import com.github.mitallast.ghost.client.common.toArrayBuffer
import com.github.mitallast.ghost.client.common.toByteArray
import com.github.mitallast.ghost.client.crypto.HEX
import com.github.mitallast.ghost.common.codec.Codec
import com.github.mitallast.ghost.common.codec.Message
import com.github.mitallast.ghost.client.e2e.E2EDHFlow
import com.github.mitallast.ghost.e2ee.E2EEncrypted
import com.github.mitallast.ghost.e2ee.E2ERequest
import com.github.mitallast.ghost.e2ee.E2EResponse
import com.github.mitallast.ghost.message.TextMessage
import org.w3c.dom.ARRAYBUFFER
import org.w3c.dom.BinaryType
import org.w3c.dom.WebSocket
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
        socket.onopen = { e ->
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
                        when (decoded) {
                            is E2ERequest -> {
                                console.log("e2e request received")
                                val response = E2EDHFlow.response(auth!!, decoded)
                                send(response)
                            }
                            is E2EResponse -> {
                                console.log("e2e response received")
                                val e2eAuth = E2EDHFlow.keyAgreement(decoded)
                                send(e2eAuth.auth, TextMessage("hello world"))
                            }
                            is E2EEncrypted -> {
                                console.log("e2e encrypted received")
                                val decrypted = E2EDHFlow.decrypt(decoded)
                                console.log("e2e received", decrypted)
                            }
                        }
                    }
                }
                else ->
                    console.warn("unexpected message from server", msg)
            }
        }
        socket.onerror = {
            console.log("connection error", it)
            console.log(it)
            connection.close()
            reconnect()
            resolve.invoke(connection)
        }
        socket.onclose = {
            console.log("closed", it)
            connection.close()
            reconnect()
            resolve.invoke(connection)
        }
    })

    private fun reconnect() {
        console.log("reconnect")
        connectPromise = connect()
    }

    private var connectPromise: Promise<Connection> = connect()
    private var connection: Connection? = null

    suspend fun connection(): Connection {
        while (connection == null || !connection!!.isConnected()) {
            console.log("await connection...")
            connection = connectPromise.await()
            console.log("connected!")
        }
        return connection!!
    }

    suspend fun send(message: Message) {
        console.log("send", message)
        connection().send(message)
    }

    suspend fun send(to: ByteArray, message: Message) {
        console.log("send e2e", HEX.toHex(to), message)
        val encoded = Codec.anyCodec<Message>().write(message)
        val encrypted = E2EDHFlow.encrypt(to, toArrayBuffer(encoded))
        send(encrypted)
    }
}

interface Connection {
    fun id(): Int
    fun auth(): ECDHAuth
    fun isConnected(): Boolean
    fun send(message: Message)
}
