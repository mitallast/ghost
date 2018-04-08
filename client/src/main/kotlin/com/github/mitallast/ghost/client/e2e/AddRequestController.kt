package com.github.mitallast.ghost.client.e2e

import com.github.mitallast.ghost.client.common.launch
import com.github.mitallast.ghost.client.common.toByteArray
import com.github.mitallast.ghost.client.crypto.HEX
import com.github.mitallast.ghost.client.ecdh.ECDHController
import com.github.mitallast.ghost.client.html.div
import com.github.mitallast.ghost.client.html.input
import com.github.mitallast.ghost.client.html.text
import com.github.mitallast.ghost.client.updates.UpdatesController
import com.github.mitallast.ghost.client.view.ContentFooterController
import com.github.mitallast.ghost.client.view.ContentHeaderView
import com.github.mitallast.ghost.client.view.ContentMainController
import com.github.mitallast.ghost.client.view.View

object AddRequestController {
    fun start() {
        console.log("start add dialog")
        ContentHeaderView.setTitle("Add new dialog")
        ContentMainController.view(AddDialogView())
        ContentFooterController.hide()
    }

    suspend fun add(to: ByteArray): String? {
        return when {
            isSelf(to) -> "it's your address"
            existsAuth(to) -> "address already exists"
            existsRequest(to) -> "request already exists"
            else -> {
                val auth = ECDHController.auth()
                val request = E2EDHFlow.request(auth, to)
                UpdatesController.send(to, request)
                ContentMainController.view(DialogRequestSentView(to))
                null
            }
        }
    }

    private suspend fun isSelf(address: ByteArray): Boolean {
        val self = ECDHController.auth()
        return self.contentEquals(address)
    }

    private suspend fun existsRequest(address: ByteArray): Boolean {
        return E2EOutgoingRequestStore.load(address) != null
    }

    private suspend fun existsAuth(address: ByteArray): Boolean {
        return E2EAuthStore.load(address) != null
    }
}

class AddDialogView : View {
    private val errorMessageText = text("")

    private val errorMessage = div {
        clazz("error-message")
        hide()
        append(errorMessageText)
    }

    private val addressInput = input {
        type("text")
        name("address")
        focus()
        on("changed", { errorMessage.hide() })
    }

    override val root = div {
        clazz("form-container")
        form {
            div {
                clazz("form-input")
                append(addressInput)
                append(errorMessage)
            }
            div {
                clazz("form-hint")
                text("Enter address in HEX format (32 symbols or 16 bytes).")
            }
            div {
                clazz("form-button")
                button {
                    clazz("btn")
                    type("submit")
                    text("OK")
                }
            }
            onsubmit {
                launch {
                    it.preventDefault()
                    val value = addressInput.value()
                    if (value.isEmpty()) {
                        addressInput.focus()
                        error("address is required")
                    } else if (!value.matches("^[0-9abcdefABCDEF]{32}$")) {
                        addressInput.focus()
                        error("not valid address")
                    } else {
                        val address = toByteArray(HEX.parseHex(value).buffer)
                        val error = AddRequestController.add(address)
                        if(error != null) {
                            addressInput.focus()
                            error(error)
                        }
                    }
                }
            }
        }
    }

    private fun error(message: String) {
        errorMessage.show()
        errorMessageText.text(message)
    }
}

class DialogRequestSentView(address: ByteArray) : View {
    override val root = div {
        clazz("form-container")
        div {
            clazz("form-success")
            text("Request sent, wait for response!")
        }
        div {
            clazz("form-hint")
            text(HEX.toHex(address))
        }
    }
}

object SidebarAddDialogMenuView : View {
    override val root = div {
        clazz("sidebar-menu-item")
        span {
            clazz("sidebar-menu-text")
            text("Add new dialog")
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
        onclick { AddRequestController.start() }
    }
}