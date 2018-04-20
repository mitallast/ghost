package com.github.mitallast.ghost.client.messages

import com.github.mitallast.ghost.client.common.launch
import com.github.mitallast.ghost.client.html.div
import com.github.mitallast.ghost.client.view.View
import com.github.mitallast.ghost.message.MessageContent
import com.github.mitallast.ghost.message.TextMessage

interface MessageSendController {
    suspend fun send(content: MessageContent)
}

class MessageEditorView(private val controller: MessageSendController) : View {
    override val root = div {
        clazz("message-editor")
        textarea {
            on("keypress", { event ->
                val key = event.keyCode as Int
                if (key == 13) {
                    event.preventDefault()
                    val text = value()
                    if (text.isNotEmpty()) {
                        clear()
                        launch { controller.send(TextMessage(text)) }
                    } else {
                        focus()
                    }
                }
            })
        }
    }
}