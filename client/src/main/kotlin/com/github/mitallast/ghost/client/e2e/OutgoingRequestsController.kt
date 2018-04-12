package com.github.mitallast.ghost.client.e2e

import com.github.mitallast.ghost.client.common.launch
import com.github.mitallast.ghost.client.crypto.HEX
import com.github.mitallast.ghost.client.html.div
import com.github.mitallast.ghost.client.view.*

object OutgoingRequestsController {
    suspend fun view() {
        console.log("load all requests")
        val requests = E2EOutgoingRequestStore.list()
        console.log("render")
        val view = OutgoingRequestsView(requests)
        console.log("view")
        ContentHeaderView.setTitle("Outgoing requests")
        ContentMainController.view(view)
        ContentFooterController.hide()
    }
}

class OutgoingRequestsView(private val requests: List<ByteArray>) : View {
    override val root = div {
        clazz("requests-list")
        div {
            clazz("requests-scroll")
            for (request in requests) {
                console.log("render request")
                div {
                    clazz("request-container")
                    h4 { text(HEX.toHex(request)) }
                    button {
                        clazz("btn", "btn-sm")
                        text("Cancel")
                        onclick { E2EController.cancel(request) }
                    }
                }
            }
        }
    }
}

object SidebarOutgoingRequestsMenuView : View {
    override val root = div {
        clazz("sidebar-menu-item")
        span {
            clazz("sidebar-menu-text")
            text("Outgoing requests")
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
        onclick { launch { OutgoingRequestsController.view() } }
    }
}