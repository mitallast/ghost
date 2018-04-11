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
        ResizeObserver({ mutation() }).observe(root.element)
        ResizeObserver({ mutation() }).observe(view.root.element)
    }

    private var timer: Int? = null
    private var bottom = true
    private var mouseY = 0

    private val minHeight = 20

    private fun scheduleHide() {
        cancelHide()
        timer = window.setTimeout(this::hide, 1000)
    }

    private fun cancelHide() {
        if(timer != null) {
            window.clearTimeout(timer!!)
        }
    }

    private fun resize() {
        if (viewport.scrollHeight != 0) {
            // compute real bar height
            val computedHeight = (barContainer.offsetHeight * (viewport.offsetHeight.toDouble() / viewport.scrollHeight)).roundToInt()
            // constraint bar height
            val height = max(computedHeight, minHeight)
            // what constraint added to real bar height
            val delta = max(0, height - computedHeight)
            // constraint bar container height
            val containerHeight = barContainer.offsetHeight - delta
            // compute offset with constraint
            val offset = (containerHeight * (viewport.scrollTop / viewport.scrollHeight)).roundToInt()
            if (height != barContainer.offsetHeight) {
                scrollbar.style.height = "${height}px"
                scrollbar.style.display = "block"
                scrollbar.style.top = "${offset}px"
                scheduleHide()
            } else hide()
        } else hide()
    }

    private fun hide() {
        scrollbar.style.display = "none"
        cancelHide()
    }

    private fun mutation() {
        if (bottom) {
            viewport.scrollTop = viewport.scrollHeight.toDouble()
        }
        resize()
    }

    private fun scroll(delta: Int) {
        if (bottom && delta > 0) {
            viewport.scrollTop = viewport.scrollHeight.toDouble()
        } else {
            val maxOffset = viewport.scrollHeight - viewport.offsetHeight
            val newOffset = max(0.0, min(maxOffset.toDouble(), (viewport.scrollTop + delta)))
            bottom = maxOffset == newOffset.roundToInt()
            viewport.scrollTop = newOffset
        }
        resize()
    }

    private fun wheel(e: dynamic) {
        e.preventDefault()
        scroll(e.deltaY as Int)
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
        val delta: Int = (viewport.scrollHeight * (mouseDelta.toDouble() / barContainer.offsetHeight)).roundToInt()
        scroll(delta)
    }
}