package com.github.mitallast.ghost.crypto.utils

actual class CryptoRandom {
    actual fun nextBytes(bytes: ByteArray) {
        js("window.crypto.getRandomValues(bytes);")
    }

    actual fun nextInt(): Int {
        val bytes = ByteArray(4)
        nextBytes(bytes)
        val a = bytes[0].toInt() and 0xFF shl 24
        val b = bytes[1].toInt() and 0xFF shl 16
        val c = bytes[2].toInt() and 0xFF shl 8
        val d = bytes[3].toInt() and 0xFF
        return a.or(b).or(c).or(d)
    }
}