package com.github.mitallast.ghost.client.messages

import com.github.mitallast.ghost.client.html.div
import com.github.mitallast.ghost.client.view.View

class MessagesListView : View {
    override val root = div {
        clazz("messages-list")
    }

    fun add(view: View) {
        root.append(view.root)
    }
}