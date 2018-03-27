package com.github.mitallast.ghost.crypto.chf

import com.github.mitallast.ghost.crypto.utils.Longs
import com.github.mitallast.ghost.crypto.utils.Strings

object SHA512 {

    private var H = longArrayOf(0x6A09E667F3BCC908L, -0x4498517a7b3558c5L, 0x3C6EF372FE94F82BL, -0x5ab00ac5a0e2c90fL, 0x510E527FADE682D1L, -0x64fa9773d4c193e1L,
            0x1F83D9ABFB41BD6BL, 0x5BE0CD19137E2179L)

    private var K = longArrayOf(0x428A2F98D728AE22L, 0x7137449123EF65CDL, -0x4a3f043013b2c4d1L, -0x164a245a7e762444L, 0x3956C25BF348B538L, 0x59F111F1B605D019L,
            -0x6dc07d5b50e6b065L, -0x54e3a12a25927ee8L, -0x27f855675cfcfdbeL, 0x12835B0145706FBEL, 0x243185BE4EE4B28CL, 0x550C7DC3D5FFB4E2L, 0x72BE5D74F27B896FL,
            -0x7f214e01c4e9694fL, -0x6423f958da38edcbL, -0x3e640e8b3096d96cL, -0x1b64963e610eb52eL, -0x1041b879c7b0da1dL, 0x0FC19DC68B8CD5B5L, 0x240CA1CC77AC9C65L,
            0x2DE92C6F592B0275L, 0x4A7484AA6EA6E483L, 0x5CB0A9DCBD41FBD4L, 0x76F988DA831153B5L, -0x67c1aead11992055L, -0x57ce3992d24bcdf0L, -0x4ffcd8376704dec1L,
            -0x40a680384110f11cL, -0x391ff40cc257703eL, -0x2a586eb86cf558dbL, 0x06CA6351E003826FL, 0x142929670A0E6E70L, 0x27B70A8546D22FFCL, 0x2E1B21385C26C926L,
            0x4D2C6DFC5AC42AEDL, 0x53380D139D95B3DFL, 0x650A73548BAF63DEL, 0x766A0ABB3C77B2A8L, -0x7e3d36d1b812511aL, -0x6d8dd37aeb7dcac5L, -0x5d40175eb30efc9cL,
            -0x57e599b443bdcfffL, -0x3db4748f2f07686fL, -0x3893ae5cf9ab41d0L, -0x2e6d17e62910ade8L, -0x2966f9dbaa9a56f0L, -0xbf1ca7aa88edfd6L, 0x106AA07032BBD1B8L,
            0x19A4C116B8D2D0C8L, 0x1E376C085141AB53L, 0x2748774CDF8EEB99L, 0x34B0BCB5E19B48A8L, 0x391C0CB3C5C95A63L, 0x4ED8AA4AE3418ACBL, 0x5B9CCA4F7763E373L,
            0x682E6FF3D6B2B8A3L, 0x748F82EE5DEFB2FCL, 0x78A5636F43172F60L, -0x7b3787eb5e0f548eL, -0x7338fdf7e59bc614L, -0x6f410005dc9ce1d8L, -0x5baf9314217d4217L,
            -0x41065c084d3986ebL, -0x398e870d1c8dacd5L, -0x35d8c13115d99e64L, -0x2e794738de3f3df9L, -0x15258229321f14e2L, -0xa82b08011912e88L, 0x06F067AA72176FBAL,
            0x0A637DC5A2C898A6L, 0x113F9804BEF90DAEL, 0x1B710B35131C471BL, 0x28DB77F523047D84L, 0x32CAAB7B40C72493L, 0x3C9EBE0A15C9BEBCL, 0x431D67C49C100D4CL,
            0x4CC5D4BECB3E42B6L, 0x597F299CFC657E2AL, 0x5FCB6FAB3AD6FAECL, 0x6C44198C4A475817L)

    fun hash(msgBytes: ByteArray): ByteArray {

        // Pre-processing:
        val preprocessedMessage = preProcessing(msgBytes)

        // Processing
        return processMessage(preprocessedMessage, preprocessedMessage.size)
    }

    fun hash(message: String) = encode(hash(Strings.toByteArray(message)))

    private fun preProcessing(msgBytes: ByteArray): ByteArray {
        val initialLen = msgBytes.size
        var newMessageLen = initialLen
        while (newMessageLen % (1024 / 8) != 896 / 8) {
            newMessageLen++
        }

        // append "0" bit until message length
        val newMessage = msgBytes.copyOf(newMessageLen + 16)

        // append "1" bit to message
        newMessage[msgBytes.size] = 0x80.toByte()

        //append original length in bits mod 264 to message
        toBytes(msgBytes.size * 8L, newMessage)

        return newMessage
    }

    private fun processMessage(msg: ByteArray, len: Int): ByteArray {

        val chunks = len / 128

        var a = H[0]
        var b = H[1]
        var c = H[2]
        var d = H[3]
        var e = H[4]
        var f = H[5]
        var g = H[6]
        var h = H[7]
        var i = 0

        while (i < chunks) {
            val w = LongArray(80)

            for (j in 0..79) {
                when (j) {
                    in 0..15 -> {
                        w[j] = toLong(msg, i * 128 + 8 * j)
                    }
                    in 16..79 -> {
                        val s0 = rightrotate(w[j-15], 1) xor rightrotate(w[j-15],8) xor w[j-15].ushr(7)
                        val s1 = rightrotate(w[j-2], 19) xor rightrotate(w[j-2], 61) xor w[j-2].ushr(6)
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

            for (j in 0..79) {

                val s1 = rightrotate(e, 14) xor rightrotate(e, 18) xor rightrotate(e, 41)
                val ch = (e and f) xor (e.inv() and g)
                val temp1 = h + s1 + ch + K[j] + w[j]
                val s0 = rightrotate(a, 28) xor rightrotate(a, 34) xor rightrotate(a, 39)
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

        val sha512raw = ByteArray(64)

        append(sha512raw, a, 0)
        append(sha512raw, b, 8)
        append(sha512raw, c, 16)
        append(sha512raw, d, 24)
        append(sha512raw, e, 32)
        append(sha512raw, f, 40)
        append(sha512raw, g, 48)
        append(sha512raw, h, 56)

        return sha512raw
    }

    private fun toLong(input: ByteArray, j: Int): Long {
        var v: Long = 0
        for (i in 0..7) {
            v = (v shl 8) + (input[i + j].toLong() and 0xff)
        }
        return v
    }

    private fun encode(bytes: ByteArray): String {
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
        for (i in 1..15) {
            bytes[bytes.size - i] = (va and 0x000000FF).toByte()
            va = va.ushr(8)
        }
    }

    private fun rightrotate(num: Long, am: Int): Long {
        return num.ushr(am) or (num shl 64 - am)
    }

    private fun append(hash: ByteArray, value: Long, offset: Int) {
        for (j in 0..7) {
            hash[j + offset] = (value.ushr(56 - j * 8) and 0xFF).toByte()
        }
    }
}