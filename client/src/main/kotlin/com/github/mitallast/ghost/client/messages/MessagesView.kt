package com.github.mitallast.ghost.client.messages

import com.github.mitallast.ghost.client.html.div
import com.github.mitallast.ghost.message.TextMessage

object MessagesView {
    val root = div {
        attr("class", "messages-scroll")
    }

    fun clear() {
        console.log("clear")
        root.removeChildren()
    }

    fun addOwn(from: ByteArray, message: TextMessage) {
        val view = MessageView(from, message, true)
        root.append(view.root)
    }

    fun addFrom(from: ByteArray, message: TextMessage) {
        val view = MessageView(from, message, false)
        root.append(view.root)
    }
}

class MessageView(from: ByteArray, message: TextMessage, own: Boolean) {
    val root = div {
        if (own) {
            attr("class", "message-container message-own")
        } else {
            attr("class", "message-container")
        }
        div {
            attr("class", "message")
            div {
                attr("class", "message-text")
                text(message.text)
            }
            div {
                attr("class", "message-time")
                text("00:00")
            }
        }
    }
}
