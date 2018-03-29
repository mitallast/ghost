package com.github.mitallast.ghost.client.crypto

import org.khronos.webgl.Uint8Array

external class TextEncoder() {
    val encoding: String
    fun encode(text: String): Uint8Array
}