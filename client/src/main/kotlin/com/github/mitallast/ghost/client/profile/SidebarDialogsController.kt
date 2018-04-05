package com.github.mitallast.ghost.client.profile

import com.github.mitallast.ghost.client.common.launch
import com.github.mitallast.ghost.client.crypto.HEX
import com.github.mitallast.ghost.client.html.div
import com.github.mitallast.ghost.client.html.text
import com.github.mitallast.ghost.client.messages.MessagesController
import com.github.mitallast.ghost.client.view.SidebarController
import com.github.mitallast.ghost.client.view.View
import com.github.mitallast.ghost.message.Message
import com.github.mitallast.ghost.message.ServiceMessage
import com.github.mitallast.ghost.message.TextMessage
import com.github.mitallast.ghost.profile.UserProfile
import kotlin.js.Date

object SidebarDialogsController {
    private val dialogsMap = HashMap<String, SidebarDialogView>()

    fun show() {
        console.log("show dialogs")
        SidebarController.view(SidebarDialogsView)
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
    private val list = div {
        attr("class", "sidebar-list-scroll")
    }

    override val root = div {
        attr("class", "sidebar-list")
        append(list)
    }

    fun add(view: SidebarDialogView) {
        list.append(view.root)
    }
}

class SidebarDialogView(profile: UserProfile) {
    private val id = profile.id
    private val dateText = text("00:00")
    private val messageText = text("...")
    private val fullnameText = text(profile.fullname)
    private val letterText = text(profile.fullname.substring(0, 1))

    fun update(profile: UserProfile) {
        fullnameText.text(profile.fullname)
        letterText.text(profile.fullname.substring(0, 1))
    }

    fun update(message: Message) {
        console.log("last ${timeFormat(message.date)}:${message.randomId}")
        dateText.text(timeFormat(message.date))
        val content = message.content
        when (content) {
            is TextMessage -> messageText.text(content.text.substring(0, 32))
            is ServiceMessage -> messageText.text(content.text.substring(0, 32))
            else -> messageText.text("...")
        }
    }

    val root = div {
        attr("class", "dialog-container")
        div {
            attr("class", "dialog")
            div {
                attr("class", "avatar-container")
                div {
                    attr("class", "avatar-placeholder")
                    append(letterText)
                }
            }
            div {
                attr("class", "dialog-main")
                div {
                    attr("class", "dialog-header")
                    span { append(dateText) }
                    h4 { append(fullnameText) }
                }
                div {
                    attr("class", "dialog-message")
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