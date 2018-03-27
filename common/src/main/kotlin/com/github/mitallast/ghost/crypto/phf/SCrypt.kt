package com.github.mitallast.ghost.crypto.phf

import com.github.mitallast.ghost.crypto.utils.Arrays
import com.github.mitallast.ghost.crypto.utils.CryptoRandom
import kotlin.experimental.xor
import kotlin.math.pow


object SCrypt {

    fun hash(password: String, salt: ByteArray, n: Int, r: Int, p: Int): ByteArray {

        if (n < 2 || n and n - 1 != 0 || (n > (2.0).pow(128.0 * r / 8))) {
            throw IllegalArgumentException("costParameter must be larger than 1, a power of 2, and less than 2^(128 * r / 8)")
        }

        if(p < 0 || p > (((2.0).pow(32.0 - 1)) * 32) / (128 * r)) {
            throw IllegalArgumentException("parallelizationParameter must be positiv integer less than or equal to ((2^32-1) * 32) / (128 * r)")
        }

        val dkLen = p * 128 * r

        if (dkLen > ((2.0).pow(32.0) - 1) * 32) {
            throw IllegalArgumentException("derived key too long")
        }

        val b = PBKDF2.hash("sha256", password, salt, 1, dkLen)

        for (i in 0 until p) {
            var bi = b.copyOfRange(i * 128 * r, 128 * r)
            bi = scryptROMix(r, bi, n)
            Arrays.arraycopy(bi, 0, b, i * 128 * r, 128 * r)
        }

        return PBKDF2.hash("sha256", password, b, 1, 32)
    }

    fun verify(password: String, hashed: ByteArray, salt: ByteArray, n: Int, r: Int, p: Int): Boolean {

        val verify = hash(password, salt, n, r, p)

        var diff = verify.size xor hashed.size
        var i = 0
        while (i < verify.size && i < hashed.size) {
            diff = diff or (verify[i].toInt() xor hashed[i].toInt())
            i++
        }
        return diff == 0
    }

    fun salt(size: Int): ByteArray {
        val saltBytes = ByteArray(size)
        CryptoRandom().nextBytes(saltBytes)
        return saltBytes
    }

    private fun scryptROMix(r: Int, bytes: ByteArray, n: Int) : ByteArray {

        val v = ByteArray(128 * r * n)
        var b = bytes.copyOf()

        for (i in 0 until n) {
            Arrays.arraycopy(b, 0, v, i * (128 * r), 128 * r)
            b = scryptBlockMix(b, r)
        }

        for (i in 0 until n) {
            val j = integerify(b, r) and (n - 1)
            xor(b, v, j * (128 * r))
            b = scryptBlockMix(b, r)
        }

        return b
    }

    private fun scryptBlockMix(b: ByteArray, r: Int): ByteArray {

        val y = ByteArray(b.size)
        val x = b.copyOfRange((2 * r - 1) * 64, (2 * r - 1) * 64 + 64)
        var j = 0

        for (i in 0 until (2 * r)) {
            xor(x, b, i * 64)
            salsa208(x)
            if(i % 2 == 0) {
                Arrays.arraycopy(x, 0, y, j * 64, 64)
            } else {
                Arrays.arraycopy(x, 0, y, (j + r) * 64, 64)
                j++
            }
        }

        return y
    }

    private fun integerify(b: ByteArray, r: Int): Int {
        val num = (2 * r - 1) * 64
        var n = b[num].toInt() and 0xff shl 0
        n = n or (b[num + 1].toInt() and 0xff shl 8)
        n = n or (b[num + 2].toInt() and 0xff shl 16)
        n = n or (b[num + 3].toInt() and 0xff shl 24)
        return n
    }

    private fun xor(dest: ByteArray, src: ByteArray, from: Int) {
        for (i in dest.indices) {
            dest[i] = dest[i] xor src[from + i]
        }
    }

    private fun salsa208(b: ByteArray) {

        val inArray = IntArray(16)
        val outArray = IntArray(16)
        var i = 0

        while (i < 16) {
            inArray[i] = (((b[i * 4]).toInt() and 0xff) shl 0)
            inArray[i] = inArray[i] or (((b[i * 4 + 1]).toInt() and 0xff) shl 8)
            inArray[i] = inArray[i] or (((b[i * 4 + 2]).toInt() and 0xff) shl 16)
            inArray[i] = inArray[i] or (((b[i * 4 + 3]).toInt() and 0xff) shl 24)
            i++
        }

        val x = inArray.copyOf()

        i = 8
        while (i > 0) {
            x[4] = x[4] xor shift(x[0] + x[12], 7)
            x[8] = x[8] xor shift(x[4] + x[0], 9)
            x[12] = x[12] xor shift(x[8] + x[4], 13)
            x[0] = x[0] xor shift(x[12] + x[8], 18)
            x[9] = x[9] xor shift(x[5] + x[1], 7)
            x[13] = x[13] xor shift(x[9] + x[5], 9)
            x[1] = x[1] xor shift(x[13] + x[9], 13)
            x[5] = x[5] xor shift(x[1] + x[13], 18)
            x[14] = x[14] xor shift(x[10] + x[6], 7)
            x[2] = x[2] xor shift(x[14] + x[10], 9)
            x[6] = x[6] xor shift(x[2] + x[14], 13)
            x[10] = x[10] xor shift(x[6] + x[2], 18)
            x[3] = x[3] xor shift(x[15] + x[11], 7)
            x[7] = x[7] xor shift(x[3] + x[15], 9)
            x[11] = x[11] xor shift(x[7] + x[3], 13)
            x[15] = x[15] xor shift(x[11] + x[7], 18)
            x[1] = x[1] xor shift(x[0] + x[3], 7)
            x[2] = x[2] xor shift(x[1] + x[0], 9)
            x[3] = x[3] xor shift(x[2] + x[1], 13)
            x[0] = x[0] xor shift(x[3] + x[2], 18)
            x[6] = x[6] xor shift(x[5] + x[4], 7)
            x[7] = x[7] xor shift(x[6] + x[5], 9)
            x[4] = x[4] xor shift(x[7] + x[6], 13)
            x[5] = x[5] xor shift(x[4] + x[7], 18)
            x[11] = x[11] xor shift(x[10] + x[9], 7)
            x[8] = x[8] xor shift(x[11] + x[10], 9)
            x[9] = x[9] xor shift(x[8] + x[11], 13)
            x[10] = x[10] xor shift(x[9] + x[8], 18)
            x[12] = x[12] xor shift(x[15] + x[14], 7)
            x[13] = x[13] xor shift(x[12] + x[15], 9)
            x[14] = x[14] xor shift(x[13] + x[12], 13)
            x[15] = x[15] xor shift(x[14] + x[13], 18)
            i -= 2
        }

        i = 0
        while (i < 16) {
            outArray[i] = x[i] + inArray[i]
            ++i
        }

        i = 0
        while (i < 16) {
            b[i * 4] = (outArray[i] shr 0 and 0xff).toByte()
            b[i * 4 + 1] = (outArray[i] shr 8 and 0xff).toByte()
            b[i * 4 + 2] = (outArray[i] shr 16 and 0xff).toByte()
            b[i * 4 + 3] = (outArray[i] shr 24 and 0xff).toByte()
            i++
        }
    }

    private fun shift(a: Int, b: Int): Int {
        return a shl b or a.ushr(32 - b)
    }
}