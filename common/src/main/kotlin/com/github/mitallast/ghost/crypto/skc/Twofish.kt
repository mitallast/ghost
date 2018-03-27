/**
 * Twofish - Encryption and decryption with Twofish
 *
 * Copyright (c) 2018 Dirk Gerhardt
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.github.mitallast.ghost.crypto.skc

import com.github.mitallast.ghost.crypto.utils.Arrays

object Twofish {

    fun encryptECB(plainText: ByteArray, key: ByteArray, pad: Padding.SCHEME = Padding.SCHEME.NONE): ByteArray {

        var plain = plainText
        if (pad == Padding.SCHEME.PKCS7) {
            plain = Padding.pkcs7Padding(plain)
        } else {
            checkSize(plain)
        }

        return encrypt(plain, key, mode = Mode.ECB)
    }

    fun decryptECB(chipertext: ByteArray, key: ByteArray, pad: Padding.SCHEME = Padding.SCHEME.NONE): ByteArray {

        val plaintext = decrypt(chipertext, key, mode = Mode.ECB)
        if (pad == Padding.SCHEME.PKCS7) {
            return Padding.pkcs7Unpadding(plaintext)
        }
        return plaintext
    }

    fun encryptCBC(plainText: ByteArray, key: ByteArray, iv: ByteArray, pad: Padding.SCHEME = Padding.SCHEME.NONE): ByteArray {

        var plain = plainText
        if (pad == Padding.SCHEME.PKCS7) {
            plain = Padding.pkcs7Padding(plain)
        } else {
            checkSize(plain)
        }
        checkIv(iv)

        return encrypt(plain, key, iv, Mode.CBC)
    }

    fun decryptCBC(chipertext: ByteArray, key: ByteArray, iv: ByteArray, pad: Padding.SCHEME = Padding.SCHEME.NONE): ByteArray {

        checkIv(iv)
        val plaintext = decrypt(chipertext, key, iv, mode = Mode.CBC)
        if (pad == Padding.SCHEME.PKCS7) {
            return Padding.pkcs7Unpadding(plaintext)
        }
        return plaintext
    }

    private fun encrypt(plaintext: ByteArray, key: ByteArray, iv: ByteArray = ByteArray(0), mode: Mode): ByteArray {

        val keys = expandKey(key)

        val ciphertext = ByteArray(plaintext.size)
        var initvector = iv

        val rounds = if (plaintext.size % BLOCK_SIZE > 0) plaintext.size / BLOCK_SIZE + 1 else plaintext.size / BLOCK_SIZE
        for (i in 0 until rounds) {

            if(mode == Mode.CBC) {
                xorBlock(plaintext, i * BLOCK_SIZE, initvector)
            }

            val result = encryptBlock(plaintext, i * BLOCK_SIZE, keys)
            Arrays.arraycopy(result, 0, ciphertext, BLOCK_SIZE * i, BLOCK_SIZE)
            if(mode == Mode.CBC) {
                initvector = result
            }
        }
        return ciphertext
    }

    private fun decrypt(chipertext: ByteArray, key: ByteArray, iv: ByteArray = ByteArray(0), mode: Mode): ByteArray {

        val keys = expandKey(key)
        val plaintext = ByteArray(chipertext.size)
        val rounds = if (chipertext.size % BLOCK_SIZE > 0) chipertext.size / BLOCK_SIZE + 1 else chipertext.size / BLOCK_SIZE
        var initvector = iv

        for (i in 0 until rounds) {
            val result = decryptBlock(chipertext, i * BLOCK_SIZE, keys)
            Arrays.arraycopy(result, 0, plaintext, BLOCK_SIZE * i, 16)

            if(mode == Mode.CBC) {
                val tmp = chipertext.copyOfRange(i* BLOCK_SIZE, i* BLOCK_SIZE + BLOCK_SIZE)
                xorBlock(plaintext, i* BLOCK_SIZE, initvector)
                initvector = tmp.copyOf()
            }
        }

        return plaintext
    }

    private fun expandKey(key: ByteArray): IntArray {

        val blocks = key.size / 8
        val k32e = IntArray(4)
        val k32o = IntArray(4)
        val sBoxKey = IntArray(4)
        val subKeys = IntArray(40)

        var i = 0
        var j = blocks - 1
        var offset = 0

        while (i < 4 && offset < key.size) {
            k32e[i] = integerify(key, offset)
            k32o[i] = integerify(key, offset + 4)
            sBoxKey[j] = rsMsdEncode(k32e[i], k32o[i])
            i++
            j--
            offset += 8
        }

        var q = 0
        var a: Int
        var b: Int

        for (k in 0 until subKeys.size / 2) {
            a = f32(blocks, q, k32e)
            b = f32(blocks, q + SK_BUMP, k32o)
            b = b shl 8 or b.ushr(24)
            a += b
            subKeys[2 * k] = a
            a += b
            subKeys[2 * k + 1] = a shl SK_ROTL or a.ushr(32 - SK_ROTL)
            q += SK_STEP
        }

        initSbox(sBoxKey, blocks)

        return subKeys
    }

    private fun initSbox(sBoxKey: IntArray, k64Cnt: Int) {

        var b0: Int
        var b1: Int
        var b2: Int
        var b3: Int

        for (i in 0 until 256) {
            b3 = i
            b2 = b3
            b1 = b2
            b0 = b1
            when (k64Cnt and 3) {
                1 -> {
                    sBox[2 * i] = mdsMatrix[0][P[P_01][b0].toInt() and 0xFF xor b0(sBoxKey[0])]
                    sBox[2 * i + 1] = mdsMatrix[1][P[P_11][b1].toInt() and 0xFF xor b1(sBoxKey[0])]
                    sBox[0x200 + 2 * i] = mdsMatrix[2][P[P_21][b2].toInt() and 0xFF xor b2(sBoxKey[0])]
                    sBox[0x200 + 2 * i + 1] = mdsMatrix[3][P[P_31][b3].toInt() and 0xFF xor b3(sBoxKey[0])]
                }
                0 -> { // 256 bit key
                    b0 = P[P_04][b0].toInt() and 0xFF xor b0(sBoxKey[3])
                    b1 = P[P_14][b1].toInt() and 0xFF xor b1(sBoxKey[3])
                    b2 = P[P_24][b2].toInt() and 0xFF xor b2(sBoxKey[3])
                    b3 = P[P_34][b3].toInt() and 0xFF xor b3(sBoxKey[3])
                    b0 = P[P_03][b0].toInt() and 0xFF xor b0(sBoxKey[2])
                    b1 = P[P_13][b1].toInt() and 0xFF xor b1(sBoxKey[2])
                    b2 = P[P_23][b2].toInt() and 0xFF xor b2(sBoxKey[2])
                    b3 = P[P_33][b3].toInt() and 0xFF xor b3(sBoxKey[2])
                    fillbox(i, b0, sBoxKey[1], sBoxKey[0], b1, b2, b3)
                }
                3 -> { // 192 bit key
                    b0 = P[P_03][b0].toInt() and 0xFF xor b0(sBoxKey[2])
                    b1 = P[P_13][b1].toInt() and 0xFF xor b1(sBoxKey[2])
                    b2 = P[P_23][b2].toInt() and 0xFF xor b2(sBoxKey[2])
                    b3 = P[P_33][b3].toInt() and 0xFF xor b3(sBoxKey[2])
                    fillbox(i, b0, sBoxKey[1], sBoxKey[0], b1, b2, b3)
                }
                2 -> { // 128 bit key
                    fillbox(i, b0, sBoxKey[1], sBoxKey[0], b1, b2, b3)
                }
            }
        }
    }

    private fun fillbox(i: Int, b0: Int, k1: Int, k0: Int, b1: Int, b2: Int, b3: Int) {
        sBox[2 * i] = mdsMatrix[0][P[P_01][P[P_02][b0].toInt() and 0xFF xor b0(k1)].toInt() and 0xFF xor b0(k0)]
        sBox[2 * i + 1] = mdsMatrix[1][P[P_11][P[P_12][b1].toInt() and 0xFF xor b1(k1)].toInt() and 0xFF xor b1(k0)]
        sBox[0x200 + 2 * i] = mdsMatrix[2][P[P_21][P[P_22][b2].toInt() and 0xFF xor b2(k1)].toInt() and 0xFF xor b2(k0)]
        sBox[0x200 + 2 * i + 1] = mdsMatrix[3][P[P_31][P[P_32][b3].toInt() and 0xFF xor b3(k1)].toInt() and 0xFF xor b3(k0)]
    }

    private fun f32(k64Cnt: Int, x: Int, k32: IntArray): Int {
        var b0 = b0(x)
        var b1 = b1(x)
        var b2 = b2(x)
        var b3 = b3(x)
        val k0 = k32[0]
        val k1 = k32[1]
        val k2 = k32[2]
        val k3 = k32[3]

        var result = 0
        when (k64Cnt and 3) {
            1 -> result = mdsMatrix[0][P[P_01][b0].toInt() and 0xFF xor b0(k0)] xor
                    mdsMatrix[1][P[P_11][b1].toInt() and 0xFF xor b1(k0)] xor
                    mdsMatrix[2][P[P_21][b2].toInt() and 0xFF xor b2(k0)] xor
                    mdsMatrix[3][P[P_31][b3].toInt() and 0xFF xor b3(k0)]
            0 -> {
                b0 = P[P_04][b0].toInt() and 0xFF xor b0(k3)
                b1 = P[P_14][b1].toInt() and 0xFF xor b1(k3)
                b2 = P[P_24][b2].toInt() and 0xFF xor b2(k3)
                b3 = P[P_34][b3].toInt() and 0xFF xor b3(k3)
                b0 = P[P_03][b0].toInt() and 0xFF xor b0(k2)
                b1 = P[P_13][b1].toInt() and 0xFF xor b1(k2)
                b2 = P[P_23][b2].toInt() and 0xFF xor b2(k2)
                b3 = P[P_33][b3].toInt() and 0xFF xor b3(k2)
                result = f32result(b0, k1, k0, b1, b2, b3)
            }
            3 -> {
                b0 = P[P_03][b0].toInt() and 0xFF xor b0(k2)
                b1 = P[P_13][b1].toInt() and 0xFF xor b1(k2)
                b2 = P[P_23][b2].toInt() and 0xFF xor b2(k2)
                b3 = P[P_33][b3].toInt() and 0xFF xor b3(k2)
                result = f32result(b0, k1, k0, b1, b2, b3)
            }
            2 -> result = f32result(b0, k1, k0, b1, b2, b3)
        }
        return result
    }

    private fun f32result(b0: Int, k1: Int, k0: Int, b1: Int, b2: Int, b3: Int): Int {
        return mdsMatrix[0][P[P_01][P[P_02][b0].toInt() and 0xFF xor b0(k1)].toInt() and 0xFF xor b0(k0)] xor
                mdsMatrix[1][P[P_11][P[P_12][b1].toInt() and 0xFF xor b1(k1)].toInt() and 0xFF xor b1(k0)] xor
                mdsMatrix[2][P[P_21][P[P_22][b2].toInt() and 0xFF xor b2(k1)].toInt() and 0xFF xor b2(k0)] xor
                mdsMatrix[3][P[P_31][P[P_32][b3].toInt() and 0xFF xor b3(k1)].toInt() and 0xFF xor b3(k0)]
    }

    private fun encryptBlock(input: ByteArray, offset: Int, sKey: IntArray): ByteArray {

        val state = intArrayOf(
                integerify(input, offset) xor sKey[0],
                integerify(input, offset + 4) xor sKey[1],
                integerify(input, offset + 8) xor sKey[2],
                integerify(input, offset + 12) xor sKey[3]
        )

        var t0: Int
        var t1: Int
        var k = ROUND_SUBKEYS
        for (round in 0 until 16 step 2) {
            t0 = fe32(sBox, state[0], 0)
            t1 = fe32(sBox, state[1], 3)
            state[2] = state[2] xor t0 + t1 + sKey[k++]
            state[2] = state[2].ushr(1) or (state[2] shl 31)
            state[3] = state[3] shl 1 or state[3].ushr(31)
            state[3] = state[3] xor t0 + 2 * t1 + sKey[k++]

            t0 = fe32(sBox, state[2], 0)
            t1 = fe32(sBox, state[3], 3)
            state[0] = state[0] xor t0 + t1 + sKey[k++]
            state[0] = state[0].ushr(1) or (state[0] shl 31)
            state[1] = state[1] shl 1 or state[1].ushr(31)
            state[1] = state[1] xor t0 + 2 * t1 + sKey[k++]
        }
        state[2] = state[2] xor sKey[4]
        state[3] = state[3] xor sKey[5]
        state[0] = state[0] xor sKey[6]
        state[1] = state[1] xor sKey[7]

        return getBytes(intArrayOf(state[2], state[3], state[0], state[1]))
    }

    private fun decryptBlock(input: ByteArray, offset: Int, sKey: IntArray): ByteArray {

        val state = intArrayOf(
                integerify(input, offset + 8) xor sKey[6],
                integerify(input, offset + 12) xor sKey[7],
                integerify(input, offset) xor sKey[4],
                integerify(input, offset + 4) xor sKey[5]
        )

        var k = ROUND_SUBKEYS + 2 * ROUNDS - 1
        var t0: Int
        var t1: Int

        for (round in 0 until 16 step 2) {
            t0 = fe32(sBox, state[2], 0)
            t1 = fe32(sBox, state[3], 3)
            state[1] = state[1] xor t0 + 2 * t1 + sKey[k--]
            state[1] = state[1].ushr(1) or (state[1] shl 31)
            state[0] = state[0] shl 1 or state[0].ushr(31)
            state[0] = state[0] xor t0 + t1 + sKey[k--]

            t0 = fe32(sBox, state[0], 0)
            t1 = fe32(sBox, state[1], 3)
            state[3] = state[3] xor t0 + 2 * t1 + sKey[k--]
            state[3] = state[3].ushr(1) or (state[3] shl 31)
            state[2] = state[2] shl 1 or state[2].ushr(31)
            state[2] = state[2] xor t0 + t1 + sKey[k--]
        }
        state[0] = state[0] xor sKey[0]
        state[1] = state[1] xor sKey[1]
        state[2] = state[2] xor sKey[2]
        state[3] = state[3] xor sKey[3]

        return getBytes(state)
    }

    private fun integerify(b: ByteArray, num: Int): Int {
        var n = b[num].toInt() and 0xff shl 0
        n = n or (b[num + 1].toInt() and 0xff shl 8)
        n = n or (b[num + 2].toInt() and 0xff shl 16)
        n = n or (b[num + 3].toInt() and 0xff shl 24)
        return n
    }

    private fun fe32(sBox: IntArray, x: Int, r: Int): Int {
        return sBox[2 * getB(x, r)] xor sBox[2 * getB(x, r + 1) + 1] xor
                sBox[0x200 + 2 * getB(x, r + 2)] xor sBox[0x200 + 2 * getB(x, r + 3) + 1]
    }

    private fun getBytes(state: IntArray): ByteArray {
        return byteArrayOf(
                state[0].toByte(),
                state[0].ushr(8).toByte(),
                state[0].ushr(16).toByte(),
                state[0].ushr(24).toByte(),
                state[1].toByte(),
                state[1].ushr(8).toByte(),
                state[1].ushr(16).toByte(),
                state[1].ushr(24).toByte(),
                state[2].toByte(),
                state[2].ushr(8).toByte(),
                state[2].ushr(16).toByte(),
                state[2].ushr(24).toByte(),
                state[3].toByte(),
                state[3].ushr(8).toByte(),
                state[3].ushr(16).toByte(),
                state[3].ushr(24).toByte())
    }

    private fun xorBlock(block: ByteArray, offset: Int, iv: ByteArray) {
        for (i in iv.indices) {
            block[i+offset] = (block[i+offset].toInt() xor iv[i].toInt()).toByte()
        }
    }

    private fun b0(x: Int): Int {
        return x and 0xFF
    }

    private fun b1(x: Int): Int {
        return x.ushr(8) and 0xFF
    }

    private fun b2(x: Int): Int {
        return x.ushr(16) and 0xFF
    }

    private fun b3(x: Int): Int {
        return x.ushr(24) and 0xFF
    }

    private fun rsMsdEncode(k0: Int, k1: Int): Int {
        var r = k1
        for (i in 0..3) {
            r = rsRem(r)
        }
        r = r xor k0
        for (i in 0..3) {
            r = rsRem(r)
        }
        return r
    }

    private fun rsRem(x: Int): Int {
        val b = x.ushr(24) and 0xFF
        val g2 = b shl 1 xor if (b and 0x80 != 0) RS_GF_FDBK else 0 and 0xFF
        val g3 = b.ushr(1) xor (if (b and 0x01 != 0) RS_GF_FDBK.ushr(1) else 0) xor g2
        return x shl 8 xor (g3 shl 24) xor (g2 shl 16) xor (g3 shl 8) xor b
    }

    private fun getB(x: Int, n: Int): Int {
        var result = 0
        when (n % 4) {
            0 -> result = b0(x)
            1 -> result = b1(x)
            2 -> result = b2(x)
            3 -> result = b3(x)
        }
        return result
    }

    private fun checkSize(plain: ByteArray) {
        if (plain.size % BLOCK_SIZE != 0) {
            throw IllegalArgumentException("plainText size is not a multiple of 128 bit: " + plain.size * 8)
        }
    }

    private fun checkIv(iv: ByteArray) {
        if (iv.isNotEmpty() && iv.size != BLOCK_SIZE) throw IllegalArgumentException("The initialization vector should have a size of 128 bit: " + iv.size * 8)
    }

    internal fun checkKey(key: ByteArray) {
        if (key.size < 8 || key.size > 32 || key.size % 8 != 0) throw IllegalArgumentException("key size is not supported")
    }

    private var sBox = IntArray(1024)

    private const val RS_GF_FDBK = 0x14D
    private const val SK_STEP = 0x02020202
    private const val SK_BUMP = 0x01010101
    private const val SK_ROTL = 9
    private const val BLOCK_SIZE = 16
    private const val ROUNDS = 16
    private const val ROUND_SUBKEYS = 8
    
    private const val P_01 = 0
    private const val P_02 = 0
    private const val P_03 = P_01 xor 1
    private const val P_04 = 1
    
    private const val P_11 = 0
    private const val P_12 = 1
    private const val P_13 = P_11 xor 1
    private const val P_14 = 0
    
    private const val P_21 = 1
    private const val P_22 = 0
    private const val P_23 = P_21 xor 1
    private const val P_24 = 0
    
    private const val P_31 = 1
    private const val P_32 = 1
    private const val P_33 = P_31 xor 1
    private const val P_34 = 1

    private val P = arrayOf(
        byteArrayOf(
            0xA9.toByte(), 0x67.toByte(), 0xB3.toByte(), 0xE8.toByte(),
            0x04.toByte(), 0xFD.toByte(), 0xA3.toByte(), 0x76.toByte(),
            0x9A.toByte(), 0x92.toByte(), 0x80.toByte(), 0x78.toByte(),
            0xE4.toByte(), 0xDD.toByte(), 0xD1.toByte(), 0x38.toByte(),
            0x0D.toByte(), 0xC6.toByte(), 0x35.toByte(), 0x98.toByte(),
            0x18.toByte(), 0xF7.toByte(), 0xEC.toByte(), 0x6C.toByte(),
            0x43.toByte(), 0x75.toByte(), 0x37.toByte(), 0x26.toByte(),
            0xFA.toByte(), 0x13.toByte(), 0x94.toByte(), 0x48.toByte(),
            0xF2.toByte(), 0xD0.toByte(), 0x8B.toByte(), 0x30.toByte(),
            0x84.toByte(), 0x54.toByte(), 0xDF.toByte(), 0x23.toByte(),
            0x19.toByte(), 0x5B.toByte(), 0x3D.toByte(), 0x59.toByte(),
            0xF3.toByte(), 0xAE.toByte(), 0xA2.toByte(), 0x82.toByte(),
            0x63.toByte(), 0x01.toByte(), 0x83.toByte(), 0x2E.toByte(),
            0xD9.toByte(), 0x51.toByte(), 0x9B.toByte(), 0x7C.toByte(),
            0xA6.toByte(), 0xEB.toByte(), 0xA5.toByte(), 0xBE.toByte(),
            0x16.toByte(), 0x0C.toByte(), 0xE3.toByte(), 0x61.toByte(),
            0xC0.toByte(), 0x8C.toByte(), 0x3A.toByte(), 0xF5.toByte(),
            0x73.toByte(), 0x2C.toByte(), 0x25.toByte(), 0x0B.toByte(),
            0xBB.toByte(), 0x4E.toByte(), 0x89.toByte(), 0x6B.toByte(),
            0x53.toByte(), 0x6A.toByte(), 0xB4.toByte(), 0xF1.toByte(),
            0xE1.toByte(), 0xE6.toByte(), 0xBD.toByte(), 0x45.toByte(),
            0xE2.toByte(), 0xF4.toByte(), 0xB6.toByte(), 0x66.toByte(),
            0xCC.toByte(), 0x95.toByte(), 0x03.toByte(), 0x56.toByte(),
            0xD4.toByte(), 0x1C.toByte(), 0x1E.toByte(), 0xD7.toByte(),
            0xFB.toByte(), 0xC3.toByte(), 0x8E.toByte(), 0xB5.toByte(),
            0xE9.toByte(), 0xCF.toByte(), 0xBF.toByte(), 0xBA.toByte(),
            0xEA.toByte(), 0x77.toByte(), 0x39.toByte(), 0xAF.toByte(),
            0x33.toByte(), 0xC9.toByte(), 0x62.toByte(), 0x71.toByte(),
            0x81.toByte(), 0x79.toByte(), 0x09.toByte(), 0xAD.toByte(),
            0x24.toByte(), 0xCD.toByte(), 0xF9.toByte(), 0xD8.toByte(),
            0xE5.toByte(), 0xC5.toByte(), 0xB9.toByte(), 0x4D.toByte(),
            0x44.toByte(), 0x08.toByte(), 0x86.toByte(), 0xE7.toByte(),
            0xA1.toByte(), 0x1D.toByte(), 0xAA.toByte(), 0xED.toByte(),
            0x06.toByte(), 0x70.toByte(), 0xB2.toByte(), 0xD2.toByte(),
            0x41.toByte(), 0x7B.toByte(), 0xA0.toByte(), 0x11.toByte(),
            0x31.toByte(), 0xC2.toByte(), 0x27.toByte(), 0x90.toByte(),
            0x20.toByte(), 0xF6.toByte(), 0x60.toByte(), 0xFF.toByte(),
            0x96.toByte(), 0x5C.toByte(), 0xB1.toByte(), 0xAB.toByte(),
            0x9E.toByte(), 0x9C.toByte(), 0x52.toByte(), 0x1B.toByte(),
            0x5F.toByte(), 0x93.toByte(), 0x0A.toByte(), 0xEF.toByte(),
            0x91.toByte(), 0x85.toByte(), 0x49.toByte(), 0xEE.toByte(),
            0x2D.toByte(), 0x4F.toByte(), 0x8F.toByte(), 0x3B.toByte(),
            0x47.toByte(), 0x87.toByte(), 0x6D.toByte(), 0x46.toByte(),
            0xD6.toByte(), 0x3E.toByte(), 0x69.toByte(), 0x64.toByte(),
            0x2A.toByte(), 0xCE.toByte(), 0xCB.toByte(), 0x2F.toByte(),
            0xFC.toByte(), 0x97.toByte(), 0x05.toByte(), 0x7A.toByte(),
            0xAC.toByte(), 0x7F.toByte(), 0xD5.toByte(), 0x1A.toByte(),
            0x4B.toByte(), 0x0E.toByte(), 0xA7.toByte(), 0x5A.toByte(),
            0x28.toByte(), 0x14.toByte(), 0x3F.toByte(), 0x29.toByte(),
            0x88.toByte(), 0x3C.toByte(), 0x4C.toByte(), 0x02.toByte(),
            0xB8.toByte(), 0xDA.toByte(), 0xB0.toByte(), 0x17.toByte(),
            0x55.toByte(), 0x1F.toByte(), 0x8A.toByte(), 0x7D.toByte(),
            0x57.toByte(), 0xC7.toByte(), 0x8D.toByte(), 0x74.toByte(),
            0xB7.toByte(), 0xC4.toByte(), 0x9F.toByte(), 0x72.toByte(),
            0x7E.toByte(), 0x15.toByte(), 0x22.toByte(), 0x12.toByte(),
            0x58.toByte(), 0x07.toByte(), 0x99.toByte(), 0x34.toByte(),
            0x6E.toByte(), 0x50.toByte(), 0xDE.toByte(), 0x68.toByte(),
            0x65.toByte(), 0xBC.toByte(), 0xDB.toByte(), 0xF8.toByte(),
            0xC8.toByte(), 0xA8.toByte(), 0x2B.toByte(), 0x40.toByte(),
            0xDC.toByte(), 0xFE.toByte(), 0x32.toByte(), 0xA4.toByte(),
            0xCA.toByte(), 0x10.toByte(), 0x21.toByte(), 0xF0.toByte(),
            0xD3.toByte(), 0x5D.toByte(), 0x0F.toByte(), 0x00.toByte(),
            0x6F.toByte(), 0x9D.toByte(), 0x36.toByte(), 0x42.toByte(),
            0x4A.toByte(), 0x5E.toByte(), 0xC1.toByte(), 0xE0.toByte()
        ), byteArrayOf (
            0x75.toByte(), 0xF3.toByte(), 0xC6.toByte(), 0xF4.toByte(),
            0xDB.toByte(), 0x7B.toByte(), 0xFB.toByte(), 0xC8.toByte(),
            0x4A.toByte(), 0xD3.toByte(), 0xE6.toByte(), 0x6B.toByte(),
            0x45.toByte(), 0x7D.toByte(), 0xE8.toByte(), 0x4B.toByte(),
            0xD6.toByte(), 0x32.toByte(), 0xD8.toByte(), 0xFD.toByte(),
            0x37.toByte(), 0x71.toByte(), 0xF1.toByte(), 0xE1.toByte(),
            0x30.toByte(), 0x0F.toByte(), 0xF8.toByte(), 0x1B.toByte(),
            0x87.toByte(), 0xFA.toByte(), 0x06.toByte(), 0x3F.toByte(),
            0x5E.toByte(), 0xBA.toByte(), 0xAE.toByte(), 0x5B.toByte(),
            0x8A.toByte(), 0x00.toByte(), 0xBC.toByte(), 0x9D.toByte(),
            0x6D.toByte(), 0xC1.toByte(), 0xB1.toByte(), 0x0E.toByte(),
            0x80.toByte(), 0x5D.toByte(), 0xD2.toByte(), 0xD5.toByte(),
            0xA0.toByte(), 0x84.toByte(), 0x07.toByte(), 0x14.toByte(),
            0xB5.toByte(), 0x90.toByte(), 0x2C.toByte(), 0xA3.toByte(),
            0xB2.toByte(), 0x73.toByte(), 0x4C.toByte(), 0x54.toByte(),
            0x92.toByte(), 0x74.toByte(), 0x36.toByte(), 0x51.toByte(),
            0x38.toByte(), 0xB0.toByte(), 0xBD.toByte(), 0x5A.toByte(),
            0xFC.toByte(), 0x60.toByte(), 0x62.toByte(), 0x96.toByte(),
            0x6C.toByte(), 0x42.toByte(), 0xF7.toByte(), 0x10.toByte(),
            0x7C.toByte(), 0x28.toByte(), 0x27.toByte(), 0x8C.toByte(),
            0x13.toByte(), 0x95.toByte(), 0x9C.toByte(), 0xC7.toByte(),
            0x24.toByte(), 0x46.toByte(), 0x3B.toByte(), 0x70.toByte(),
            0xCA.toByte(), 0xE3.toByte(), 0x85.toByte(), 0xCB.toByte(),
            0x11.toByte(), 0xD0.toByte(), 0x93.toByte(), 0xB8.toByte(),
            0xA6.toByte(), 0x83.toByte(), 0x20.toByte(), 0xFF.toByte(),
            0x9F.toByte(), 0x77.toByte(), 0xC3.toByte(), 0xCC.toByte(),
            0x03.toByte(), 0x6F.toByte(), 0x08.toByte(), 0xBF.toByte(),
            0x40.toByte(), 0xE7.toByte(), 0x2B.toByte(), 0xE2.toByte(),
            0x79.toByte(), 0x0C.toByte(), 0xAA.toByte(), 0x82.toByte(),
            0x41.toByte(), 0x3A.toByte(), 0xEA.toByte(), 0xB9.toByte(),
            0xE4.toByte(), 0x9A.toByte(), 0xA4.toByte(), 0x97.toByte(),
            0x7E.toByte(), 0xDA.toByte(), 0x7A.toByte(), 0x17.toByte(),
            0x66.toByte(), 0x94.toByte(), 0xA1.toByte(), 0x1D.toByte(),
            0x3D.toByte(), 0xF0.toByte(), 0xDE.toByte(), 0xB3.toByte(),
            0x0B.toByte(), 0x72.toByte(), 0xA7.toByte(), 0x1C.toByte(),
            0xEF.toByte(), 0xD1.toByte(), 0x53.toByte(), 0x3E.toByte(),
            0x8F.toByte(), 0x33.toByte(), 0x26.toByte(), 0x5F.toByte(),
            0xEC.toByte(), 0x76.toByte(), 0x2A.toByte(), 0x49.toByte(),
            0x81.toByte(), 0x88.toByte(), 0xEE.toByte(), 0x21.toByte(),
            0xC4.toByte(), 0x1A.toByte(), 0xEB.toByte(), 0xD9.toByte(),
            0xC5.toByte(), 0x39.toByte(), 0x99.toByte(), 0xCD.toByte(),
            0xAD.toByte(), 0x31.toByte(), 0x8B.toByte(), 0x01.toByte(),
            0x18.toByte(), 0x23.toByte(), 0xDD.toByte(), 0x1F.toByte(),
            0x4E.toByte(), 0x2D.toByte(), 0xF9.toByte(), 0x48.toByte(),
            0x4F.toByte(), 0xF2.toByte(), 0x65.toByte(), 0x8E.toByte(),
            0x78.toByte(), 0x5C.toByte(), 0x58.toByte(), 0x19.toByte(),
            0x8D.toByte(), 0xE5.toByte(), 0x98.toByte(), 0x57.toByte(),
            0x67.toByte(), 0x7F.toByte(), 0x05.toByte(), 0x64.toByte(),
            0xAF.toByte(), 0x63.toByte(), 0xB6.toByte(), 0xFE.toByte(),
            0xF5.toByte(), 0xB7.toByte(), 0x3C.toByte(), 0xA5.toByte(),
            0xCE.toByte(), 0xE9.toByte(), 0x68.toByte(), 0x44.toByte(),
            0xE0.toByte(), 0x4D.toByte(), 0x43.toByte(), 0x69.toByte(),
            0x29.toByte(), 0x2E.toByte(), 0xAC.toByte(), 0x15.toByte(),
            0x59.toByte(), 0xA8.toByte(), 0x0A.toByte(), 0x9E.toByte(),
            0x6E.toByte(), 0x47.toByte(), 0xDF.toByte(), 0x34.toByte(),
            0x35.toByte(), 0x6A.toByte(), 0xCF.toByte(), 0xDC.toByte(),
            0x22.toByte(), 0xC9.toByte(), 0xC0.toByte(), 0x9B.toByte(),
            0x89.toByte(), 0xD4.toByte(), 0xED.toByte(), 0xAB.toByte(),
            0x12.toByte(), 0xA2.toByte(), 0x0D.toByte(), 0x52.toByte(),
            0xBB.toByte(), 0x02.toByte(), 0x2F.toByte(), 0xA9.toByte(),
            0xD7.toByte(), 0x61.toByte(), 0x1E.toByte(), 0xB4.toByte(),
            0x50.toByte(), 0x04.toByte(), 0xF6.toByte(), 0xC2.toByte(),
            0x16.toByte(), 0x25.toByte(), 0x86.toByte(), 0x56.toByte(),
            0x55.toByte(), 0x09.toByte(), 0xBE.toByte(), 0x91.toByte()
        )
    )
    
    private val mdsMatrix = arrayOf(
        intArrayOf(
            -1128517003, -320069133, 538985414, -1280062988, -623246373, 33721211, -488494085, -1633748280, -909513654, -724301357,
            404253670, 505323371, -1734865339, -1296942979, -1499016472, 640071499, 1010587606, -1819047374, -2105348392, 1381144829,
            2071712823, -1145358479, 1532729329, 1195869153, 606354480, 1364320783, -1162164488, 1246425883, -1077983097, 218984698,
            -1330597114, 1970658879, -757924514, 2105352378, 1717973422, 976921435, 1499012234, 0, -842165316, 437969053,
            -1364317075, 2139073473, 724289457, -1094797042, -522149760, -1970663331, 993743570, 1684323029, -656897888, -404249212,
            1600120839, 454758676, 741130933, -50547568, 825304876, -2139069021, 1936927410, 202146163, 2037997388, 1802191188,
            1263207058, 1397975412, -1802203338, -2088558767, 707409464, -993747792, 572704957, -707397542, -1111636996, 1212708960,
            -12702, 1280051094, 1094809452, -943200702, -336911113, 471602192, 1566401404, 909517352, 1734852647, -370561140,
            1145370899, 336915093, -168445028, -808511289, 1061104932, -1061100730, 1920129851, 1414818928, 690572490, -252693021,
            134807173, -960096309, -202158319, -1936923440, -1532733037, -892692808, 1751661478, -1195881085, 943204384, -437965057,
            -1381149025, 185304183, -926409277, -1717960756, 1482222851, 421108335, 235801096, -1785364801, 1886408768, -134795033,
            1852755755, 522153698, -1246413447, 151588620, 1633760426, 1465325186, -1616966847, -1650622406, 286352618, 623234489,
            -1347428892, 1162152090, -538997340, -1549575017, -353708674, 892688602, -303181702, 1128528919, -117912730, -67391084,
            926405537, -84262883, -1027446723, -1263219472, 842161630, -1667468877, 1448535819, -471606670, -2021171033, 353704732,
            -101106961, 1667481553, 875866451, -1701149378, -1313783153, 2088554803, -2004313306, 1027450463, -1583228948, -454762634,
            -2122214358, -1852767927, 252705665, -286348664, 370565614, -673746143, -1751648828, -1515870182, -16891925, 1835906521,
            2021174981, -976917191, 488498585, 1987486925, 1044307117, -875862223, -1229568117, -269526271, 303177240, 1616954659,
            1785376989, 1296954911, -825300658, -555844563, 1431674361, 2122209864, 555856463, 50559730, -1600117147, 1583225230,
            1515873912, 1701137244, 1650609752, -33733351, 101119117, 1077970661, -218972520, 859024471, 387420263, 84250239,
            -387424763, 1330609508, -1987482961, 269522275, 1953771446, 168457726, 1549570805, -1684310857, 757936956, 808507045,
            774785486, 1229556201, 1179021928, 2004309316, -1465329440, -1768553395, 673758531, -1448531607, -640059095, -2038001362,
            -774797396, -185316843, -1920133799, -690584920, -1179010038, 1111625118, -151600786, 791656519, -572717345, 589510964,
            -859020747, -235813782, -1044311345, -2054820900, -1886413278, 1903272393, -1869549376, -1431678053, 16904585, -1953766956,
            1313770733, -1903267925, -1414815214, 1869561506, -421112819, -606342574, -1835893829, -1212697086, 1768540719, 960092585,
            -741143337, -1482218655, -1566397154, -1010591308, 1819034704, 117900548, 67403766, 656885442, -1397971178, -791644635,
            1347425158, -589498538, -2071717291, -505327351, 2054825406, 320073617
        ), intArrayOf(
            -1445381831, 1737496343, -1284399972, -388847962, 67438343, -40349102, -1553629056, 1994384612, -1710734011, -1845343413,
            -2136940320, 2019973722, -455233617, -575640982, -775986333, 943073834, 223667942, -968679392, 895667404, -1732316430,
            404623890, -148575253, -321412703, 1819754817, 1136470056, 1966259388, 936672123, 647727240, -93319923, 335103044,
            -1800274949, 1213890174, -226884861, -790328180, -1958234442, 809247780, -2069501977, 1413573483, -553198115, 600137824,
            424017405, 1537423930, 1030275778, 1494584717, -215880468, -1372494234, -1572966545, -2112465065, 1670713360, 22802415,
            -2092058440, 781289094, -642421395, 1361019779, -1689015638, 2086886749, -1506056088, -348127490, -1512689616, -1104840070,
            380087468, 202311945, -483004176, 1629726631, -1057976176, -1934628375, 981507485, -174957476, 1937837068, 740766001,
            628543696, 199710294, -1149529454, 1323945678, -1980694271, 1805590046, 1403597876, 1791291889, -1264991293, -241738917,
            -511490233, -429189096, -1110957534, 1158584472, -496099553, -188107853, -1238403980, 1724643576, -855664231, -1779821548,
            65886296, 1459084508, -723416181, 471536917, 514695842, -687025197, -81009950, -1021458232, -1910940066, -1245565908,
            -376878775, -820854335, -1082223211, -1172275843, -362540783, 2005142283, 963495365, -1351972471, 869366908, -912166543,
            1657733119, 1899477947, -2114253041, 2034087349, 156361185, -1378075074, 606945087, -844859786, -107129515, -655457662,
            -444186560, -978421640, -1177737947, 1292146326, 1146451831, 134876686, -2045554608, -416221193, -1579993289, 490797818,
            -1439407775, -309572018, 112439472, 1886147668, -1305840781, -766362821, 1091280799, 2072707586, -1601644328, 290452467,
            828885963, -1035589849, 666920807, -1867186948, 539506744, -159448060, 1618495560, -13703707, -1777906612, 1548445029,
            -1312347349, -1418752370, -1643298238, -1665403403, 1391647707, 468929098, 1604730173, -1822841692, 180140473, -281347591,
            -1846602989, -2046949368, 1224839569, -295627242, 763158238, 1337073953, -1891454543, 1004237426, 1203253039, -2025275457,
            1831644846, 1189331136, -698926020, 1048943258, 1764338089, 1685933903, 714375553, -834064850, -887634234, 801794409,
            -54280771, -1755536477, 90106088, 2060512749, -1400385071, 2140013829, -709204892, 447260069, 1270294054, 247054014,
            -1486846073, 1526257109, 673330742, 336665371, 1071543669, 695851481, -2002063634, 1009986861, 1281325433, 45529015,
            -1198077238, -631753419, -1331903292, 402408259, 1427801220, 536235341, -1977853607, 2100867762, 1470903091, -954675249,
            -1913387514, 1953059667, -1217094757, -990537833, -1621709395, 1926947811, 2127948522, 357233908, 580816783, 312650667,
            1481532002, 132669279, -1713038051, 876159779, 1858205430, 1346661484, -564317646, 1752319558, 1697030304, -1131164211,
            -620504358, -121193798, -923099490, -1467820330, 735014510, 1079013488, -588544635, -25884150, 847942547, -1534205985,
            -900978391, 269753372, 561240023, -255019852, -754330412, 1561365130, 266490193, 0, 1872369945, -1646257638,
            915379348, 1122420679, 1257032137, 1593692882, -1045725313, -522671960
        ), intArrayOf(
            -1133134798, -319558623, 549855299, -1275808823, -623126013, 41616011, -486809045, -1631019270, -917845524, -724315127,
            417732715, 510336671, -1740269554, -1300385224, -1494702382, 642459319, 1020673111, -1825401974, -2099739922, 1392333464,
            2067233748, -1150174409, 1542544279, 1205946243, 607134780, 1359958498, -1158104378, 1243302643, -1081622712, 234491248,
            -1341738829, 1967093214, -765537539, 2109373728, 1722705457, 979057315, 1502239004, 0, -843264621, 446503648,
            -1368543700, 2143387563, 733031367, -1106329927, -528424800, -1973581296, 1003633490, 1691706554, -660547448, -410720347,
            1594318824, 454302481, 750070978, -57606988, 824979751, -2136768411, 1941074730, 208866433, 2035054943, 1800694593,
            1267878658, 1400132457, -1808362353, -2091810017, 708323894, -995048292, 582820552, -715467272, -1107509821, 1214269560,
            -10289202, 1284918279, 1097613687, -951924762, -336073948, 470817812, 1568431459, 908604962, 1730635712, -376641105,
            1142113529, 345314538, -174262853, -808988904, 1059340077, -1069104925, 1916498651, 1416647788, 701114700, -253497291,
            142936318, -959724009, -216927409, -1932489500, -1533828007, -893859178, 1755736123, -1199327155, 941635624, -436214482,
            -1382044330, 192351108, -926693347, -1714644481, 1476614381, 426711450, 235408906, -1782606466, 1883271248, -135792848,
            1848340175, 534912878, -1250314947, 151783695, 1638555956, 1468159766, -1623089397, -1657102976, 300552548, 632890829,
            -1343967267, 1167738120, -542842995, -1550343332, -360781099, 903492952, -310710832, 1125598204, -127469365, -74122319,
            933312467, -98698688, -1036139928, -1259293492, 853422685, -1665950607, 1443583719, -479009830, -2019063968, 354161947,
            -101713606, 1674666943, 877868201, -1707173243, -1315983038, 2083749073, -2010740581, 1029651878, -1578327593, -461970209,
            -2127920748, -1857449727, 260116475, -293015894, 384702049, -685648013, -1748723723, -1524980312, -18088385, 1842965941,
            2026207406, -986069651, 496573925, 1993176740, 1051541212, -885929113, -1232357817, -285085861, 303567390, 1612931269,
            1792895664, 1293897206, -833696023, -567419268, 1442403741, 2118680154, 558834098, 66192250, -1603952602, 1586388505,
            1517836902, 1700554059, 1649959502, -48628411, 109905652, 1088766086, -224857410, 861352876, 392632208, 92210574,
            -402266018, 1331974013, -1984984726, 274927765, 1958114351, 184420981, 1559583890, -1682465932, 758918451, 816132310,
            785264201, 1240025481, 1181238898, 2000975701, -1461671720, -1773300220, 675489981, -1452693207, -651568775, -2043771247,
            -777203321, -199887798, -1923511019, -693578110, -1190479428, 1117667853, -160500031, 793194424, -572531450, 590619449,
            -868889502, -244649532, -1043349230, -2049145365, -1893560418, 1909027233, -1866428176, -1432638893, 25756145, -1949004831,
            1324174988, -1901359505, -1424839774, 1872916286, -435296684, -615326734, -1833201029, -1224558666, 1764714954, 967391705,
            -740830452, -1486772445, -1575050579, -1011563623, 1817209924, 117704453, 83231871, 667035462, -1407800153, -802828170,
            1350979603, -598287113, -2074770406, -519446191, 2059303461, 328274927
        ), intArrayOf(
            -650532391, -1877514352, 1906094961, -760813358, 84345861, -1739391592, 1702929253, -538675489, 138779144, 38507010,
            -1595899744, 1717205094, -575675171, -1335173712, -1083977281, 908736566, 1424362836, 1126221379, 1657550178, -1091397442,
            504502302, 619444004, -677253929, 2000776311, -1121434691, 851211570, -730122284, -1685576037, 1879964272, -112978951,
            -1308912463, 1518225498, 2047079034, -460533532, 1203145543, 1009004604, -1511553883, 1097552961, 115203846, -983555131,
            1174214981, -1556456541, 1757560168, 361584917, 569176865, 828812849, 1047503422, 374833686, -1794088043, 1542390107,
            1303937869, -1853477231, -1251092043, 528699679, 1403689811, 1667071075, 996714043, 1073670975, -701454890, 628801061,
            -1481894233, 252251151, 904979253, 598171939, -258948880, -1343648593, -2137179520, -1839401582, -2129890431, 657533991,
            1993352566, -413791257, 2073213819, -372355351, -251557391, -1625396321, -1456188503, -990811452, -1715227495, -1755582057,
            -2092441213, 1796793963, -937247288, 244860174, 1847583342, -910953271, 796177967, -872913205, -6697729, -367749654,
            -312998931, -136554761, -510929695, 454368283, -1381884243, 215209740, 736295723, 499696413, 425627161, -1037257278,
            -1991644791, 314691346, 2123743102, 545110560, 1678895716, -2079623292, 1841641837, 1787408234, -780389423, -1586378335,
            -822123826, 935031095, -82869765, 1035303229, 1373702481, -599872036, 759112749, -1535717980, -1655309923, -293414674,
            -2042567290, -1367816786, -853165619, 76958980, 1433879637, 168691722, 324044307, 821552944, -751328813, 1090133312,
            878815796, -1940984436, -1280309581, 1817473132, 712225322, 1379652178, 194986251, -1962771573, -1999069048, 1341329743,
            1741369703, 1177010758, -1066981440, -1258516300, 674766888, 2131031679, 2018009208, 786825006, 122459655, 1264933963,
            -953437753, 1871620975, 222469645, -1141531461, -220507406, -213246989, -1505927258, 1503957849, -1128723780, 989458234,
            -283930129, -32995842, 26298625, 1628892769, 2094935420, -1306439758, 1118932802, -613270565, -1204861000, 1220511560,
            749628716, -473938205, 1463604823, -2053489019, 698968361, 2102355069, -1803474284, 1227804233, 398904087, -899076150,
            -1010959165, 1554224988, 1592264030, -789742896, -2016301945, -1912242290, -1167796806, -1465574744, -1222227017, -1178726727,
            1619502944, -120235272, 573974562, 286987281, -562741282, 2044275065, -1427208022, 858602547, 1601784927, -1229520202,
            -1765099370, 1479924312, -1664831332, -62711812, 444880154, -162717706, 475630108, 951221560, -1405921364, 416270104,
            -200897036, 1767076969, 1956362100, -174603019, 1454219094, -622628134, -706052395, 1257510218, -1634786658, -1565846878,
            1315067982, -396425240, -451044891, 958608441, -1040814399, 1147949124, 1563614813, 1917216882, 648045862, -1815233389,
            64674563, -960825146, -90257158, -2099861374, -814863409, 1349533776, -343548693, 1963654773, -1970064758, -1914723187,
            1277807180, 337383444, 1943478643, -860557108, 164942601, 277503248, -498003998, 0, -1709609062, -535126560,
            -1886112113, -423148826, -322352404, -36544771, -1417690709, -660021032
        )
    )
}