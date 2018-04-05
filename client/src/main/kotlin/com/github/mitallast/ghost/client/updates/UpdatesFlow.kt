package com.github.mitallast.ghost.client.updates

import com.github.mitallast.ghost.client.e2e.E2EFlow
import com.github.mitallast.ghost.client.ecdh.ECDHController
import com.github.mitallast.ghost.common.codec.CodecMessage
import com.github.mitallast.ghost.e2e.E2EEncrypted
import com.github.mitallast.ghost.e2e.E2ERequest
import com.github.mitallast.ghost.e2e.E2EResponse
import com.github.mitallast.ghost.updates.InstallUpdate
import com.github.mitallast.ghost.updates.Update
import com.github.mitallast.ghost.updates.UpdateInstalled
import com.github.mitallast.ghost.updates.UpdateRejected

object UpdatesFlow {
    suspend fun handle(message: CodecMessage) {
        when (message) {
            is Update -> {
                val lastInstalled = UpdatesStore.loadLastInstalled()
                if (lastInstalled + 1 == message.sequence) {
                    console.log("update received", message)
                    installUpdate(message.update)
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
                        installUpdate(update.update)
                        lastInstalled = update.sequence
                    } else {
                        console.error("sequence does not match last=$lastInstalled update=${update.sequence}")
                        error = true
                        break
                    }
                }
                UpdatesStore.updateLastInstalled(lastInstalled)
                if (error) {
                    console.info("send update rejected", lastInstalled)
                    ECDHController.send(UpdateRejected(lastInstalled))
                } else {
                    console.info("send update installed", lastInstalled)
                    ECDHController.send(UpdateInstalled(lastInstalled))
                }
            }
        }
    }

    private suspend fun installUpdate(update: CodecMessage) {
        console.log("install update", update)
        when (update) {
            is E2ERequest -> E2EFlow.handle(update)
            is E2EResponse -> E2EFlow.handle(update)
            is E2EEncrypted -> E2EFlow.handle(update)
        }
    }
}