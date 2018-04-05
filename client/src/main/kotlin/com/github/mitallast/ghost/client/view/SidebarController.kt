package com.github.mitallast.ghost.client.view

import com.github.mitallast.ghost.client.common.launch
import com.github.mitallast.ghost.client.html.a
import com.github.mitallast.ghost.client.profile.SidebarDialogsController
import com.github.mitallast.ghost.client.html.div

object SidebarController {
    private var last: View? = null

    fun view(view: View) {
        hide()
        last = view
        SidebarView.view(view)
    }

    fun hide() {
        if (last != null) {
            SidebarView.remove(last!!)
            last = null
        }
    }

    private var state = "init"
    fun toggle() {
        launch {
            when (state) {
                "init" -> {
                    state = "settings"
                    SidebarSettingsController.show()
                }
                "dialogs" -> {
                    state = "settings"
                    SidebarSettingsController.show()
                }
                "settings" -> {
                    state = "dialogs"
                    SidebarDialogsController.show()
                }
            }
        }
    }
}

object SidebarView {
    private val menu = a {
        attr("class", "sidebar-menu-button")
        attr("title", "Show menu")
        svg {
            attr("viewBox", "0 0 16 14")
            g {
                attr("fill", "#000")
                attr("fill-fule", "#evenodd")
                rect {
                    attr("width", "16")
                    attr("height", "2")
                    attr("rx", "1")
                }
                rect {
                    attr("width", "16")
                    attr("height", "2")
                    attr("y", "6")
                    attr("rx", "1")
                }
                rect {
                    attr("width", "16")
                    attr("height", "2")
                    attr("y", "12")
                    attr("rx", "1")
                }
            }
        }

        hide()

        onclick { SidebarController.toggle() }
    }
    val root = div {
        attr("class", "ghost-sidebar")
        div {
            attr("class", "sidebar-header")
            div {
                attr("class", "sidebar-header-content")
                append(menu)
                h2 {
                    attr("class", "sidebar-header-logo")
                    text("Ghost messenger")
                }
            }
        }
    }

    fun remove(view: View) {
        menu.hide()
        root.remove(view.root)
    }

    fun view(view: View) {
        menu.show()
        root.append(view.root)
    }
}