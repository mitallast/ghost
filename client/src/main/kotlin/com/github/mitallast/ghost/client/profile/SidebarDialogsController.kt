package com.github.mitallast.ghost.client.profile

import com.github.mitallast.ghost.client.common.launch
import com.github.mitallast.ghost.client.crypto.HEX
import com.github.mitallast.ghost.client.html.div
import com.github.mitallast.ghost.client.html.text
import com.github.mitallast.ghost.client.messages.MessagesController
import com.github.mitallast.ghost.client.view.SidebarController
import com.github.mitallast.ghost.client.view.View
import com.github.mitallast.ghost.profile.UserProfile

object SidebarDialogsController {
    private val dialogsMap = HashMap<String, SidebarDialogView>()

    fun show() {
        console.log("show dialogs")
        SidebarController.view(SidebarDialogsView)
    }

    fun update(profile: UserProfile) {
        val key = HEX.toHex(profile.id)
        val old: SidebarDialogView? = dialogsMap[key]
        if (old == null) {
            val view = SidebarDialogView(profile)
            dialogsMap[key] = view
            SidebarDialogsView.add(view)
        } else {
            old.update(profile)
        }
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
    private val dateText = text("00:00")
    private val messageText = text("...")
    private val fullnameText = text(profile.fullname)
    private val letterText = text(profile.fullname.substring(0, 1))

    fun update(profile: UserProfile) {
        fullnameText.text(profile.fullname)
        letterText.text(profile.fullname.substring(0, 1))
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
}