package com.github.mitallast.ghost.client.messages

import com.github.mitallast.ghost.client.common.launch
import com.github.mitallast.ghost.client.crypto.HEX
import com.github.mitallast.ghost.client.e2e.E2EFlow
import com.github.mitallast.ghost.client.html.div
import com.github.mitallast.ghost.client.profile.ProfileController
import com.github.mitallast.ghost.client.view.ContentFooterController
import com.github.mitallast.ghost.client.view.ContentHeaderView
import com.github.mitallast.ghost.client.view.ContentMainController
import com.github.mitallast.ghost.client.view.View
import com.github.mitallast.ghost.common.codec.Message
import com.github.mitallast.ghost.message.TextMessage
import com.github.mitallast.ghost.profile.UserProfile

object MessagesController {
    private val messagesMap = HashMap<String, MessagesListController>()

    fun open(dialog: UserProfile) {
        val key = HEX.toHex(dialog.id)
        val exist = messagesMap[key]
        if (exist == null) {
            val controller = MessagesListController(dialog)
            messagesMap[key] = controller
            controller.show()
        } else {
            exist.show()
        }
    }

    suspend fun handle(from: ByteArray, message: Message) {
        val key = HEX.toHex(from)
        var controller = messagesMap[key]
        if (controller == null) {
            val profile = ProfileController.profile(from)
            controller = MessagesListController(profile)
            messagesMap[key] = controller
        }
        controller.handle(message)
    }
}

class MessagesListController(private val profile: UserProfile) {
    private val listView = MessagesListView()
    private val editorView = MessageEditorView(this)

    fun show() {
        ContentHeaderView.setTitle(profile.fullname)
        ContentMainController.view(listView)
        ContentFooterController.view(editorView)
    }

    suspend fun send(message: TextMessage) {
        val self = ProfileController.profile()
        val view = MessageView(self, message, true)
        listView.add(view)
        E2EFlow.send(profile.id, message)
    }

    suspend fun handle(message: Message) {
        when (message) {
            is TextMessage -> listView.add(MessageView(profile, message, false))
        }
    }
}

class MessagesListView : View {
    private val list = div {
        attr("class", "messages-scroll")
    }
    override val root = div {
        attr("class", "messages-list")
        append(list)
    }

    fun add(view: View) {
        list.append(view.root)
    }
}

class MessageView(from: UserProfile, message: TextMessage, own: Boolean) : View {
    override val root = div {
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

class MessageEditorView(private val controller: MessagesListController) : View {
    override val root = div {
        attr("class", "message-editor")
        textarea {
            on("keypress", { event ->
                val key: Int = event.asDynamic().keyCode;
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