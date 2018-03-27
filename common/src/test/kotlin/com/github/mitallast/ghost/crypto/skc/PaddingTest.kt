package com.github.mitallast.ghost.crypto.skc

import kotlin.test.Test
import kotlin.test.assertTrue

class PaddingTest {

    @Test
    fun pkcs5Padding() {
    }

    @Test
    fun pkcs5Unpadding() {
    }

    @Test
    fun pkcs7Padding() {
        var plain = ByteArray(16, { i -> (i + 1).toByte() })
        var testvector = ByteArray(32, { i ->
            (if (i < 16) (i + 1).toByte() else 16)
        })
        var padded = Padding.pkcs7Padding(plain)
        assertTrue(testvector.contentEquals(padded))

        plain = ByteArray(17, { i -> (i + 1).toByte() })
        testvector = ByteArray(32, { i ->
            (if (i < 17) (i + 1).toByte() else 15)
        })
        padded = Padding.pkcs7Padding(plain)
        assertTrue(testvector.contentEquals(padded))

        plain = ByteArray(12, { i -> (i + 1).toByte() })
        testvector = ByteArray(16, { i ->
            (if (i < 12) (i + 1).toByte() else 4)
        })
        padded = Padding.pkcs7Padding(plain)
        assertTrue(testvector.contentEquals(padded))
    }

    @Test
    fun pkcs7Unpadding() {
    }
}