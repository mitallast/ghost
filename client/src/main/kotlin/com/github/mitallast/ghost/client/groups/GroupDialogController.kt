package com.github.mitallast.ghost.client.groups

import com.github.mitallast.ghost.client.common.await
import com.github.mitallast.ghost.client.common.launch
import com.github.mitallast.ghost.client.common.toArrayBuffer
import com.github.mitallast.ghost.client.common.toByteArray
import com.github.mitallast.ghost.client.crypto.AES
import com.github.mitallast.ghost.client.crypto.HEX
import com.github.mitallast.ghost.client.crypto.crypto
import com.github.mitallast.ghost.client.dialog.DialogController
import com.github.mitallast.ghost.client.dialog.SidebarDialogsController
import com.github.mitallast.ghost.client.e2e.E2EController
import com.github.mitallast.ghost.client.files.FilesController
import com.github.mitallast.ghost.client.files.FilesDropController
import com.github.mitallast.ghost.client.html.div
import com.github.mitallast.ghost.client.messages.MessageEditorView
import com.github.mitallast.ghost.client.messages.MessageView
import com.github.mitallast.ghost.client.messages.MessagesListView
import com.github.mitallast.ghost.client.messages.MessagesStore
import com.github.mitallast.ghost.client.profile.ProfileListView
import com.github.mitallast.ghost.client.profile.ProfileView
import com.github.mitallast.ghost.client.profile.ProfilesController
import com.github.mitallast.ghost.client.updates.UpdatesController
import com.github.mitallast.ghost.client.view.*
import com.github.mitallast.ghost.common.codec.Codec
import com.github.mitallast.ghost.common.codec.CodecMessage
import com.github.mitallast.ghost.groups.GroupEncrypted
import com.github.mitallast.ghost.groups.GroupJoin
import com.github.mitallast.ghost.groups.GroupJoined
import com.github.mitallast.ghost.groups.GroupLeaved
import com.github.mitallast.ghost.message.FileMessage
import com.github.mitallast.ghost.message.Message
import com.github.mitallast.ghost.message.MessageContent
import com.github.mitallast.ghost.profile.UserProfile
import org.khronos.webgl.Uint8Array
import org.w3c.files.File
import kotlin.js.Date

class GroupDialogController(
    private val self: UserProfile,
    private val group: Group
) : DialogController {
    private val sidebarView = SidebarDialogsController.add(this)
    private val listView = MessagesListView()
    private val scrollView = ReverseScrollView(listView)
    private val editorView = MessageEditorView(this)
    private var last: Message? = null

    init {
        sidebarView.title(group.title)
        FilesDropController.handle(this)
        launch { historyTop() }
    }

    private suspend fun historyTop() {
        // @todo optimize as single transaction
        MessagesStore.historyTop(group.address, 100)
            .asReversed()
            .forEach { show(it) }
    }

    override fun open() {
        FilesDropController.handle(this)
        ContentHeaderController.action(group.title, { launch { info() } })
        ContentMainController.view(scrollView)
        ContentFooterController.view(editorView)
    }

    suspend fun incoming(from: ByteArray, encrypted: GroupEncrypted) {
        console.log("group incoming", HEX.toHex(from), encrypted)
        val decrypted = AES.decrypt(
            group.secretKey,
            toArrayBuffer(encrypted.encrypted),
            Uint8Array(toArrayBuffer(encrypted.iv))
        ).await()
        val update = Codec.anyCodec<CodecMessage>().read(toByteArray(decrypted))
        when (update) {
            is Message -> {
                MessagesStore.put(group.address, update)
                show(update)
            }
            is GroupJoined -> {
                GroupStore.putMember(group.address, update.member)
            }
            is GroupLeaved -> {
                GroupStore.removeMember(group.address, from)
            }
        }
    }

    override suspend fun send(file: File) {
        val encrypted = FilesController.upload(file)
        send(FileMessage(encrypted))
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
        MessagesStore.put(group.address, message)
        show(message)

        broadcast(message)
    }

    private suspend fun broadcast(message: CodecMessage) {
        val members = GroupStore.members(group.address)
        broadcast(message, members)
    }

    private suspend fun broadcast(message: CodecMessage, members: List<UserProfile>) {
        val data = Codec.anyCodec<CodecMessage>().write(message)
        val (encrypted, iv) = AES.encrypt(group.secretKey, toArrayBuffer(data)).await()
        val groupEncrypted = GroupEncrypted(
            group = group.address,
            iv = toByteArray(iv),
            encrypted = toByteArray(encrypted)
        )
        for (member in members) {
            console.log("send group to ", HEX.toHex(member.id))
            UpdatesController.send(member.id, groupEncrypted)
        }
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

    suspend fun addMember() {
        console.log("start create group")
        val contacts = ProfilesController.contacts()

        ContentHeaderController.title("Add member to group")
        ContentMainController.view(AddMemberView(this, contacts))
        ContentFooterController.hide()
    }

    suspend fun addMember(profile: UserProfile) {
        val members = GroupStore.members(group.address)
        GroupStore.putMember(group.address, profile)
        val secret = AES.exportKey(group.secretKey).await()
        val full = ArrayList(members)
        full.add(self)
        val join = GroupJoin(
            group = group.address,
            title = group.title,
            avatar = null,
            members = full,
            secret = toByteArray(secret)
        )
        E2EController.send(profile.id, join)
        val joined = GroupJoined(group.address, profile)
        broadcast(joined, members)
    }

    private suspend fun info() {
        val members = GroupStore.members(group.address)

        ContentHeaderController.action(group.title, { launch { open() } })
        ContentMainController.view(GroupInfoView(this, members))
        ContentFooterController.hide()
    }
}

class AddMemberView(
    private val controller: GroupDialogController,
    contacts: List<UserProfile>
) : View {

    private val listView = ProfileListView()
    private val scrollView = ScrollView(listView)

    init {
        for (contact in contacts) {
            listView.add(ProfileView(contact, { view, profile ->
                launch {
                    controller.addMember(profile)
                    listView.remove(view)
                }
            }))
        }
    }

    override val root = div {
        clazz("form-container")
        append(scrollView.root)
    }
}

class GroupInfoView(
    private val controller: GroupDialogController,
    members: List<UserProfile>
) : View {
    private val listView = ProfileListView()
    private val scrollView = ScrollView(listView)

    init {
        for (member in members) {
            listView.add(ProfileView(member, { _, _ -> }))
        }
    }

    override val root = div {
        clazz("form-container")
        div {
            clazz("form-actions")
            button {
                clazz("btn")
                text("Add member")
                onclick { launch { controller.addMember() } }
            }
        }
        append(scrollView.root)
    }
}