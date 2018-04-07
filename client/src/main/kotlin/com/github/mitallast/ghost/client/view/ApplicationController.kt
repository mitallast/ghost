package com.github.mitallast.ghost.client.view

import com.github.mitallast.ghost.client.common.launch
import com.github.mitallast.ghost.client.ecdh.ECDHController
import com.github.mitallast.ghost.client.html.div

object ApplicationController {
    fun start() {
        ApplicationView.root.appendToBody()
        launch { ECDHController.start() }
    }
}

object ApplicationView : View {
    override val root = div {
        clazz("ghost")
        div {
            clazz("ghost-container")
            append(SidebarView.root)
            div {
                clazz("ghost-main")
                div {
                    clazz("content-container")
                    append(ContentHeaderView.root)
                    append(ContentMainView.root)
                    append(ContentFooterView.root)
                }
            }
        }
    }
}