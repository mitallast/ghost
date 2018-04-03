package com.github.mitallast.ghost.client.messages

import com.github.mitallast.ghost.client.connection.ConnectionService
import com.github.mitallast.ghost.client.e2e.E2EFlow
import com.github.mitallast.ghost.common.codec.Message
import com.github.mitallast.ghost.message.TextMessage

object MessagesFlow {
    suspend fun handle(from: ByteArray, message: Message) {
        when (message) {
            is TextMessage -> MessagesView.add(from, message)
        }
    }

    suspend fun showHistory(auth: ByteArray) {
        console.log("show history", MessagesView)
        MessagesView.clear()
        MessagesView.showHistory(auth)
    }

    suspend fun send(auth: ByteArray, message: TextMessage) {
        val self = ConnectionService.connection().auth()
        E2EFlow.send(auth, message)
        MessagesView.add(self.auth, message)
    }
}