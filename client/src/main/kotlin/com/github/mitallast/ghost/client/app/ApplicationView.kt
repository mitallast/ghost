package com.github.mitallast.ghost.client.app

import com.github.mitallast.ghost.client.crypto.HEX
import com.github.mitallast.ghost.client.dialogs.DialogsFlow
import com.github.mitallast.ghost.client.dialogs.DialogsView
import com.github.mitallast.ghost.client.html.div
import com.github.mitallast.ghost.client.html.text
import com.github.mitallast.ghost.client.messages.MessagesView
import com.github.mitallast.ghost.message.TextMessage

object ApplicationView {
    private val root = div {
        attr("class", "ghost")
        div {
            attr("class", "ghost-container")
            div {
                attr("class", "ghost-sidebar")
                div {
                    attr("class", "sidebar-header")
                    div {
                        attr("class", "sidebar-header-content")
                        a {
                            attr("class", "sidebar-menu-button")
                            attr("title", "Show menu")
                            svg {
                                attr("viewBox", "0 0 16 14")
                                g {
                                    attr("fill", "#000")
                                    attr("fill-fule", "#evenodd")
                                    rect {
                                        attr("width", "16")
                                        attr("height", "2")
                                        attr("rx", "1")
                                    }
                                    rect {
                                        attr("width", "16")
                                        attr("height", "2")
                                        attr("y", "6")
                                        attr("rx", "1")
                                    }
                                    rect {
                                        attr("width", "16")
                                        attr("height", "2")
                                        attr("y", "12")
                                        attr("rx", "1")
                                    }
                                }
                            }

                            onclick {
                                DialogsFlow.add()
                            }
                        }
                        h2 {
                            attr("class", "sidebar-header-logo")
                            text("Ghost messenger")
                        }
                    }
                }
                div {
                    attr("class", "sidebar-list")
                    append(DialogsView.root)
                }
            }
            append(ApplicationMainView.root)
        }
        appendToBody()
    }
}

object ApplicationMainView {
    val root = div {
        attr("class", "ghost-main")
        append(ChatView.root)
    }
}

object ChatView {
    private val title = text("")
    val root = div {
        attr("class", "chat-container")
        div {
            attr("class", "chat-header")
            div {
                attr("class", "chat-content")
                h3 { append(title) }
            }
        }
        div {
            attr("class", "chat-list")
            append(MessagesView.root)
        }
        div {
            attr("class", "chat-footer")
            append(MessageEditorView.root)
        }
    }

    fun currentDialog(auth: ByteArray) {
        title.text(HEX.toHex(auth))
    }
}

object MessageEditorView {
    val root = div {
        attr("class", "message-editor")
        textarea {
            on("keypress", { event ->
                val key: Int = event.asDynamic().keyCode;
                if (key == 13) {
                    event.preventDefault()
                    val text = value()
                    if (text.isNotEmpty()) {
                        clear()
                        DialogsFlow.send(TextMessage(text))
                    } else {
                        focus()
                    }
                }
            })
        }
    }
}