package com.github.mitallast.ghost.client.html

import org.w3c.dom.Element
import org.w3c.dom.events.Event
import org.w3c.dom.get
import org.w3c.files.Blob
import kotlin.browser.window
import kotlin.js.json

abstract class HTMLElement(name: String, ns: String? = null) {
    val element = if (ns == null) {
        window.document.createElement(name)
    } else {
        window.document.createElementNS(ns, name)
    }

    val offsetWidth: Int get() = element.asDynamic().offsetWidth as Int
    val offsetHeight: Int get() = element.asDynamic().offsetHeight as Int
    val scrollWidth: Int get() = element.scrollWidth
    val scrollHeight: Int get() = element.scrollHeight

    var scrollTop: Double
        get() = element.scrollTop
        set(value) {
            element.scrollTop = value
        }
    var scrollLeft: Double
        get() = element.scrollLeft
        set(value) {
            element.scrollLeft = value
        }

    val style: dynamic get() = element.asDynamic().style

    fun attr(name: String, value: String) {
        element.setAttribute(name, value)
    }

    fun show() {
        element.asDynamic().style.visibility = "visible"
    }

    fun hide() {
        element.asDynamic().style.visibility = "hidden"
    }

    fun clazz(vararg classList: String) {
        for (c in classList) {
            element.asDynamic().classList.add(c)
        }
    }

    fun removeClass(vararg classList: String) {
        for (c in classList) {
            element.asDynamic().classList.remove(c)
        }
    }

    fun required() {
        element.setAttribute("required", "required")
    }

    fun disabled() {
        element.setAttribute("disabled", "disabled")
    }

    fun readonly() {
        element.setAttribute("readonly", "readonly")
    }

    fun div(init: DIV.() -> Unit): DIV = initTag(DIV(), init)
    fun a(init: A.() -> Unit): A = initTag(A(), init)
    fun h2(init: H2.() -> Unit): H2 = initTag(H2(), init)
    fun h3(init: H3.() -> Unit): H3 = initTag(H3(), init)
    fun h4(init: H4.() -> Unit): H4 = initTag(H4(), init)
    fun span(init: SPAN.() -> Unit): SPAN = initTag(SPAN(), init)
    fun textarea(init: TEXTAREA.() -> Unit): TEXTAREA = initTag(TEXTAREA(), init)
    fun input(init: INPUT.() -> Unit): INPUT = initTag(INPUT(), init)
    fun button(init: BUTTON.() -> Unit): BUTTON = initTag(BUTTON(), init)
    fun form(init: FORM.() -> Unit): FORM = initTag(FORM(), init)
    fun img(init: IMG.() -> Unit): IMG = initTag(IMG(), init)
    fun svg(init: SVG.() -> Unit): SVG = initTag(SVG(), init)

    fun text(text: String) {
        val node = window.document.createTextNode(text)
        element.appendChild(node)
    }

    fun append(node: HTMLElement) {
        element.appendChild(node.element)
    }

    fun append(node: TEXT) {
        element.appendChild(node.node)
    }

    fun appendToBody() {
        window.document.body?.appendChild(element)
    }

    fun on(name: String, listener: (e: dynamic) -> Unit) {
        element.addEventListener(name, listener)
    }

    fun onclick(listener: (e: dynamic) -> Unit) {
        element.addEventListener("click", listener)
    }

    fun onwheel(listener: (e: dynamic) -> Unit) {
        element.addEventListener("wheel", listener, json(Pair("passive", false)))
    }

    fun onload(listener: (e: dynamic) -> Unit) {
        element.addEventListener("load", listener)
    }

    fun remove() {
        element.remove()
    }

    fun remove(child: HTMLElement) {
        element.removeChild(child.element)
    }

    fun removeChildren() {
        val nodes = element.childNodes
        for (i in 0 until nodes.length) {
            element.removeChild(nodes[i]!!)
        }
    }

