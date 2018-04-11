package com.github.mitallast.ghost.client.messages

import com.github.mitallast.ghost.client.common.launch
import com.github.mitallast.ghost.client.common.toByteArray
import com.github.mitallast.ghost.client.crypto.HEX
import com.github.mitallast.ghost.client.crypto.crypto
import com.github.mitallast.ghost.client.e2e.E2EController
import com.github.mitallast.ghost.client.e2e.E2EDHFlow
import com.github.mitallast.ghost.client.files.await
import com.github.mitallast.ghost.client.files.FilesDropController
import com.github.mitallast.ghost.client.files.FilesDropHandler
import com.github.mitallast.ghost.client.html.div
import com.github.mitallast.ghost.client.profile.ProfileController
import com.github.mitallast.ghost.client.profile.SidebarDialogsController
import com.github.mitallast.ghost.client.view.*
import com.github.mitallast.ghost.common.codec.Codec
import com.github.mitallast.ghost.message.EncryptedFileMessage
import com.github.mitallast.ghost.message.Message
import com.github.mitallast.ghost.message.MessageContent
import com.github.mitallast.ghost.message.TextMessage
import com.github.mitallast.ghost.profile.UserProfile
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Uint8Array
import org.w3c.dom.url.URL
import org.w3c.files.Blob
import org.w3c.files.BlobPropertyBag
import org.w3c.files.File
import org.w3c.files.FileReader
import org.w3c.xhr.ARRAYBUFFER
import org.w3c.xhr.XMLHttpRequest
import org.w3c.xhr.XMLHttpRequestResponseType
import kotlin.js.Date
import kotlin.js.Promise

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

class MessagesListController(private val self: UserProfile, private val profile: UserProfile) : FilesDropHandler {
    private val listView = MessagesListView()

    private val editorView = MessageEditorView(this)
    private var last: Message? = null

    init {
        FilesDropController.handle(this)
        launch { historyTop() }
    }

    private suspend fun historyTop() {
        // @todo optimize as single transaction
        MessagesStore.historyTop(profile.id, 100)
            .asReversed()
            .forEach { show(it) }
    }

    fun show() {
        ContentHeaderView.setTitle(profile.fullname)
        ContentMainController.view(ReverseScrollView(listView))
        ContentFooterController.view(editorView)
    }

    override fun send(file: File) {
        launch {
            console.log("send e2e file")
            val reader = FileReader()
            reader.readAsArrayBuffer(file)
            val buffer = reader.await<ArrayBuffer>()
            val encrypted = E2EDHFlow.encrypt(self.id, profile.id, buffer)
            val xhr = XMLHttpRequest()
            xhr.open("POST", "/file/upload", true)
            xhr.onload = {
                if (xhr.status.toInt() == 200) {
                    val address = xhr.responseText
                    console.log("uploaded address", address)
                    val fileMeta = EncryptedFileMessage(
                        file.name,
                        file.size,
                        file.type,
                        address,
                        encrypted.sign,
                        encrypted.iv
                    )
                    launch { send(fileMeta) }
                } else {
                    console.error("upload error", it, xhr)
                }
            }
            xhr.send(encrypted.encrypted)
        }
    }

    fun download(message: EncryptedFileMessage, own: Boolean): Promise<Blob> {
        return Promise({ resolve, reject ->
            console.log("download e2e file")
            val xhr = XMLHttpRequest()
            xhr.responseType = XMLHttpRequestResponseType.ARRAYBUFFER
            xhr.open("GET", "/file/${message.address}", true)
            xhr.onload = {
                if (xhr.status.toInt() == 200) {
                    val buffer = xhr.response as ArrayBuffer
                    launch {
                        val decrypted = E2EDHFlow.decrypt(profile.id, self.id, message.sign, message.iv, buffer, own)
                        val blob = Blob(arrayOf(decrypted), BlobPropertyBag(type = message.mimetype))
                        console.info("success download")
                        resolve.invoke(blob)
                    }
                } else {
                    console.error("download error", it, xhr)
                    reject.invoke(RuntimeException("response error"))
                }
            }
            xhr.onerror = { it ->
                console.log("xhr error", it)
                reject.invoke(RuntimeException("xhr error"))
            }
            xhr.send()
        })
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
        E2EController.send(profile.id, message)
    }

    suspend fun handle(message: Message) {
        MessagesStore.put(profile.id, message)
        show(message)
    }

    private fun show(message: Message) {
        val own = !message.sender.contentEquals(profile.id)
        val view = MessageView(this, message, own)
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
    override val root = div {
        clazz("messages-list")
    }

    fun add(view: View) {
        root.append(view.root)
    }
}

class MessageView(
    controller: MessagesListController,
    message: Message,
    own: Boolean
) : View {
    override val root = div {
        if (own) {
            clazz("message-container", "message-own")
        } else {
            clazz("message-container")
        }
        val content = message.content
        when (content) {
            is TextMessage -> div {
                clazz("message")
                div {
                    clazz("message-text")
                    text(content.text)
                }
                div {
                    clazz("message-time")
                    text(timeFormat(message.date))
                }
            }
            is EncryptedFileMessage -> div {
                clazz("message")
                div {
                    clazz("message-image")
                    img {
                        controller.download(content, own).then {
                            onload {
                                URL.revokeObjectURL(src)
                            }
                            src = URL.createObjectURL(it)
                        }
                    }
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