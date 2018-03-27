package com.github.mitallast.ghost.crypto.utils

import java.security.SecureRandom
import java.util.*

actual class CryptoRandom {
    private val rnd = SecureRandom.getInstance("SHA1PRNG")
    actual fun nextBytes(bytes: ByteArray) {
        rnd.nextBytes(bytes)
    }

    actual fun nextInt(): Int {
        return rnd.nextInt()
    }
}