    protected fun <T : HTMLElement> initTag(tag: T, init: T.() -> Unit): T {
        tag.init()
        element.appendChild(tag.element)
        return tag
    }
}

abstract class SVGElement(name: String) : HTMLElement(name, "http://www.w3.org/2000/svg") {
    fun g(init: G.() -> Unit): G = initTag(G(), init)
    fun rect(init: RECT.() -> Unit): RECT = initTag(RECT(), init)
    fun path(init: PATH.() -> Unit): PATH = initTag(PATH(), init)
    fun polygon(init: POLYGON.() -> Unit): POLYGON = initTag(POLYGON(), init)
}

class POLYGON() : SVGElement("polygon")
class PATH() : SVGElement("path")
class RECT() : SVGElement("rect")
class G() : SVGElement("g")
class SVG() : SVGElement("svg")

class DIV() : HTMLElement("div")
class A() : HTMLElement("a") {
    var href: String
        get() = element.asDynamic().href as String
        set(value) {
            element.asDynamic().href = value
        }
    var download: String
        get() = element.asDynamic().download as String
        set(value) {
            element.asDynamic().download = value
        }
}
class H2() : HTMLElement("h2")
class H3() : HTMLElement("h3")
class H4() : HTMLElement("h4")
class SPAN() : HTMLElement("span")
class TEXTAREA() : HTMLElement("textarea") {
    fun value(): String {
        val text: String? = element.asDynamic().value
        return text ?: ""
    }

    fun clear() {
        element.asDynamic().value = ""
    }

    fun focus() {
        element.asDynamic().focus()
    }
}

class INPUT() : HTMLElement("input") {
    fun type(value: String) = attr("type", value)
    fun name(value: String) = attr("name", value)
    fun autocomplete(value: String) = attr("autocomplete", value)

    fun value(value: String) {
        element.asDynamic().value = value
    }

    fun value(): String {
        val text: String? = element.asDynamic().value
        return text ?: ""
    }

    fun focus() {
        element.asDynamic().focus()
    }
}

class BUTTON() : HTMLElement("button") {
    fun type(value: String) = attr("type", value)
}

class FORM() : HTMLElement("form") {
    fun onsubmit(listener: (e: Event) -> Unit) = on("submit", listener)
}

class IMG() : HTMLElement("img") {
    var src: String
        get() = element.asDynamic().src as String
        set(value) {
            element.asDynamic().src = value
        }
}

class TEXT(text: String) {
    internal val node = window.document.createTextNode(text)

    fun text(text: String) {
        node.data = text
    }
}

fun div(init: DIV.() -> Unit): DIV {
    val node = DIV()
    node.init()
    return node
}

fun a(init: A.() -> Unit): A {
    val node = A()
    node.init()
    return node
}

fun h2(init: H2.() -> Unit): H2 {
    val node = H2()
    node.init()
    return node
}

fun h3(init: H3.() -> Unit): H3 {
    val node = H3()
    node.init()
    return node
}

fun h4(init: H4.() -> Unit): H4 {
    val node = H4()
    node.init()
    return node
}

fun span(init: SPAN.() -> Unit): SPAN {
    val node = SPAN()
    node.init()
    return node
}

fun textarea(init: TEXTAREA.() -> Unit): TEXTAREA {
    val node = TEXTAREA()
    node.init()
    return node
}

fun input(init: INPUT.() -> Unit): INPUT {
    val node = INPUT()
    node.init()
    return node
}

fun button(init: BUTTON.() -> Unit): BUTTON {
    val node = BUTTON()
    node.init()
    return node
}

fun form(init: FORM.() -> Unit): FORM {
    val node = FORM()
    node.init()
    return node
}

fun img(init: IMG.() -> Unit): IMG {
    val node = IMG()
    node.init()
    return node
}

fun text(text: String): TEXT {
    return TEXT(text)
}

external class ResizeObserver(callback: (e: dynamic) -> Unit) {
    fun observe(e: Element)
}