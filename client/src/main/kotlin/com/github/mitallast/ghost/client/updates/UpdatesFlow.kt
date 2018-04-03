package com.github.mitallast.ghost.client.updates

import com.github.mitallast.ghost.client.connection.ConnectionService
import com.github.mitallast.ghost.client.e2e.E2EFlow
import com.github.mitallast.ghost.common.codec.Message
import com.github.mitallast.ghost.e2ee.E2EEncrypted
import com.github.mitallast.ghost.e2ee.E2ERequest
import com.github.mitallast.ghost.e2ee.E2EResponse
import com.github.mitallast.ghost.updates.InstallUpdate
import com.github.mitallast.ghost.updates.Update
import com.github.mitallast.ghost.updates.UpdateInstalled
import com.github.mitallast.ghost.updates.UpdateRejected

object UpdatesFlow {
    suspend fun handle(message: Message) {
        when (message) {
            is Update -> {
                val lastInstalled = UpdatesStore.loadLastInstalled()
                if (lastInstalled + 1 == message.sequence) {
                    console.log("update received", message)
                    installUpdate(message.update)
                    UpdatesStore.updateLastInstalled(message.sequence)
                    ConnectionService.send(UpdateInstalled(message.sequence))
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
                    ConnectionService.send(UpdateRejected(lastInstalled))
                } else {
                    console.info("send update installed", lastInstalled)
                    ConnectionService.send(UpdateInstalled(lastInstalled))
                }
            }
        }
    }

    private suspend fun installUpdate(update: Message) {
        console.log("install update", update)
        when (update) {
            is E2ERequest -> E2EFlow.handle(update)
            is E2EResponse -> E2EFlow.handle(update)
            is E2EEncrypted -> E2EFlow.handle(update)
        }
    }
}