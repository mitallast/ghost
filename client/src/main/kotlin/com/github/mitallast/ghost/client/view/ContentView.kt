package com.github.mitallast.ghost.client.view

import com.github.mitallast.ghost.client.common.launch
import com.github.mitallast.ghost.client.groups.Group
import com.github.mitallast.ghost.client.groups.GroupDialogController
import com.github.mitallast.ghost.client.html.div

class DefaultHeaderView(title: String) : View {
    override val root = div {
        h3 {
            text(title)
        }
    }
}

class ActionHeaderView(
    title: String,
    action: () -> Unit
) : View {
    override val root = div {
        h3 {
            clazz("header-action")
            text(title)
            onclick { action() }
        }
    }
}

object ContentHeaderController {
    private var last: View? = null

    fun title(title: String) {
        view(DefaultHeaderView(title))
    }

    fun action(title: String, action: () -> Unit) {
        view(ActionHeaderView(title, action))
    }

    fun view(view: View) {
        hide()
        last = view
        ContentHeaderView.view(view)
    }

    fun hide() {
        if (last != null) {
            ContentHeaderView.remove(last!!)
            last = null
        }
    }
}

object ContentHeaderView : View {
    override val root = div {
        clazz("content-header")
    }

    fun remove(view: View) {
        root.remove(view.root)
    }

    fun view(view: View) {
        root.append(view.root)
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

    fun contains(view: View): Boolean {
        return last === view
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