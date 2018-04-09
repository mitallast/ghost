package com.github.mitallast.ghost.client.view

import com.github.mitallast.ghost.client.html.ResizeObserver
import com.github.mitallast.ghost.client.html.div
import kotlin.browser.window
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

class ReverseScrollView(private val view: View) : View {
    private val scrollbar = div {
        clazz("scroll-bar")
    }
    private val barContainer = div {
        clazz("scroll-bar-container")
        append(scrollbar)
    }
    private val viewport = div {
        clazz("scroll-viewport")
        append(view.root)
    }
    override val root = div {
        clazz("scroll-container")
        append(barContainer)
        append(viewport)
    }

    init {
        root.onwheel(this::wheel)
        scrollbar.on("mousedown", this::mouseDown)
        ResizeObserver({ resize() }).observe(root.element)
        ResizeObserver({ mutation() }).observe(view.root.element)
    }

    private var bottom = true
    private var mouseY = 0

    private fun resize() {
        if(viewport.scrollHeight != 0) {
            val h = (barContainer.offsetHeight * (viewport.offsetHeight.toDouble() / viewport.scrollHeight)).roundToInt()
            val offset = (barContainer.offsetHeight * (viewport.scrollTop / viewport.scrollHeight)).roundToInt()
            if (h != barContainer.offsetHeight) {
                scrollbar.style.height = "${h}px"
                scrollbar.style.display = "block"
                scrollbar.style.top = "${offset}px"
            } else hide()
        }else hide()
    }

    private fun hide() {
        scrollbar.style.display = "none"
    }

    private fun mutation() {
        if(bottom) {
            viewport.scrollTop = viewport.scrollHeight.toDouble()
        }
        resize()
    }

    private fun scroll(delta: Int) {
        val maxOffset = viewport.scrollHeight - viewport.offsetHeight
        val newOffset = max(0.0, min(maxOffset.toDouble(), (viewport.scrollTop + delta)))
        bottom = maxOffset == newOffset.roundToInt()
        viewport.scrollTop = newOffset
        resize()
    }

    private fun wheel(e: dynamic) {
        e.preventDefault()
        scroll(e.deltaY as Int / 10)
    }

    private fun mouseDown(e: dynamic) {
        e.preventDefault()
        window.document.addEventListener("mouseup", mouseUp)
        window.document.addEventListener("mousemove", mouseMove)
        mouseY = e.clientY as Int
    }

    private val mouseUpListener: (dynamic) -> Unit = {
         window.document.removeEventListener("mouseup", this.mouseUp)
         window.document.removeEventListener("mousemove", mouseMove)
    }

    private val mouseUp = mouseUpListener

    private val mouseMove: (dynamic) -> Unit = { e ->
        val mouseDelta = e.clientY as Int - mouseY
        mouseY = e.clientY as Int
        val delta : Int= (viewport.scrollHeight * (mouseDelta.toDouble() / barContainer.offsetHeight)).roundToInt()
        scroll(delta)
    }
}