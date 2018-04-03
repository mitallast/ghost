package com.github.mitallast.ghost.client.messages

import com.github.mitallast.ghost.client.common.launch
import com.github.mitallast.ghost.client.crypto.HEX
import com.github.mitallast.ghost.message.TextMessage
import org.w3c.dom.get
import kotlin.browser.window

object MessagesView {
    private var currentDialog: ByteArray? = null
    val container = window.document.getElementById("messages")!!
    val messagesSend = window.document.getElementById("messagesSend")!!
    val sendButton = window.document.getElementById("sendButton")!!
    val sendText = window.document.getElementById("sendText")!!

    var messages = ArrayList<MessageView>()

    init {
        sendButton.addEventListener("click", {
            if (currentDialog != null) {
                val text: String? = sendText.asDynamic().value
                if (text != null) {
                    launch {
                        MessagesFlow.send(currentDialog!!, TextMessage(text))
                    }
                } else {
                    console.log("text is blank")
                }
            } else {
                console.log("no current dialog")
            }
        })
    }

    suspend fun clear() {
        console.log("clear")
        messages = ArrayList()
        val nodes = container.childNodes
        for (i in 0 until nodes.length) {
            container.removeChild(nodes[i]!!)
        }
        messagesSend.asDynamic().style.display = "none"
    }

    suspend fun showHistory(dialog: ByteArray) {
        currentDialog = dialog
        messagesSend.asDynamic().style.display = null
    }

    suspend fun add(from: ByteArray, message: TextMessage) {
        val view = MessageView(from, message)
        messages.add(view)
        container.appendChild(view.container)
    }
}

class MessageView(val from: ByteArray, val message: TextMessage) {
    val container = window.document.createElement("div")
    val fromSpan = window.document.createElement("snan")
    val pre = window.document.createElement("pre")
    val fromText = window.document.createTextNode(HEX.toHex(from))
    val messageText = window.document.createTextNode(message.text)

    init {
        fromSpan.appendChild(fromText)
        pre.appendChild(messageText)
        container.appendChild(fromSpan)
        container.appendChild(pre)
    }
}
