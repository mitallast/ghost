package com.github.mitallast.ghost.client.dialogs

import com.github.mitallast.ghost.client.common.launch
import com.github.mitallast.ghost.client.common.toByteArray
import com.github.mitallast.ghost.client.crypto.HEX
import com.github.mitallast.ghost.client.e2e.E2EFlow
import com.github.mitallast.ghost.client.messages.MessagesFlow
import org.w3c.dom.get
import kotlin.browser.window

object DialogsView {
    val dialogsContainer = window.document.getElementById("dialogs")!!
    val addDialogButton = window.document.getElementById("addDialog")!!

    private val dialogs = ArrayList<DialogView>()

    init {
        addDialogButton.addEventListener("click", {
            val hex = window.prompt("enter user id")
            if (hex != null) {
                launch {
                    val id = toByteArray(HEX.parseHex(hex).buffer)
                    E2EFlow.connect(id)
                }
            }
        })
    }

    fun clear() {
        dialogs.clear()
        val nodes = dialogsContainer.childNodes
        for (i in 0 until nodes.length) {
            dialogsContainer.removeChild(nodes[i]!!)
        }
    }

    fun addDialog(auth: ByteArray) {
        val dialog = DialogView(auth)
        dialogs.add(dialog)
        DialogsView.dialogsContainer.appendChild(dialog.container)
    }
}

class DialogView(val auth: ByteArray) {
    val container = window.document.createElement("div")
    val a = window.document.createElement("a")
    val aText = window.document.createTextNode(HEX.toHex(auth))

    init {
        a.setAttribute("href", "#" + HEX.toHex(auth))
        a.addEventListener("click", {
            launch {
                MessagesFlow.showHistory(auth)
            }
        })
        a.appendChild(aText)
        container.appendChild(a)
    }
}