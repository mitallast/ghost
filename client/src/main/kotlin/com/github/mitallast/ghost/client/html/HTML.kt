package com.github.mitallast.ghost.client.html

import org.w3c.dom.events.Event
import org.w3c.dom.get
import kotlin.browser.window

abstract class Element(name: String, ns: String? = null) {
    protected val element = if (ns == null) {
        window.document.createElement(name)
    } else {
        window.document.createElementNS(ns, name)
    }


    fun attr(name: String, value: String) {
        element.setAttribute(name, value)
    }

    fun show() {
        attr("style", "display: none;")
    }
    fun hide() {
        attr("style", "display: none;")
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
    fun svg(init: SVG.() -> Unit): SVG = initTag(SVG(), init)

    fun text(text: String) {
        val node = window.document.createTextNode(text)
        element.appendChild(node)
    }

    fun append(node: Element) {
        element.appendChild(node.element)
    }

    fun append(node: TEXT) {
        element.appendChild(node.node)
    }

    fun appendToBody() {
        window.document.body?.appendChild(element)
    }

    fun on(name: String, listener: (e: Event) -> Unit) {
        element.addEventListener(name, listener)
    }

    fun onclick(listener: (e: Event) -> Unit) = on("click", listener)

    fun remove() {
        element.remove()
    }

    fun remove(child: Element) {
        element.removeChild(child.element)
    }

    fun removeChildren() {
        val nodes = element.childNodes
        for (i in 0 until nodes.length) {
            element.removeChild(nodes[i]!!)
        }
    }

    protected fun <T : Element> initTag(tag: T, init: T.() -> Unit): T {
        tag.init()
        element.appendChild(tag.element)
        return tag
    }
}

abstract class SVGElement(name: String) : Element(name, "http://www.w3.org/2000/svg") {
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

class DIV() : Element("div")
class A() : Element("a")
class H2() : Element("h2")
class H3() : Element("h3")
class H4() : Element("h4")
class SPAN() : Element("span")
class TEXTAREA() : Element("textarea") {
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
class INPUT() : Element("input") {
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

class BUTTON() : Element("button") {
    fun type(value: String) = attr("type", value)
}

class FORM() : Element("form") {
    fun onsubmit(listener: (e: Event) -> Unit) = on("submit", listener)
}

class TEXT(text: String) {
    internal val node = window.document.createTextNode(text)

    fun text(text: String) {
        node.replaceWith(text)
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

fun text(text: String): TEXT {
    return TEXT(text)
}