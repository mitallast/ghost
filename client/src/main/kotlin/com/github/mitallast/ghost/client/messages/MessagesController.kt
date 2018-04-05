package com.github.mitallast.ghost.client.messages

import com.github.mitallast.ghost.client.common.launch
import com.github.mitallast.ghost.client.common.toByteArray
import com.github.mitallast.ghost.client.crypto.HEX
import com.github.mitallast.ghost.client.crypto.crypto
import com.github.mitallast.ghost.client.e2e.E2EFlow
import com.github.mitallast.ghost.client.html.div
import com.github.mitallast.ghost.client.profile.ProfileController
import com.github.mitallast.ghost.client.profile.SidebarDialogsController
import com.github.mitallast.ghost.client.view.ContentFooterController
import com.github.mitallast.ghost.client.view.ContentHeaderView
import com.github.mitallast.ghost.client.view.ContentMainController
import com.github.mitallast.ghost.client.view.View
import com.github.mitallast.ghost.common.codec.Codec
import com.github.mitallast.ghost.message.Message
import com.github.mitallast.ghost.message.MessageContent
import com.github.mitallast.ghost.message.TextMessage
import com.github.mitallast.ghost.profile.UserProfile
import org.khronos.webgl.Uint8Array
import kotlin.js.Date

object MessagesController {
    private val messagesMap = HashMap<String, MessagesListController>()

    suspend fun lastMessage(id: ByteArray): Message? {
        return MessagesStore.lastMessage(id)
    }

    suspend fun open(profile: UserProfile) {
        val key = HEX.toHex(profile.id)
        val exist = messagesMap[key]
        if (exist == null) {
            val self = ProfileController.profile()
            val controller = MessagesListController(self, profile)
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
            val self = ProfileController.profile()
            val profile = ProfileController.profile(from)
            controller = MessagesListController(self, profile)
            messagesMap[key] = controller
        }
        controller.handle(message)
    }
}

class MessagesListController(private val self: UserProfile, private val profile: UserProfile) {
    private val listView = MessagesListView()
    private val editorView = MessageEditorView(this)
    private var last: Message? = null

    init {
        launch { historyTop() }
    }

    private suspend fun historyTop() {
        MessagesStore.historyTop(profile.id, 100)
            .asReversed()
            .forEach { show(it) }
    }

    fun show() {
        ContentHeaderView.setTitle(profile.fullname)
        ContentMainController.view(listView)
        ContentFooterController.view(editorView)
    }

    suspend fun send(content: MessageContent) {
        val buffer = Uint8Array(4)
        crypto.getRandomValues(buffer)
        val randomId = Codec.longCodec().read(toByteArray(buffer.buffer))
        val message = Message(
            date = Date.now().toLong(),
            randomId = randomId,
            sender = self.id,
            content = content
        )
        MessagesStore.put(profile.id, message)
        show(message)
        E2EFlow.send(profile.id, message)
    }

    suspend fun handle(message: Message) {
        MessagesStore.put(profile.id, message)
        show(message)
    }

    private fun show(message: Message) {
        val own = !message.sender.contentEquals(profile.id)
        val view = MessageView(message, own)
        listView.add(view)
        updateDialog(message)
    }

    private fun updateDialog(message: Message) {
        if (last == null || last!!.olderThan(message)) {
            last = message
            SidebarDialogsController.update(profile.id, message)
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

class MessageView(message: Message, own: Boolean) : View {
    override val root = div {
        if (own) {
            attr("class", "message-container message-own")
        } else {
            attr("class", "message-container")
        }
        val content = message.content
        when (content) {
            is TextMessage -> div {
                attr("class", "message")
                div {
                    attr("class", "message-text")
                    text(content.text)
                }
                div {
                    attr("class", "message-time")
                    text(timeFormat(message.date))
                }
            }
        }
    }

    private fun timeFormat(timestamp: Long): String {
        val date = Date(timestamp)
        val hh = date.getHours().toString().padStart(2, '0')
        val mm = date.getMinutes().toString().padStart(2, '0')
        return "$hh:$mm"
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