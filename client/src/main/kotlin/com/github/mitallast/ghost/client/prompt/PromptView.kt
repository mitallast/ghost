package com.github.mitallast.ghost.client.prompt

import org.w3c.dom.events.EventListener
import kotlin.browser.window
import kotlin.js.Promise

object PromptView {
    val container = window.document.getElementById("prompt")!!
    val promptTitle = window.document.getElementById("promptTitle")!!
    val promptTitleText = window.document.createTextNode("")
    val promptInput = window.document.getElementById("promptInput")!!
    val promptButton = window.document.getElementById("promptButton")!!

    init {
        promptTitle.appendChild(promptTitleText)
    }

    private var inProgress: Boolean = false

    fun prompt(title: String): Promise<String> {
        if (inProgress) {
            return Promise.reject(IllegalStateException("already in prompt"))
        } else {
            inProgress = true
            container.asDynamic().style.display = "block"
            return Promise({ resolve, _ ->
                promptTitleText.replaceWith(title)
                promptInput.asDynamic().value = ""
                promptButton.addEventListener("click", {
                    inProgress = false
                    container.asDynamic().style.display = "none"
                    val password: String? = promptInput.asDynamic().value
                    promptInput.asDynamic().value = ""
                    val eventListener: EventListener? = null
                    promptInput.removeEventListener("click", eventListener)
                    resolve.invoke(password ?: "")
                })
            })
        }
    }
}