package com.github.mitallast.ghost.client.ecdh

import com.github.mitallast.ghost.client.common.await
import com.github.mitallast.ghost.client.common.toArrayBuffer
import com.github.mitallast.ghost.client.common.toByteArray
import com.github.mitallast.ghost.client.profile.ProfileController
import com.github.mitallast.ghost.client.updates.UpdatesController
import com.github.mitallast.ghost.common.codec.Codec
import com.github.mitallast.ghost.common.codec.CodecMessage
import com.github.mitallast.ghost.ecdh.ECDHEncrypted
import com.github.mitallast.ghost.ecdh.ECDHResponse
import com.github.mitallast.ghost.updates.InstallUpdate
import com.github.mitallast.ghost.updates.SendAck
import com.github.mitallast.ghost.updates.Update
import kotlin.browser.window
import kotlin.js.Promise

object ECDHController {
    private var auth: ECDHAuth? = null

    private var connectPromise: Promise<IConnection>? = null
    private var connection: IConnection? = null

    suspend fun start() {
        auth = ECDHAuthStore.loadAuth()
        connectPromise = connect()
        if (auth != null) {
            ProfileController.start(auth!!.auth)
        }
    }

    private fun connect(): Promise<IConnection> {
        return Promise({ resolve, _ ->
            if (auth == null) {
                ConnectionFactory.connect(StartListener(resolve))
            } else {
                ConnectionFactory.connect(ReconnectListener(resolve, auth!!))
            }
        })
    }

    private fun reconnect() {
        connection = null
        connectPromise = Promise({ resolve, reject ->
            window.setTimeout({ connect().then(resolve, reject) }, 5000)
        })
    }

    private suspend fun connection(): IConnection {
        var connection: IConnection? = connection
        while (connection == null || !connection.isConnected()) {
            console.log("await connection...")
            connection = connectPromise!!.await()
            console.log("connected!")
        }
        return connection
    }

    private suspend fun authorized() {
        ProfileController.start(auth!!.auth)
    }

    private suspend fun handle(message: CodecMessage) {
        when (message) {
            is Update -> UpdatesController.handle(message)
            is InstallUpdate -> UpdatesController.handle(message)
            is SendAck -> UpdatesController.handle(message)
            else -> console.warn("unexpected", message)
        }
    }

    fun auth(): ByteArray = auth!!.auth

    suspend fun send(message: CodecMessage) {
        // @todo rewrite promise
        console.log("[ecdh] send message", message)
        val encoded = Codec.anyCodec<CodecMessage>().write(message)
        val encrypted = ECDHCipher.encrypt(auth!!, toArrayBuffer(encoded))
        val connect = connection()
        console.log("[ecdh] send message to connection")
        connect.send(encrypted)
    }

    private class StartListener(private val resolve: (IConnection) -> Unit) : ConnectionListener {
        private val ecdh = ECDHFlow.start()
        private var connect: IConnection? = null

        override suspend fun connected(connection: IConnection) {
            this.connect = connection
            val request = ecdh.request()
            connection.send(request)
        }

        override suspend fun disconnected(connection: IConnection) {
            ECDHController.reconnect()
            resolve.invoke(connection)
        }

        override suspend fun handle(message: CodecMessage) {
            when (message) {
                is ECDHResponse -> {
                    if (auth == null) {
                        auth = ecdh.response(message)
                        ECDHController.authorized()
                        resolve(connect!!)
                    } else {
                        console.error("unexpected ecdh request message")
                        connect!!.close()
                    }
                }
                is ECDHEncrypted -> {
                    if (auth == null) {
                        console.error("unexpected ecdh encrypted message")
                        connect!!.close()
                    } else {
                        val decrypted = ECDHCipher.decrypt(auth!!, message)
                        val decoded = Codec.anyCodec<CodecMessage>().read(toByteArray(decrypted))
                        ECDHController.handle(decoded)
                    }
                }
                else -> console.warn("unexpected message from server", message)
            }
        }
    }

    private class ReconnectListener(
        private val resolve: (IConnection) -> Unit,
        private val auth: ECDHAuth
    ) : ConnectionListener {
        private var connect: IConnection? = null
        override suspend fun connected(connection: IConnection) {
            this.connect = connection
            val request = ECDHFlow.reconnect(auth)
            connection.send(request)
            resolve.invoke(connection)
            UpdatesController.maybeSend()
        }

        override suspend fun disconnected(connection: IConnection) {
            ECDHController.reconnect()
            resolve.invoke(connection)
        }

        override suspend fun handle(message: CodecMessage) {
            when (message) {
                is ECDHEncrypted -> {
                    val decrypted = ECDHCipher.decrypt(auth, message)
                    val decoded = Codec.anyCodec<CodecMessage>().read(toByteArray(decrypted))
                    ECDHController.handle(decoded)
                }
                else -> console.warn("unexpected message from server", message)
            }
        }
    }
}