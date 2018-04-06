package com.github.mitallast.ghost.client.prompt

import com.github.mitallast.ghost.client.html.div
import com.github.mitallast.ghost.client.html.input
import kotlin.js.Promise

object PromptView {
    fun prompt(title: String): Promise<String> {
        return Promise({ resolve, _ ->
            val password = input {
                type("password")
                autocomplete("false")
            }
            div {
                val prompt = this
                attr("class", "prompt-container")
                div {
                    attr("class", "prompt")
                    form {
                        div {
                            attr("class", "prompt-title")
                            text(title)
                        }
                        div {
                            attr("class", "prompt-action")
                            attr("required", "required")
                            append(password)
                            button {
                                attr("class", "btn")
                                type("submit")
                                text("OK")
                            }
                            button {
                                attr("class", "btn")
                                type("button")
                                text("Cancel")
                                onclick {
                                    prompt.remove()
                                    resolve.invoke("")
                                }
                            }
                        }
                        onsubmit { e ->
                            e.preventDefault()
                            val pwd = password.value()
                            if (pwd.isNotEmpty()) {
                                prompt.remove()
                                resolve.invoke(pwd)
                            } else {
                                password.focus()
                            }
                        }
                    }
                }
                appendToBody()
            }
        })
    }
}