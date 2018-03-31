package com.github.mitallast.ghost.client.crypto

import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Uint8Array
import org.khronos.webgl.get
import org.khronos.webgl.set

object HEX {

    fun toHex(b: ArrayBuffer): String {
        return toHex(Uint8Array(b))
    }

    fun toHex(array: Uint8Array): String {
        var hex = "";
        for (i in 0 until array.length) {
            hex += byteToHex(array[i].toInt())
        }
        return hex
    }

    private fun byteToHex(n: Int): String {
        return "" + hexChar(n shr 4 and 0xF) + "" + hexChar(n and 0xF)
    }

    private fun hexChar(n: Int): Char {
        return if (n < 10) '0' + n else 'a' + (n - 10)
    }

    fun parseHex(s: String): Uint8Array {
        val len = s.length

        if (len % 2 != 0) {
            throw IllegalArgumentException("hexBinary needs to be even-length: $s")
        }

        val out = Uint8Array(len / 2)

        var i = 0
        while (i < len) {
            val h = parseHex(s[i])
            val l = parseHex(s[i + 1])
            if (h == -1 || l == -1) {
                throw IllegalArgumentException("contains illegal character for hexBinary: $s")
            }

            out[i / 2] = (h * 16 + l).toByte()
            i += 2
        }

        return out
    }

    private fun parseHex(ch: Char): Int {
        if (ch in '0'..'9') {
            return ch - '0'
        }
        if (ch in 'A'..'F') {
            return ch - 'A' + 10
        }
        return if (ch in 'a'..'f') {
            ch - 'a' + 10
        } else -1
    }
}
