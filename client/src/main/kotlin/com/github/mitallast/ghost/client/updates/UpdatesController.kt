package com.github.mitallast.ghost.client.updates

import com.github.mitallast.ghost.client.e2e.E2EController
import com.github.mitallast.ghost.client.ecdh.ECDHController
import com.github.mitallast.ghost.common.codec.CodecMessage
import com.github.mitallast.ghost.e2e.E2EAuthCanceled
import com.github.mitallast.ghost.e2e.E2EAuthRequest
import com.github.mitallast.ghost.e2e.E2EAuthResponse
import com.github.mitallast.ghost.e2e.E2EEncrypted
import com.github.mitallast.ghost.updates.*

object UpdatesController {
    suspend fun handle(message: CodecMessage) {
        when (message) {
            is Update -> {
                val lastInstalled = UpdatesStore.loadLastInstalled()
                if (lastInstalled + 1 == message.sequence) {
                    console.log("update received", message)
                    installUpdate(message.from, message.update)
                    UpdatesStore.updateLastInstalled(message.sequence)
                    ECDHController.send(UpdateInstalled(message.sequence))
                }
            }
            is InstallUpdate -> {
                console.log("install update received", message)
                var error = false
                var lastInstalled = UpdatesStore.loadLastInstalled()
                for (update in message.updates) {
                    if (lastInstalled + 1 == update.sequence) {
                        installUpdate(update.from, update.update)
                        lastInstalled = update.sequence
                    } else {
                        console.error("sequence does not match last=$lastInstalled update=${update.sequence}")
                        error = true
                        break
                    }
                }
                UpdatesStore.updateLastInstalled(lastInstalled)
                if (error) {
                    console.info("send update rejected $lastInstalled")
                    ECDHController.send(UpdateRejected(lastInstalled))
                } else {
                    console.info("send update installed $lastInstalled")
                    ECDHController.send(UpdateInstalled(lastInstalled))
                }
            }
            is SendAck -> {
                console.log("send ack received ${message.randomId}")
                UpdatesStore.complete(message.randomId)
                maybeSend()
            }
        }
    }

    suspend fun send(address: ByteArray, message: CodecMessage) {
        val send = UpdatesStore.queue(address, message)
        console.log("send update ${send.randomId}")
        ECDHController.send(send)
    }

    private suspend fun installUpdate(from: ByteArray, update: CodecMessage) {
        console.log("install update", update)
        when (update) {
            is E2EAuthRequest -> E2EController.handle(from, update)
            is E2EAuthResponse -> E2EController.handle(from, update)
            is E2EAuthCanceled -> E2EController.handle(from, update)
            is E2EEncrypted -> E2EController.handle(from, update)
            else -> console.error("unexpected message", from, update)
        }
    }

    suspend fun maybeSend() {
        val send = UpdatesStore.peek()
        if (send != null) {
            console.log("resend update ${send.randomId}")
            ECDHController.send(send)
        }
    }
}