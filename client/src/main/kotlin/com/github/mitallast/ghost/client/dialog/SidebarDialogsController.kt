package com.github.mitallast.ghost.client.dialog

import com.github.mitallast.ghost.client.common.launch
import com.github.mitallast.ghost.client.html.div
import com.github.mitallast.ghost.client.html.text
import com.github.mitallast.ghost.client.view.ScrollView
import com.github.mitallast.ghost.client.view.SidebarController
import com.github.mitallast.ghost.client.view.View
import com.github.mitallast.ghost.message.FileMessage
import com.github.mitallast.ghost.message.Message
import com.github.mitallast.ghost.message.ServiceMessage
import com.github.mitallast.ghost.message.TextMessage
import kotlin.js.Date

object SidebarDialogsController {
    private val scrollView = ScrollView(SidebarDialogsView)

    fun show() {
        console.log("show dialogs")
        SidebarController.view(scrollView)
    }

    fun add(controller: SidebarDialogController): SidebarDialogView {
        val view = SidebarDialogView(controller)
        SidebarDialogsView.add(view)
        return view
    }

    fun remove(view: SidebarDialogView) {
        SidebarDialogsView.remove(view)
    }
}

object SidebarDialogsView : View {
    override val root = div {
        clazz("dialogs")
    }

    fun add(view: SidebarDialogView) {
        root.append(view.root)
    }

    fun remove(view: SidebarDialogView) {
        root.remove(view.root)
    }
}

interface SidebarDialogController {
    fun open()
}

class SidebarDialogView(private val controller: SidebarDialogController) {
    private val dateText = text("")
    private val messageText = text("")
    private val fullnameText = text("")
    private val letterText = text("")

    fun title(title: String) {
        fullnameText.text(title)
        letterText.text(title.substring(0, 1))
    }

    fun message(message: Message) {
        dateText.text(timeFormat(message.date))
        val content = message.content
        when (content) {
            is TextMessage -> messageText.text(content.text.substring(0, 32))
            is ServiceMessage -> messageText.text(content.text.substring(0, 32))
            is FileMessage -> messageText.text(content.file.name.substring(0, 32))
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
                controller.open()
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