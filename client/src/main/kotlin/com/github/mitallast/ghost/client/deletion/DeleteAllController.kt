package com.github.mitallast.ghost.client.deletion

import com.github.mitallast.ghost.client.common.launch
import com.github.mitallast.ghost.client.e2e.E2EAuthStore
import com.github.mitallast.ghost.client.e2e.E2EIncomingRequestStore
import com.github.mitallast.ghost.client.e2e.E2EOutgoingRequestStore
import com.github.mitallast.ghost.client.e2e.E2EResponseStore
import com.github.mitallast.ghost.client.ecdh.ECDHAuthStore
import com.github.mitallast.ghost.client.groups.GroupStore
import com.github.mitallast.ghost.client.html.div
import com.github.mitallast.ghost.client.messages.MessagesStore
import com.github.mitallast.ghost.client.profile.ProfileStore
import com.github.mitallast.ghost.client.updates.UpdatesStore
import com.github.mitallast.ghost.client.view.*
import kotlin.browser.window

object DeleteAllController {
    fun view() {
        ContentHeaderController.title("Delete profile")
        ContentMainController.view(DeleteAllView)
        ContentFooterController.hide()
    }

    fun deleteAll() {
        launch {
            E2EAuthStore.cleanup()
            E2EOutgoingRequestStore.cleanup()
            E2EIncomingRequestStore.cleanup()
            E2EResponseStore.cleanup()
            ECDHAuthStore.cleanup()
            MessagesStore.cleanup()
            ProfileStore.cleanup()
            UpdatesStore.cleanup()
            GroupStore.cleanup()
            window.location.reload()
        }
    }
}

object DeleteAllView : View {
    override val root = com.github.mitallast.ghost.client.html.div {
        clazz("form-container")
        form {
            div {
                clazz("form-hint")
                text("All your data will be removed!")
            }
            div {
                clazz("form-button")
                button {
                    clazz("btn")
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
        clazz("sidebar-menu-item")
        span {
            clazz("sidebar-menu-text")
            text("Delete profile")
        }
        span {
            clazz("sidebar-menu-icon")
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