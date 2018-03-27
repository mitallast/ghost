package com.github.mitallast.ghost.crypto.mac

import com.github.mitallast.ghost.crypto.chf.MD5
import com.github.mitallast.ghost.crypto.chf.SHA1
import com.github.mitallast.ghost.crypto.chf.SHA256
import com.github.mitallast.ghost.crypto.chf.SHA512
import com.github.mitallast.ghost.crypto.dec.Hex
import com.github.mitallast.ghost.crypto.utils.Strings

object HMAC {

    private const val BLOCK_SIZE_64 = 64
    private const val BLOCK_SIZE_128 = 128

    fun md5(key: String, message: String) = hmac(Strings.toByteArray(key), Strings.toByteArray(message), BLOCK_SIZE_64, MD5::hash)

    fun md5(key: ByteArray, message: ByteArray) = hmac(key, message, BLOCK_SIZE_64, MD5::hash)

    fun sha1(key: String, message: String) = hmac(Strings.toByteArray(key), Strings.toByteArray(message), BLOCK_SIZE_64, SHA1::hash)

    fun sha1(key: ByteArray, message: ByteArray) = hmacRaw(key, message, BLOCK_SIZE_64, SHA1::hash)

    fun sha256(key: String, message: String) = hmac(Strings.toByteArray(key), Strings.toByteArray(message), BLOCK_SIZE_64, SHA256::hash)

    fun sha256(key: ByteArray, message: ByteArray) = hmacRaw(key, message, BLOCK_SIZE_64, SHA256::hash)

    fun sha512(key: String, message: String) = hmac(Strings.toByteArray(key), Strings.toByteArray(message), BLOCK_SIZE_128, SHA512::hash)

    fun sha512(key: ByteArray, message: ByteArray) = hmacRaw(key, message, BLOCK_SIZE_128, SHA512::hash)

    private fun hmacRaw(key: ByteArray, message: ByteArray, blockSize: Int, hashFunction: (m: ByteArray) -> ByteArray): ByteArray {

        var preprocessedKey = key
        if (preprocessedKey.size > blockSize) {
            preprocessedKey = hashFunction(preprocessedKey)
        }

        if (preprocessedKey.size < blockSize) {
            preprocessedKey = preprocessedKey.copyOf(blockSize)
        }

        val outerPaddedKey = xorByteArray(preprocessedKey, ByteArray(blockSize, { 0x5c }))
        val innerPaddedKey = xorByteArray(preprocessedKey, ByteArray(blockSize, { 0x36 }))

        val ipk = hashFunction(appendBytes(innerPaddedKey, message))
        return hashFunction(appendBytes(outerPaddedKey, ipk))
    }

    private fun hmac(key: ByteArray, message: ByteArray, blockSize: Int, hashFunction: (m: ByteArray) -> ByteArray): String {
        return Hex.encode(hmacRaw(key, message, blockSize, hashFunction))
    }

    private fun xorByteArray(a: ByteArray, b: ByteArray): ByteArray {
        val result = ByteArray(a.size)
        for (i in b.indices) {
            result[i] = (a[i].toInt() xor b[i].toInt()).toByte()
        }
        return result
    }

    private fun appendBytes(origin: ByteArray, bytesToAppend: ByteArray): ByteArray {
        val result = origin.copyOf(origin.size + bytesToAppend.size)
        for (i in bytesToAppend.indices) {
            result[i+origin.size] = bytesToAppend[i]
        }
        return result
    }
}