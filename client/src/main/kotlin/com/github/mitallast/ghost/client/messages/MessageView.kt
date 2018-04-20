package com.github.mitallast.ghost.client.messages

import com.github.mitallast.ghost.client.common.launch
import com.github.mitallast.ghost.client.files.FilesController
import com.github.mitallast.ghost.client.html.div
import com.github.mitallast.ghost.client.view.View
import com.github.mitallast.ghost.message.FileMessage
import com.github.mitallast.ghost.message.Message
import com.github.mitallast.ghost.message.TextMessage
import org.w3c.dom.url.URL
import kotlin.js.Date

class MessageView(
    message: Message,
    own: Boolean
) : View {
    override val root = div {
        if (own) {
            clazz("message-container", "message-own")
        } else {
            clazz("message-container")
        }
        val content = message.content
        when (content) {
            is TextMessage -> div {
                clazz("message")
                div {
                    clazz("message-text")
                    text(content.text)
                }
                div {
                    clazz("message-time")
                    text(timeFormat(message.date))
                }
            }
            is FileMessage -> {
                val file = content.file
                when {
                    file.mimetype.startsWith("image/") -> {
                        div {
                            clazz("message-image")
                            img {
                                hide()
                                launch {
                                    val blob = FilesController.download(file)
                                    onload {
                                        URL.revokeObjectURL(src)
                                        show()
                                    }
                                    src = URL.createObjectURL(blob)
                                }
                            }
                            div {
                                clazz("message-time")
                                text(timeFormat(message.date))
                            }
                        }
                    }
                    else -> {
                        div {
                            clazz("message")
                            div {
                                clazz("message-file")
                                div {
                                    clazz("files-icon-container")
                                    div {
                                        clazz("file-icon")
                                        text(file.name.split('.').last())
                                    }
                                }
                                div {
                                    clazz("message-file-info")
                                    div {
                                        clazz("file-name")
                                        text(file.name)
                                    }
                                    div {
                                        clazz("file-mimetype")
                                        text(file.mimetype)
                                    }
                                    div {
                                        clazz("file-size")
                                        text("${file.size} bytes")
                                    }
                                }
                                div {
                                    clazz("message-file-actions")
                                    val link = a { hide() }
                                    button {
                                        type("button")
                                        clazz("btn")
                                        text("Download")
                                        onclick {
                                            launch {
                                                val blob = FilesController.download(file)
                                                link.href = URL.createObjectURL(blob)
                                                link.download = file.name
                                                link.element.asDynamic().click()
                                                URL.revokeObjectURL(link.href)
                                                link.href = "#"
                                            }
                                        }
                                    }
                                }
                                div {
                                    clazz("message-time")
                                    text(timeFormat(message.date))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun timeFormat(timestamp: Long): String {
        val date = Date(timestamp)
        val hh = date.getHours().toString().padStart(2, '0')
        val mm = date.getMinutes().toString().padStart(2, '0')
        return "$hh:$mm"
    }
}