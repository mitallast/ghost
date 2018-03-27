package com.github.mitallast.ghost.crypto.chf

import com.github.mitallast.ghost.crypto.utils.Integers
import com.github.mitallast.ghost.crypto.utils.Longs
import com.github.mitallast.ghost.crypto.utils.Strings
import kotlin.math.abs
import kotlin.math.sin

object MD5 {

    private val K: IntArray = IntArray(64, { i ->
        ((1L shl 32) * abs(sin(i + 1.0))).toLong().toInt()
    })

    private val SHIFT_AMOUNTS = intArrayOf(7, 12, 17, 22, 5, 9, 14, 20, 4, 11, 16, 23, 6, 10, 15, 21)

    // Initialize variables
    private const val A0 = 0x67452301
    private const val B0 = 0xEFCDAB89.toInt()
    private const val C0 = 0x98BADCFE.toInt()
    private const val D0 = 0x10325476

    fun hash(msgBytes: ByteArray): ByteArray {
        // Pre-processing:
        val preprocessedMessage = preProcessing(msgBytes)

        // Processing
        return processMessage(preprocessedMessage, msgBytes.size)
    }

    fun hash(message: String) = encode(hash(Strings.toByteArray(message)))

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
        toBytes(msgBytes.size * 8L, newMessage, newMessageLen)

        return newMessage
    }

    private fun processMessage(msg: ByteArray, initialLen: Int): ByteArray {

        val chunks = (initialLen + 8).ushr(6) + 1

        // Initialize hash value for this chunk:
        var a = A0
        var b = B0
        var c = C0
        var d = D0
        var i = 0
        val block = IntArray(16)

        while (i < chunks) {
            var index = i shl 6

            for (j in 0..63) {
                block[j ushr 2] = (msg[index].toInt() shl 24) or (block[j ushr 2] ushr 8)
                index++
            }

            val originalA = a
            val originalB = b
            val originalC = c
            val originalD = d

            for (j in 0..63) {
                val div16 = j ushr 4
                var f = 0
                var bufferIndex = j
                when (div16) {
                    0 -> {
                        f = (b and c) or (b.inv() and d)
                    }

                    1 -> {
                        f = (b and d) or (c and d.inv())
                        bufferIndex = (bufferIndex * 5 + 1) and 0x0F
                    }

                    2 -> {
                        f = b xor c xor d
                        bufferIndex = (bufferIndex * 3 + 5) and 0x0F
                    }

                    3 -> {
                        f = c xor (b or d.inv())
                        bufferIndex = (bufferIndex * 7) and 0x0F
                    }
                }

                val temp = b + Integers.rotateLeft(a + f + block[bufferIndex]
                        + K[j], SHIFT_AMOUNTS[(div16 shl 2) or (j and 3)])
                a = d
                d = c
                c = b
                b = temp
            }

            a += originalA
            b += originalB
            c += originalC
            d += originalD

            i++
        }

        val md5 = ByteArray(16)
        var count = 0

        for (k in 0..3) {
            var n = if (k == 0) a else (if (k == 1) b else (if (k == 2) c else d))

            for (j in 0..3) {
                md5[count++] = n.toByte()
                n = n ushr 8
            }
        }

        return md5
    }

    private fun toBytes(value: Long, bytes: ByteArray, offset: Int) {
        var va = value
        for (i in 0..7) {
            bytes[offset + i] = (va and 0x000000FF).toByte()
            va = va.ushr(8)
        }
    }

    fun encode(bytes: ByteArray): String {
        var buf = ""
        var i = 0
        while (i < bytes.size) {
            if (bytes[i].toInt() and 0xff < 0x10) buf += "0"
            buf += Longs.toHex(bytes[i].toLong() and 0xff)
            i++
        }
        return buf
    }
}