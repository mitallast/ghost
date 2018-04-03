package com.github.mitallast.ghost.client.dialogs

import com.github.mitallast.ghost.client.app.ChatView
import com.github.mitallast.ghost.client.common.await
import com.github.mitallast.ghost.client.common.launch
import com.github.mitallast.ghost.client.common.toByteArray
import com.github.mitallast.ghost.client.crypto.HEX
import com.github.mitallast.ghost.client.e2e.E2EFlow
import com.github.mitallast.ghost.client.html.a
import com.github.mitallast.ghost.client.messages.MessagesFlow
import com.github.mitallast.ghost.client.prompt.PromptView
import com.github.mitallast.ghost.message.TextMessage
import org.w3c.dom.Text

object DialogsFlow {
    private var currentDialog: ByteArray? = null

    suspend fun newContact(auth: ByteArray) {
        DialogsStore.add(auth)
        DialogsView.addDialog(auth)
    }

    suspend fun load() {
        val dialogs = DialogsStore.load()
        DialogsView.clear()
        dialogs.forEach { DialogsView.addDialog(it) }
    }

    fun open(auth: ByteArray) {
        if (currentDialog == null || !currentDialog!!.contentEquals(auth)) {
            currentDialog = auth
            ChatView.currentDialog(auth)
            MessagesFlow.showHistory(auth)
        }
    }

    fun send(message: TextMessage) {
        if (currentDialog != null) {
            launch {
                MessagesFlow.send(currentDialog!!, message)
            }
        }
    }

    fun add() {
        launch {
            val hex = PromptView.prompt("Enter address").await()
            if(hex.isNotEmpty()) {
                val auth = HEX.parseHex(hex)
                E2EFlow.connect(toByteArray(auth.buffer))
            }
        }
    }
}