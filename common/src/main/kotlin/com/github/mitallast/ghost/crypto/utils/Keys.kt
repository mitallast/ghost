package com.github.mitallast.ghost.crypto.utils

object Keys {
    fun randomKey(sizeInByte: Int) : ByteArray {
        val bytes = ByteArray(sizeInByte)
        CryptoRandom().nextBytes(bytes)
        return bytes
    }
}