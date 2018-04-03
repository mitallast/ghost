package com.github.mitallast.ghost.client.dialogs

import com.github.mitallast.ghost.client.common.launch
import com.github.mitallast.ghost.client.crypto.HEX
import com.github.mitallast.ghost.client.html.div
import com.github.mitallast.ghost.client.html.text
import com.github.mitallast.ghost.client.messages.MessagesFlow

object DialogsView {
    val root = div {
        attr("class", "sidebar-list-scroll")
    }

    private val dialogs = ArrayList<DialogView>()

    fun clear() {
        dialogs.forEach { it.remove() }
        dialogs.clear()
    }

    fun addDialog(auth: ByteArray) {
        val view = DialogView(auth)
        dialogs.add(view)
        root.append(view.root)
    }
}

class DialogView(val auth: ByteArray) {
    private val name = HEX.toHex(auth)

    private val nameText = text(name)
    private val letterText = text(name.substring(0, 1))
    private val messageText = text("new dialog")
    private val dateText = text("00:00")

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
                    h4 { append(nameText) }
                }
                div {
                    attr("class", "dialog-message")
                    append(messageText)
                }
            }
        }
        onclick {
            launch {
                DialogsFlow.open(auth)
            }
        }
    }

    fun remove() {
        root.remove()
    }
}