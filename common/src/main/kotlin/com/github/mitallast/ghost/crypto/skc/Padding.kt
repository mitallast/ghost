package com.github.mitallast.ghost.crypto.skc

object Padding {

    enum class SCHEME {
        NONE,
        PKCS5,
        PKCS7
    }

    fun pkcs7Padding(bytes: ByteArray): ByteArray {
        return pkcsPadding(bytes, 16)
    }

    fun pkcs5Padding(bytes: ByteArray): ByteArray {
        return pkcsPadding(bytes, 8)
    }

    fun pkcs7Unpadding(bytes: ByteArray): ByteArray {
        return pkcsUnpadding(bytes)
    }

    fun pkcs5Unpadding(bytes: ByteArray): ByteArray {
        return pkcsUnpadding(bytes)
    }

    private fun pkcsPadding(bytes: ByteArray, size: Int): ByteArray {
        val padding = size - bytes.size % size
        val result = bytes.copyOf(bytes.size + padding)
        for (i in 0 until padding) {
            result[bytes.size + i] = padding.toByte()
        }
        return result
    }

    private fun pkcsUnpadding(b: ByteArray): ByteArray {
        val unpadding = b[b.size - 1].toInt() and 0xff
        val result = ByteArray(b.size - unpadding)
        for (i in result.indices) {
            result[i] = b[i]
        }
        return result
    }
}