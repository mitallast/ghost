package com.github.mitallast.ghost.client.e2e

import com.github.mitallast.ghost.client.common.launch
import com.github.mitallast.ghost.client.crypto.HEX
import com.github.mitallast.ghost.client.html.div
import com.github.mitallast.ghost.client.html.input
import com.github.mitallast.ghost.client.html.text
import com.github.mitallast.ghost.client.view.ContentFooterController
import com.github.mitallast.ghost.client.view.ContentHeaderView
import com.github.mitallast.ghost.client.view.ContentMainController
import com.github.mitallast.ghost.client.view.View
import com.github.mitallast.ghost.e2e.E2EAuthResponse

object ResponseController {
    suspend fun handle(response: E2EAuthResponse) {
        console.log("e2e response received")
        if (E2EDHFlow.validateResponse(response)) {
            start(response.from)
        } else {
            console.error("e2e response is invalid")
            // @todo send request canceled
        }
    }

    fun start(address: ByteArray) {
        val view = ResponseView(address)
        ContentHeaderView.setTitle("Response: " + HEX.toHex(address))
        ContentMainController.view(view)
        ContentFooterController.hide()
    }

    suspend fun complete(address: ByteArray, password: String) {
        E2EDHFlow.completeResponse(address, password)
        E2EController.complete(address)

        val view = ResponseCompleteView(address)
        ContentHeaderView.setTitle("Response: " + HEX.toHex(address))
        ContentMainController.view(view)
        ContentFooterController.hide()
    }

    suspend fun listView() {
        val responses = E2EResponseStore.list()
        val view = PendingResponsesView(responses)
        ContentHeaderView.setTitle("Pending responses")
        ContentMainController.view(view)
        ContentFooterController.hide()
    }
}

class ResponseView(private val address: ByteArray) : View {

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
                text("Enter offline password, same as requested client.")
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
                        ResponseController.complete(address, value)
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

class ResponseCompleteView(address: ByteArray) : View {
    override val root = div {
        clazz("form-container")
        div {
            clazz("form-success")
            text("Response complete!")
        }
        div {
            clazz("form-hint")
            text(HEX.toHex(address))
        }
    }
}

class PendingResponsesView(private val requests: List<ByteArray>) : View {
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
                        text("Complete")
                        onclick { ResponseController.start(request) }
                    }
                }
            }
        }
    }
}

object SidebarPendingResponsesMenuView : View {
    override val root = div {
        clazz("sidebar-menu-item")
        span {
            clazz("sidebar-menu-text")
            text("Pending responses")
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
        onclick { launch { ResponseController.listView() } }
    }
}