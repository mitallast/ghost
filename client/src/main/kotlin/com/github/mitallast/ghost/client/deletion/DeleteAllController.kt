package com.github.mitallast.ghost.client.deletion

import com.github.mitallast.ghost.client.common.launch
import com.github.mitallast.ghost.client.e2e.E2EAuthStore
import com.github.mitallast.ghost.client.ecdh.ECDHAuthStore
import com.github.mitallast.ghost.client.html.div
import com.github.mitallast.ghost.client.messages.MessagesStore
import com.github.mitallast.ghost.client.profile.ProfileStore
import com.github.mitallast.ghost.client.updates.UpdatesStore
import com.github.mitallast.ghost.client.view.ContentFooterController
import com.github.mitallast.ghost.client.view.ContentHeaderView
import com.github.mitallast.ghost.client.view.ContentMainController
import com.github.mitallast.ghost.client.view.View
import kotlin.browser.window

object DeleteAllController {
    fun view() {
        ContentHeaderView.setTitle("Delete profile")
        ContentMainController.view(DeleteAllView)
        ContentFooterController.hide()
    }

    fun deleteAll() {
        launch {
            E2EAuthStore.cleanup()
            ECDHAuthStore.cleanup()
            MessagesStore.cleanup()
            ProfileStore.cleanup()
            UpdatesStore.cleanup()
            window.location.reload()
        }
    }
}

object DeleteAllView : View {
    override val root = com.github.mitallast.ghost.client.html.div {
        attr("class", "form-container")
        form {
            div {
                attr("class", "form-hint")
                text("All your data will be removed!")
            }
            div {
                attr("class", "form-button")
                button {
                    type("submit")
                    text("REMOVE")
                }
            }
            onsubmit {
                it.preventDefault()
                DeleteAllController.deleteAll()
            }
        }
    }
}

object SidebarDeleteAllMenuItem : View {
    override val root = div {
        attr("class", "sidebar-menu-item")
        span {
            attr("class", "sidebar-menu-text")
            text("Delete profile")
        }
        span {
            attr("class", "sidebar-menu-icon")
            svg {
                attr("viewBox", "0 0 8 13")
                polygon {
                    attr("fill", "#ccc")
                    attr("fill-rule", "evenodd")
                    attr("points", "337 677.5 338.5 676 345 682.5 338.5 689 337 687.5 342 682.5")
                    attr("transform", "translate(-337 -676)")
                }
            }
        }
        onclick { DeleteAllController.view() }
    }
}