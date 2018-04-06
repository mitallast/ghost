package com.github.mitallast.ghost.client.e2e

import com.github.mitallast.ghost.client.common.launch
import com.github.mitallast.ghost.client.crypto.HEX
import com.github.mitallast.ghost.client.html.div
import com.github.mitallast.ghost.client.view.*

object PendingRequestsController {
    suspend fun view() {
        console.log("load all requests")
        val requests = E2EAuthStore.loadRequests()
        console.log("render")
        val view = PendingRequestsView(requests)
        console.log("view")
        ContentHeaderView.setTitle("Pending requests")
        ContentMainController.view(view)
        ContentFooterController.hide()
    }
}

class PendingRequestsView(private val requests: List<ByteArray>) : View {
    override val root = div {
        attr("class", "requests-list")
        div {
            attr("class", "requests-scroll")
            for (request in requests) {
                console.log("render request")
                div {
                    attr("class", "request-container")
                    h4 { text(HEX.toHex(request)) }
                    button {
                        attr("class", "btn btn-sm")
                        text("Remove")
                    }
                }
            }
        }
    }
}

object SidebarPendingRequestsMenuView : View {
    override val root = div {
        attr("class", "sidebar-menu-item")
        span {
            attr("class", "sidebar-menu-text")
            text("Pending requests")
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
        onclick { launch { PendingRequestsController.view() } }
    }
}