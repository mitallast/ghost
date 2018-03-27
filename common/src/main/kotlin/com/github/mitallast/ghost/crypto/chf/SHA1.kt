package com.github.mitallast.ghost.crypto.chf

import com.github.mitallast.ghost.crypto.utils.Longs
import com.github.mitallast.ghost.crypto.utils.Strings

object SHA1 {

    private const val H0 = 0x67452301
    private const val H1 = 0xEFCDAB89.toInt()
    private const val H2 = 0x98BADCFE.toInt()
    private const val H3 = 0x10325476
    private const val H4 = 0xC3D2E1F0.toInt()

    fun hash(message: String): String = encode(hash(Strings.toByteArray(message)))

    fun hash(msgBytes: ByteArray): ByteArray {

        // Pre-processing:
        val preprocessedMessage = preProcessing(msgBytes)

        // Processing
        return processMessage(preprocessedMessage, msgBytes.size)
    }

    private fun preProcessing(msgBytes: ByteArray): ByteArray {

        // calculate length in bits ≡ 448 (mod 512)
        val initialLen = msgBytes.size
        var newMessageLen = initialLen
        while (newMessageLen % (512 / 8) != 448 / 8) {
            newMessageLen++
        }

        // append "0" bit until message length
        val newMessage = msgBytes.copyOf(newMessageLen + 8)

        // append "1" bit to message
        newMessage[msgBytes.size] = 0x80.toByte()

        //append original length in bits mod 264 to message
        toBytes(msgBytes.size * 8L, newMessage)

        return newMessage
    }

    private fun processMessage(msg: ByteArray, initialLen: Int): ByteArray {

        val chunks = (initialLen + 8).ushr(6) + 1

        var a = H0
        var b = H1
        var c = H2
        var d = H3
        var e = H4
        var i = 0
        var f = 0
        var k = 0

        while (i < chunks) {

            val w = IntArray(80)

            for (j in 0..79) {
                when (j) {
                    in 0..15 -> {
                        w[j] = msg[i * 64 + 4 * j].toInt() shl 24 and -0x1000000
                        w[j] = w[j] or (msg[i * 64 + 4 * j + 1].toInt() shl 16 and 0x00FF0000)
                        w[j] = w[j] or (msg[i * 64 + 4 * j + 2].toInt() shl 8 and 0xFF00 or (msg[i * 64 + 4 * j + 3].toInt() and 0xFF)) //-128 and 0xFF = 128
                    }
                    in 16..79 -> {
                        w[j] = leftrotate(w[j - 3] xor w[j - 8] xor w[j - 14] xor w[j - 16], 1)
                    }
                }
            }

            val originalA = a
            val originalB = b
            val originalC = c
            val originalD = d
            val originalE = e

            for (j in 0..79) {
                when (j) {
                    in 0..19 -> {
                        f = b and c or (b.inv() and d)
                        k = 0x5A827999
                    }
                    in 20..39 -> {
                        f = b xor c xor d
                        k = 0x6ED9EBA1
                    }
                    in 40..59 -> {
                        f = b and c or (b and d) or (c and d)
                        k = 0x8F1BBCDC.toInt()
                    }
                    in 60..79 -> {
                        f = b xor c xor d
                        k = 0xCA62C1D6.toInt()
                    }
                }

                val temp = leftrotate(a, 5) + f + e + k + w[j]
                e = d
                d = c
                c = leftrotate(b, 30)
                b = a
                a = temp
            }

            a += originalA
            b += originalB
            c += originalC
            d += originalD
            e += originalE

            i++
        }

        val sha1raw = ByteArray(20)

        append(sha1raw, a, 0)
        append(sha1raw, b, 4)
        append(sha1raw, c, 8)
        append(sha1raw, d, 12)
        append(sha1raw, e, 16)

        return sha1raw
    }

    private fun toBytes(value: Long, bytes: ByteArray) {
        var va = value
        for (i in 1..7) {
            bytes[bytes.size - i] = (va and 0x000000FF).toByte()
            va = va.ushr(8)
        }
    }

    private fun leftrotate(num: Int, am: Int): Int {
        return num shl am or num.ushr(32 - am)
    }

    private fun append(hash: ByteArray, value: Int, offset: Int) {
        for (j in 0..3) {
            hash[j + offset] = (value.ushr(24 - j * 8) and 0xFF).toByte()
        }
    }

    fun encode(bytes: ByteArray): String {
        var buf = ""
        var i = 0
        while (i < bytes.size) {
            if (bytes[i].toInt() and 0xff < 0x10) buf += "0"
            buf += Longs.toHex((bytes[i].toInt() and 0xff).toLong())
            i++
        }
        return buf
    }
}