package com.github.mitallast.ghost.client.profile

import com.github.mitallast.ghost.client.common.launch
import com.github.mitallast.ghost.client.common.toByteArray
import com.github.mitallast.ghost.client.crypto.HEX
import com.github.mitallast.ghost.client.e2e.E2EFlow
import com.github.mitallast.ghost.client.html.div
import com.github.mitallast.ghost.client.html.input
import com.github.mitallast.ghost.client.view.ContentFooterController
import com.github.mitallast.ghost.client.view.ContentHeaderView
import com.github.mitallast.ghost.client.view.ContentMainController
import com.github.mitallast.ghost.client.view.View

object AddDialogController {
    fun start() {
        console.log("start add dialog")
        ContentHeaderView.setTitle("Add new dialog")
        ContentMainController.view(AddDialogView())
        ContentFooterController.hide()
    }

    fun add(address: ByteArray) {
        ContentMainController.view(DialogRequestSentView(address))
        launch { E2EFlow.connect(address) }
    }
}

class AddDialogView : View {
    private val addressInput = input {
        type("text")
        name("address")
        focus()
    }
    override val root = div {
        attr("class", "form-container")
        form {
            div {
                attr("class", "form-input")
                append(addressInput)
            }
            div {
                attr("class", "form-hint")
                text("Enter address in HEX format (32 symbols or 16 bytes).")
            }
            div {
                attr("class", "form-button")
                button {
                    attr("class", "btn")
                    type("submit")
                    text("OK")
                }
            }
            onsubmit {
                it.preventDefault()
                val value = addressInput.value()
                if (value.isEmpty()) {
                    addressInput.focus()
                } else if (!value.matches("^[0-9abcdefABCDEF]{32}$")) {
                    addressInput.focus()
                } else {
                    val address = toByteArray(HEX.parseHex(value).buffer)
                    AddDialogController.add(address)
                }
            }
        }
    }
}

class DialogRequestSentView(address: ByteArray) : View {
    override val root = div {
        attr("class", "form-container")
        div {
            attr("class", "form-success")
            text("Request sent, wait for response!")
        }
        div {
            attr("class", "form-hint")
            text(HEX.toHex(address))
        }
    }
}

object SidebarAddDialogMenuView : View {
    override val root = div {
        attr("class", "sidebar-menu-item")
        span {
            attr("class", "sidebar-menu-text")
            text("Add new dialog")
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
        onclick { AddDialogController.start() }
    }
}