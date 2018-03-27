package com.github.mitallast.ghost.crypto.utils

import com.github.mitallast.ghost.codec.CharCodec
import com.github.mitallast.ghost.codec.StringCodec
import kotlinx.io.ByteArrayInputStream
import kotlinx.io.ByteArrayOutputStream

object Strings {
    fun toByteArray(value: String): ByteArray {
        val out = ByteArrayOutputStream()
        value.forEach { CharCodec.write(out, it) }
        return out.toByteArray()
    }

    fun fromByteArray(encoded: ByteArray): String {
        val input = ByteArrayInputStream(encoded)
        val sb = StringBuilder()
        while (input.available() > 0) {
            sb.append(CharCodec.read(input))
        }
        return sb.toString()
    }

    fun digit(c: Char): Int {
        val alphabet = "0123456789ABCDEF"
        val index = alphabet.indexOf(c.toUpperCase())
        require(index >= 0)
        return index
    }

    fun digit(c: Char, radix: Int): Int = digit(c)

    fun fromArray(source: CharArray): String {
        val sb = StringBuilder(source.size)
        source.forEach { sb.append(it) }
        return sb.toString()
    }
}