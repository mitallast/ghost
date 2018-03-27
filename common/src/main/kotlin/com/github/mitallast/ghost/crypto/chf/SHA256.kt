package com.github.mitallast.ghost.crypto.chf

import com.github.mitallast.ghost.crypto.utils.Longs
import com.github.mitallast.ghost.crypto.utils.Strings

object SHA256 {

    private const val H0 = 0x6a09e667
    private const val H1 = -0x4498517b
    private const val H2 = 0x3c6ef372
    private const val H3 = -0x5ab00ac6
    private const val H4 = 0x510e527f
    private const val H5 = -0x64fa9774
    private const val H6 = 0x1f83d9ab
    private const val H7 = 0x5be0cd19

    private val k = intArrayOf(0x428a2f98, 0x71374491, -0x4a3f0431, -0x164a245b, 0x3956c25b, 0x59f111f1, -0x6dc07d5c, -0x54e3a12b,
            -0x27f85568, 0x12835b01, 0x243185be, 0x550c7dc3, 0x72be5d74, -0x7f214e02, -0x6423f959, -0x3e640e8c, -0x1b64963f, -0x1041b87a,
            0x0fc19dc6, 0x240ca1cc, 0x2de92c6f, 0x4a7484aa, 0x5cb0a9dc, 0x76f988da, -0x67c1aeae, -0x57ce3993, -0x4ffcd838, -0x40a68039,
            -0x391ff40d, -0x2a586eb9, 0x06ca6351, 0x14292967, 0x27b70a85, 0x2e1b2138, 0x4d2c6dfc, 0x53380d13, 0x650a7354, 0x766a0abb,
            -0x7e3d36d2, -0x6d8dd37b, -0x5d40175f, -0x57e599b5, -0x3db47490, -0x3893ae5d, -0x2e6d17e7, -0x2966f9dc, -0xbf1ca7b, 0x106aa070,
            0x19a4c116, 0x1e376c08, 0x2748774c, 0x34b0bcb5, 0x391c0cb3, 0x4ed8aa4a, 0x5b9cca4f, 0x682e6ff3, 0x748f82ee, 0x78a5636f,
            -0x7b3787ec, -0x7338fdf8, -0x6f410006, -0x5baf9315, -0x41065c09, -0x398e870e)

    fun hash(msgBytes: ByteArray): ByteArray {

        // Pre-processing
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
        var f = H5
        var g = H6
        var h = H7
        var i = 0

        while (i < chunks) {
            val w = IntArray(64)

            for (j in 0..63) {
                when (j) {
                    in 0..15 -> {
                        w[j] = msg[i * 64 + 4 * j].toInt() shl 24 and -0x1000000
                        w[j] = w[j] or (msg[i * 64 + 4 * j + 1].toInt() shl 16 and 0x00FF0000)
                        w[j] = w[j] or (msg[i * 64 + 4 * j + 2].toInt() shl 8 and 0xFF00 or (msg[i * 64 + 4 * j + 3].toInt() and 0xFF)) //-128 and 0xFF = 128
                    }
                    in 16..63 -> {
                        val s0 = rightrotate(w[j-15], 7) xor rightrotate(w[j-15],18) xor w[j-15].ushr(3)
                        val s1 = rightrotate(w[j-2], 17) xor rightrotate(w[j-2], 19) xor w[j-2].ushr(10)
                        w[j] = w[j-16] + s0 + w[j-7] + s1

                    }
                }
            }

            val originalA = a
            val originalB = b
            val originalC = c
            val originalD = d
            val originalE = e
            val originalF = f
            val originalG = g
            val originalH = h

            for (j in 0..63) {

                val s1 = rightrotate(e, 6) xor rightrotate(e, 11) xor rightrotate(e, 25)
                val ch = (e and f) xor (e.inv() and g)
                val temp1 = h + s1 + ch + k[j] + w[j]
                val s0 = rightrotate(a, 2) xor rightrotate(a, 13) xor rightrotate(a, 22)
                val maj = (a and b) xor (a and c) xor (b and c)
                val temp2 = s0 + maj

                h = g
                g = f
                f = e
                e = d + temp1
                d = c
                c = b
                b = a
                a = temp1 + temp2
            }

            a += originalA
            b += originalB
            c += originalC
            d += originalD
            e += originalE
            f += originalF
            g += originalG
            h += originalH

            i++
        }

        val sha256raw = ByteArray(32)

        append(sha256raw, a, 0)
        append(sha256raw, b, 4)
        append(sha256raw, c, 8)
        append(sha256raw, d, 12)
        append(sha256raw, e, 16)
        append(sha256raw, f, 20)
        append(sha256raw, g, 24)
        append(sha256raw, h, 28)

        return sha256raw
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

    private fun toBytes(value: Long, bytes: ByteArray) {
        var va = value
        for (i in 1..7) {
            bytes[bytes.size - i] = (va and 0x000000FF).toByte()
            va = va.ushr(8)
        }
    }

    private fun rightrotate(num: Int, am: Int): Int {
        return num.ushr(am) or (num shl 32 - am)
    }

    private fun append(hash: ByteArray, value: Int, offset: Int) {
        for (j in 0..3) {
            hash[j + offset] = (value.ushr(24 - j * 8) and 0xFF).toByte()
        }
    }
}