package com.github.mitallast.ghost.crypto.utils

expect class CryptoRandom() {
    fun nextBytes(bytes: ByteArray)
    fun nextInt(): Int
}