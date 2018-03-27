package com.github.mitallast.ghost.crypto.dec

import com.github.mitallast.ghost.crypto.utils.Strings

object Base64 {

    private const val CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/"

    fun encode(src: ByteArray, wrap: Boolean = false): String = Strings.fromByteArray(base64encode(src, wrap))

    fun encode(src: String, wrap: Boolean = false): String = encode(Strings.toByteArray(src), wrap)

    fun decode(src: String): String = decode(Strings.toByteArray(src))

    fun decode(src: ByteArray): String = Strings.fromByteArray(base64decode(src))

    private fun base64encode(bytes: ByteArray, wrap: Boolean): ByteArray {

        val size = (bytes.size + 2) / 3 * 4
        val numberOfNewlines = if (wrap && size > 76) size / 76 * 2 else 0
        var numberOfPaddings = if (bytes.size % 3 == 0) 0 else 3 - (bytes.size % 3)
        val srcp = bytes.copyOf(bytes.size + numberOfPaddings)
        val result = ByteArray(size + numberOfNewlines)
        var si = 0
        var ri = 0
        while (si < srcp.size) {
            if (wrap && si > 0 && (si / 3 * 4) % 76 == 0) {
                result[ri++] = 13
                result[ri++] = 10
            }
            val n = (srcp[si++].toInt() and 0xff shl 16) or (srcp[si++].toInt() and 0xff shl 8) or (srcp[si++].toInt() and 0xff)
            result[ri++] = CHARS[n shr 18 and 63].toByte()
            result[ri++] = CHARS[n shr 12 and 63].toByte()
            result[ri++] = CHARS[n shr 6 and 63].toByte()
            result[ri++] = CHARS[n and 63].toByte()
        }

        while (numberOfPaddings > 0) {
            result[result.size - numberOfPaddings] = 61
            numberOfPaddings--
        }

        return result
    }

    private fun base64decode(src: ByteArray): ByteArray {

        val tempBytes = mutableListOf<Byte>()

        src.filter { CHARS.contains(it.toChar()) }
            .forEach {
                when {
                    it == 43.toByte() -> tempBytes.add(62)
                    it == 47.toByte() -> tempBytes.add(63)
                    it < 58 -> tempBytes.add((it + 4).toByte())
                    it < 91 -> tempBytes.add((it - 65).toByte())
                    it < 123 -> tempBytes.add((it - 71).toByte())
                }
            }

        val byteDest = ByteArray(tempBytes.size * 3 / 4)

        var si = 0
        var di = 0
        while (si < tempBytes.size && di < byteDest.size / 3 * 3) {
            byteDest[di++] = (tempBytes[si++].toInt() shl 2 and 0xfc or (tempBytes[si].toInt().ushr(4) and 0x03)).toByte()
            byteDest[di++] = (tempBytes[si++].toInt() shl 4 and 0xf0 or (tempBytes[si].toInt().ushr(2) and 0x0f)).toByte()
            byteDest[di++] = (tempBytes[si++].toInt() shl 6 and 0xc0 or (tempBytes[si++].toInt() and 0x3F)).toByte()
        }

        if (si < tempBytes.size - 2) {
            byteDest[di++] = (tempBytes[si++].toInt() shl 2 and 0xfc or (tempBytes[si].toInt().ushr(4) and 0x03)).toByte()
            byteDest[di] = (tempBytes[si++].toInt() shl 4 and 0xf0 or (tempBytes[si].toInt().ushr(2) and 0x0f)).toByte()
        } else if (si < tempBytes.size - 1) {
            byteDest[di] = (tempBytes[si++].toInt() shl 2 and 0xfc or (tempBytes[si].toInt().ushr(4) and 0x03)).toByte()
        }

        return byteDest
    }
}