package com.github.mitallast.ghost.client.files

import com.github.mitallast.ghost.client.html.div
import com.github.mitallast.ghost.client.view.View
import org.w3c.dom.url.URL
import org.w3c.files.File
import org.w3c.files.FileList
import org.w3c.files.get
import kotlin.browser.window

interface FilesDropHandler {
    fun send(file: File)
}

object FilesDropController {
    private var handler: FilesDropHandler? = null
    private var last: View? = null

    init {
        window.document.body!!.addEventListener("dragover", { e ->
            e.stopPropagation()
            e.preventDefault()
            console.log("drop over")
            show()
        })
    }

    fun handle(newHandler: FilesDropHandler) {
        handler = newHandler
    }

    fun show() {
        if (handler != null) {
            view(FilesDropBox)
        }
    }

    fun hide() {
        FilesDropView.root.hide()
    }

    private fun view(view: View) {
        if (last != null) {
            FilesDropView.hide(last!!)
        }
        last = view
        FilesDropView.root.show()
        FilesDropView.view(view)
    }

    fun drop(files: FileList) {
        if (handler != null) {
            if (files.length != 0) {
                val file = files[0]!! // @todo multiple
                if (file.type.startsWith("image/")) {
                    console.info("view as image")
                    view(FilesDropImage(file))
                } else {
                    // @todo
                    console.warn("unsupported")
                }
            } else {
                console.warn("empty files")
            }
        } else {
            console.warn("no handler")
        }
    }

    fun send(file: File) {
        if (handler != null) {
            handler!!.send(file)
        }
        hide()
    }
}

object FilesDropView : View {

    override val root = div {
        clazz("files-drop-container")

        on("dragover", { e ->
            e.stopPropagation()
            e.preventDefault()
            console.log("drop over")
        })
        on("dragleave", { e ->
            e.stopPropagation()
            e.preventDefault()
            console.log("drop leave")
            FilesDropController.hide()
        })
        on("drop", { e ->
            e.stopPropagation()
            e.preventDefault()
            console.log("drop", e)
            if (e.target != null && e.target.files != null) {
                console.log("files", e.dataTransfer.files)
                val files = e.target.files as FileList
                FilesDropController.drop(files)
            } else if (e.dataTransfer != null && e.dataTransfer.files != null) {
                console.log("files", e.dataTransfer.files)
                val files = e.dataTransfer.files as FileList
                FilesDropController.drop(files)
            } else {
                console.log("unsupported drop event", e)
            }
        })

        appendToBody()
    }

    fun hide(view: View) {
        root.remove(view.root)
    }

    fun view(view: View) {
        root.append(view.root)
    }
}

object FilesDropBox : View {
    override val root = div {
        clazz("files-drop-box")
    }
}

class FilesDropImage(private val file: File) : View {
    override val root = div {
        clazz("files-drop-image-box")
        div {
            clazz("files-drop-image")
            img {
                src = URL.createObjectURL(file)
                onload { URL.revokeObjectURL(src) }
            }
        }
        div {
            clazz("files-drop-button")
            span {
                clazz("file-name")
                text(file.name)
            }
            span {
                clazz("file-mimetype")
                text(file.type)
            }
            span {
                clazz("file-size")
                text(file.size.toString())
            }
            button {
                clazz("btn")
                type("button")
                text("Upload")
                onclick { e ->
                    e.preventDefault()
                    FilesDropController.send(file)
                }
            }
        }
    }
}