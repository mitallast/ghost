package com.github.mitallast.ghost.client.profile

import com.github.mitallast.ghost.client.common.launch
import com.github.mitallast.ghost.client.crypto.HEX
import com.github.mitallast.ghost.client.html.div
import com.github.mitallast.ghost.client.html.text
import com.github.mitallast.ghost.client.messages.MessagesController
import com.github.mitallast.ghost.client.view.ScrollView
import com.github.mitallast.ghost.client.view.SidebarController
import com.github.mitallast.ghost.client.view.View
import com.github.mitallast.ghost.message.EncryptedFileMessage
import com.github.mitallast.ghost.message.Message
import com.github.mitallast.ghost.message.ServiceMessage
import com.github.mitallast.ghost.message.TextMessage
import com.github.mitallast.ghost.profile.UserProfile
import kotlin.js.Date

object SidebarDialogsController {
    private val dialogsMap = HashMap<String, SidebarDialogView>()

    fun show() {
        console.log("show dialogs")
        SidebarController.view(ScrollView(SidebarDialogsView))
    }

    suspend fun update(profile: UserProfile) {
        val key = HEX.toHex(profile.id)
        val old: SidebarDialogView? = dialogsMap[key]
        if (old == null) {
            val view = SidebarDialogView(profile)
            val last = MessagesController.lastMessage(profile.id)
            if (last != null) {
                view.update(last)
            }
            dialogsMap[key] = view
            SidebarDialogsView.add(view)
        } else {
            old.update(profile)
        }
    }

    fun update(id: ByteArray, message: Message) {
        val key = HEX.toHex(id)
        dialogsMap[key]?.update(message)
    }
}

object SidebarDialogsView : View {
    override val root = div {
        clazz("dialogs")
    }

    fun add(view: SidebarDialogView) {
        root.append(view.root)
    }
}

class SidebarDialogView(profile: UserProfile) {
    private val dateText = text("00:00")
    private val messageText = text("...")
    private val fullnameText = text(profile.fullname)
    private val letterText = text(profile.fullname.substring(0, 1))

    fun update(profile: UserProfile) {
        fullnameText.text(profile.fullname)
        letterText.text(profile.fullname.substring(0, 1))
    }

    fun update(message: Message) {
        dateText.text(timeFormat(message.date))
        val content = message.content
        when (content) {
            is TextMessage -> messageText.text(content.text.substring(0, 32))
            is ServiceMessage -> messageText.text(content.text.substring(0, 32))
            is EncryptedFileMessage -> messageText.text(content.name.substring(0, 32))
            else -> messageText.text("...")
        }
    }

    val root = div {
        clazz("dialog-container")
        div {
            clazz("dialog")
            div {
                clazz("avatar-container")
                div {
                    clazz("avatar-placeholder")
                    append(letterText)
                }
            }
            div {
                clazz("dialog-main")
                div {
                    clazz("dialog-header")
                    span { append(dateText) }
                    h4 { append(fullnameText) }
                }
                div {
                    clazz("dialog-message")
                    append(messageText)
                }
            }
        }
        onclick {
            launch {
                MessagesController.open(profile)
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