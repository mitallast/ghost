package com.github.mitallast.ghost.client.e2e

import com.github.mitallast.ghost.client.common.launch
import com.github.mitallast.ghost.client.crypto.HEX
import com.github.mitallast.ghost.client.ecdh.ECDHController
import com.github.mitallast.ghost.client.html.div
import com.github.mitallast.ghost.client.html.input
import com.github.mitallast.ghost.client.html.text
import com.github.mitallast.ghost.client.view.ContentFooterController
import com.github.mitallast.ghost.client.view.ContentHeaderView
import com.github.mitallast.ghost.client.view.ContentMainController
import com.github.mitallast.ghost.client.view.View
import com.github.mitallast.ghost.e2e.E2EAuthRequest

object IncomingRequestController {
    suspend fun handle(request: E2EAuthRequest) {
        console.log("e2e request received")
        if (E2EDHFlow.validateRequest(request)) {
            console.log("new auth request validated")
            start(request.from)
        } else {
            console.log("auth request not valid")
            // @todo send canceled
        }
    }

    fun start(address: ByteArray) {
        val view = IncomingRequestView(address)
        ContentHeaderView.setTitle("Incoming request: " + HEX.toHex(address))
        ContentMainController.view(view)
        ContentFooterController.hide()
    }

    suspend fun answer(address: ByteArray, password: String) {
        val response = E2EDHFlow.answerRequest(address, password)
        ECDHController.send(response)

        val view = IncomingRequestAnsweredView(address)

        ContentHeaderView.setTitle("Incoming request: " + HEX.toHex(address))
        ContentMainController.view(view)
        ContentFooterController.hide()
    }

    suspend fun listView() {
        val requests = E2EIncomingRequestStore.list()
        val view = IncomingRequestsView(requests)
        ContentHeaderView.setTitle("Incoming requests")
        ContentMainController.view(view)
        ContentFooterController.hide()
    }
}

class IncomingRequestView(private val address: ByteArray) : View {

    private val errorMessageText = text("")

    private val errorMessage = div {
        clazz("error-message")
        hide()
        append(errorMessageText)
    }

    private val passwordInput = input {
        type("password")
        name("address")
        autocomplete("false")
        focus()
        on("changed", { errorMessage.hide() })
    }

    override val root = div {
        clazz("form-container")
        form {
            div {
                clazz("form-input")
                append(passwordInput)
                append(errorMessage)
            }
            div {
                clazz("form-hint")
                text("Enter offline password, same as requested client should use.")
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
                    val value = passwordInput.value()
                    if (value.isEmpty()) {
                        passwordInput.focus()
                        error("password is required")
                    } else {
                        IncomingRequestController.answer(address, value)
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

class IncomingRequestAnsweredView(address: ByteArray) : View {
    override val root = div {
        clazz("form-container")
        div {
            clazz("form-success")
            text("Response sent, wait for complete!")
        }
        div {
            clazz("form-hint")
            text(HEX.toHex(address))
        }
    }
}

class IncomingRequestsView(private val requests: List<ByteArray>) : View {
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
                        text("Answer")
                        onclick { IncomingRequestController.start(request) }
                    }
                }
            }
        }
    }
}

object SidebarIncomingRequestsMenuView : View {
    override val root = div {
        clazz("sidebar-menu-item")
        span {
            clazz("sidebar-menu-text")
            text("Incoming requests")
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
        onclick { launch { IncomingRequestController.listView() } }
    }
}