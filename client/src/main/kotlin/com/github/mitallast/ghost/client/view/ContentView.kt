package com.github.mitallast.ghost.client.view

import com.github.mitallast.ghost.client.html.div
import com.github.mitallast.ghost.client.html.text

object ContentHeaderView : View {
    private val titleText = text("Ghost messenger")
    override val root = div {
        clazz("content-header")
        h3 {
            append(titleText)
        }
    }

    fun setTitle(title: String) {
        titleText.text(title)
    }
}

object ContentMainController {
    private var last: View? = null
    fun view(view: View) {
        hide()
        last = view
        ContentMainView.view(view)
    }

    fun hide() {
        if (last != null) {
            ContentMainView.remove(last!!)
            last = null
        }
    }
}

object ContentMainView : View {
    override val root = div {
        clazz("content-main")
    }

    fun remove(view: View) {
        root.remove(view.root)
    }

    fun view(view: View) {
        root.append(view.root)
    }
}

object ContentFooterController {
    private var last: View? = null
    fun view(view: View) {
        hide()
        last = view
        ContentFooterView.view(view)
    }

    fun hide() {
        if (last != null) {
            ContentFooterView.remove(last!!)
            last = null
        }
    }
}

object ContentFooterView : View {
    override val root = div {
        clazz("content-footer")
    }

    fun remove(view: View) {
        root.remove(view.root)
    }

    fun view(view: View) {
        root.append(view.root)
    }
}