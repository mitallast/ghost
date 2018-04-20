package com.github.mitallast.ghost.client.profile

import com.github.mitallast.ghost.client.common.launch
import com.github.mitallast.ghost.client.common.toByteArray
import com.github.mitallast.ghost.client.crypto.crypto
import com.github.mitallast.ghost.client.dialog.DialogController
import com.github.mitallast.ghost.client.dialog.SidebarDialogsController
import com.github.mitallast.ghost.client.e2e.E2EController
import com.github.mitallast.ghost.client.files.FilesController
import com.github.mitallast.ghost.client.files.FilesDropController
import com.github.mitallast.ghost.client.messages.MessageEditorView
import com.github.mitallast.ghost.client.messages.MessageView
import com.github.mitallast.ghost.client.messages.MessagesListView
import com.github.mitallast.ghost.client.messages.MessagesStore
import com.github.mitallast.ghost.client.view.*
import com.github.mitallast.ghost.common.codec.Codec
import com.github.mitallast.ghost.message.FileMessage
import com.github.mitallast.ghost.message.Message
import com.github.mitallast.ghost.message.MessageContent
import com.github.mitallast.ghost.profile.UserProfile
import org.khronos.webgl.Uint8Array
import org.w3c.files.File
import kotlin.js.Date

class ProfileDialogController(
    private val self: UserProfile,
    private var profile: UserProfile
) : DialogController {
    private val sidebarView = SidebarDialogsController.add(this)
    private val listView = MessagesListView()
    private val scrollView = ReverseScrollView(listView)
    private val editorView = MessageEditorView(this)
    private var last: Message? = null

    init {
        sidebarView.title(profile.fullname)
        launch { historyTop() }
    }

    private suspend fun historyTop() {
        // @todo optimize as single transaction
        MessagesStore.historyTop(profile.id, 100)
            .asReversed()
            .forEach { show(it) }
    }

    override fun open() {
        FilesDropController.handle(this)
        ContentHeaderController.title(profile.fullname)
        ContentMainController.view(scrollView)
        ContentFooterController.view(editorView)
    }

    override suspend fun send(content: MessageContent) {
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
        E2EController.send(profile.id, message)
    }

    override suspend fun send(file: File) {
        val encrypted = FilesController.upload(file)
        send(FileMessage(encrypted))
    }

    suspend fun incoming(message: Message) {
        MessagesStore.put(profile.id, message)
        show(message)
    }

    private fun show(message: Message) {
        val own = message.sender.contentEquals(self.id)
        val view = MessageView(message, own)
        listView.add(view)
        if (last == null || last!!.olderThan(message)) {
            last = message
            sidebarView.message(message)
        }
    }

    suspend fun update(updated: UserProfile) {
        require(updated.id.contentEquals(profile.id))
        profile = updated
        sidebarView.title(profile.fullname)
        if (ContentMainController.contains(scrollView)) {
            ContentHeaderController.title(profile.fullname)
        }
    }
}