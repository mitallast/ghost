package com.github.mitallast.ghost.crypto.skc

import com.github.mitallast.ghost.crypto.utils.Arrays

object AES {

    fun encryptECB(plainText: ByteArray, key: ByteArray, pad: Padding.SCHEME = Padding.SCHEME.NONE): ByteArray {

        var plain = plainText
        if(pad == Padding.SCHEME.PKCS7) {
            plain = Padding.pkcs7Padding(plain)
        } else {
            checkSize(plain)
        }
        return encrypt(plain, key, mode = Mode.ECB)
    }

    fun decryptECB(encrypted: ByteArray, key: ByteArray, pad: Padding.SCHEME = Padding.SCHEME.NONE): ByteArray {

        val plaintext = decrypt(encrypted, key, mode = Mode.ECB)
        if(pad == Padding.SCHEME.PKCS7) {
            return Padding.pkcs7Unpadding(plaintext)
        }
        return plaintext
    }

    fun encryptCBC(plainText: ByteArray, key: ByteArray, iv: ByteArray, pad: Padding.SCHEME = Padding.SCHEME.NONE): ByteArray {

        var plain = plainText
        if(pad == Padding.SCHEME.PKCS7) {
            plain = Padding.pkcs7Padding(plain)
        } else {
            checkSize(plain)
        }
        checkIv(iv)
        val initvector = getState(iv)
        return encrypt(plain, key, initvector, Mode.CBC)
    }

    fun decryptCBC(encrypted: ByteArray, key: ByteArray, iv: ByteArray, pad: Padding.SCHEME = Padding.SCHEME.NONE): ByteArray {

        checkIv(iv)
        val initvector = getState(iv)
        val plain = decrypt(encrypted, key, initvector, Mode.CBC)
        if(pad == Padding.SCHEME.PKCS7) {
            return Padding.pkcs7Unpadding(plain)
        }
        return plain
    }

    fun encryptCTR(plainText: ByteArray, key: ByteArray, initNonce: ByteArray, pad: Padding.SCHEME = Padding.SCHEME.NONE): ByteArray {

        var plain = plainText
        if(pad == Padding.SCHEME.PKCS7) {
            plain = Padding.pkcs7Padding(plain)
        }

        val rounds = if(plain.size % 16 > 0) plain.size / 16 + 1 else plain.size / 16
        var bytes = plain.size
        val ciphertext = ByteArray(plain.size)

        var nonce = initNonce
        for (i in 0 until rounds) {
            val nonceCipher = encrypt(nonce, key, mode = Mode.CTR)
            val byteSize = if(bytes-16 > 0) 16 else bytes
            for (j in 0 until byteSize) {
                ciphertext[16 * i + j] = (nonceCipher[j].toInt() and 0xff xor (plain[16 * i + j].toInt() and 0xff)).toByte()
            }
            nonce = increment(nonce)
            bytes -= 16
        }
        return ciphertext
    }

    fun decryptCTR(ciphertext: ByteArray, key: ByteArray, initNonce: ByteArray, pad: Padding.SCHEME = Padding.SCHEME.NONE): ByteArray {

        val plaintext = ByteArray(ciphertext.size)
        val rounds = if(ciphertext.size % 16 > 0) ciphertext.size / 16 + 1 else ciphertext.size / 16
        var bytes = ciphertext.size
        var nonce = initNonce
        for (i in 0 until rounds) {
            val nonceCipher = encrypt(nonce, key, mode = Mode.CTR)
            val byteSize = if(bytes-16 > 0) 16 else bytes
            for (j in 0 until byteSize) {
                plaintext[16 * i + j] = (nonceCipher[j].toInt() and 0xff xor (ciphertext[16 * i + j].toInt() and 0xff)).toByte()
            }
            nonce = increment(nonce)
            bytes -= 16
        }

        if(pad == Padding.SCHEME.PKCS7) {
            return Padding.pkcs7Unpadding(plaintext)
        }

        return plaintext
    }

    private fun encrypt(plaintext: ByteArray, key: ByteArray, iv: Array<ByteArray> = Array(4) { ByteArray(4) }, mode: Mode): ByteArray {

        val keys = expandKey(key)
        val round = getRounds(key.size)
        var initvector = iv
        val ciphertext = ByteArray(plaintext.size)

        var i = 0
        while (i < plaintext.size) {
            val tmp = ByteArray(16)
            for (j in 0..15) {
                tmp[j] = plaintext[i + j]
            }
            var state = getState(tmp)

            if(mode == Mode.CBC) {
                state = addRoundKey(state, initvector)
            }

            state = addRoundKey(state, getState(keys[0]))

            for (r in 1 until round) {
                state = subBytes(state)
                state = shiftRows(state)
                state = mixColumns(state)
                state = addRoundKey(state, getState(keys[r]))
            }

            state = subBytes(state)
            state = shiftRows(state)
            state = addRoundKey(state, getState(keys[round]))

            if(mode == Mode.CBC) {
                initvector = state
            }

            val erg = getByte(state)
            for (j in 0..15) {
                ciphertext[i + j] = erg[j]
            }
            i += 16
        }

        return ciphertext
    }

    private fun decrypt(encrypted: ByteArray, key: ByteArray, iv: Array<ByteArray> = Array(4) { ByteArray(4) }, mode: Mode): ByteArray {

        val keys = expandKey(key)
        val round = getRounds(key.size)
        val plain = ByteArray(encrypted.size)
        val nextvector = Array(4) { ByteArray(4) }

        var i = 0
        while (i < plain.size) {

            val curr = ByteArray(16)
            for (j in 0..15) {
                curr[j] = encrypted[i + j]
            }
            var state = getState(curr)

            if(mode == Mode.CBC) {
                copyArray(nextvector, state)
            }

            state = addRoundKey(state, getState(keys[round]))
            state = invShiftRows(state)
            state = invSubBytes(state)

            for (r in round - 1 downTo 1) {
                state = addRoundKey(state, getState(keys[r]))
                state = invMixColumns(state)
                state = invShiftRows(state)
                state = invSubBytes(state)
            }

            state = addRoundKey(state, getState(keys[0]))

            if(mode == Mode.CBC) {
                state = addRoundKey(state, iv)
                copyArray(iv, nextvector)
            }

            val result = getByte(state)
            for (j in 0..15) {
                plain[i + j] = result[j]
            }
            i += 16
        }

        return plain
    }

    private fun expandKey(key: ByteArray): Array<ByteArray> {

        val round = getRounds(key.size)
        val nk = getWords(key.size)
        val keys = Array(round + 1) { ByteArray(16) }
        val w = Array(4 * (round + 1)) { ByteArray(4) }

        for (i in 0 until nk) {
            for (j in 0..3) {
                w[i][j] = key[4 * i + j]
            }
        }

        for (i in nk until 4 * (round + 1)) {
            var tmp = w[i - 1]
            if (i % nk == 0) {
                val rc = byteArrayOf((rcon[i / nk]).toByte(), 0, 0, 0)
                tmp = xor(subWord(rotateWord(tmp)), rc)
            } else if (nk > 6 && i % nk == 4) {
                tmp = subWord(tmp)
            }
            w[i] = xor(w[i - nk], tmp)
        }

        for (i in 0 until round + 1) {
            for (j in 0..3) {
                for (k in 0..3) {
                    keys[i][4 * j + k] = w[4 * i + j][k]
                }
            }
        }

        return keys
    }

    private fun getRounds(size: Int): Int {
        return when (size) {
            16 -> 10
            24 -> 12
            32 -> 14
            else -> throw IllegalArgumentException("key size is not supported")
        }
    }

    private fun getWords(size: Int): Int {
        return when (size) {
            16 -> 4
            24 -> 6
            32 -> 8
            else -> throw IllegalArgumentException("key size is not supported")
        }
    }

    private fun subWord(w: ByteArray): ByteArray {
        val result = ByteArray(4)
        for (i in 0..3) {
            result[i] = subByte(w[i])
        }
        return result
    }

    private fun rotateWord(word: ByteArray): ByteArray {
        val result = ByteArray(4)
        result[0] = word[1]
        result[1] = word[2]
        result[2] = word[3]
        result[3] = word[0]
        return result
    }

    private fun getState(p: ByteArray): Array<ByteArray> {
        val result = Array(4) { ByteArray(4) }
        for (j in 0..3) {
            for (i in 0..3) {
                result[i][j] = p[4 * j + i]
            }
        }
        return result
    }

    private fun addRoundKey(state: Array<ByteArray>, key: Array<ByteArray>): Array<ByteArray> {
        val result = Array(4) { ByteArray(4) }
        for (i in 0..3) {
            for (j in 0..3) {
                result[i][j] = add(state[i][j], key[i][j])
            }
        }
        return result
    }

    private fun getByte(state: Array<ByteArray>): ByteArray {
        val result = ByteArray(16)
        for (j in 0..3) {
            for (i in 0..3) {
                result[4 * j + i] = state[i][j]
            }
        }
        return result
    }

    private fun subBytes(state: Array<ByteArray>): Array<ByteArray> {
        val result = Array(4) { ByteArray(4) }
        for (i in 0..3) {
            for (j in 0..3) {
                result[i][j] = subByte(state[i][j])
            }
        }
        return result
    }

    private fun shiftRows(state: Array<ByteArray>): Array<ByteArray> {
        val result = Array(4) { ByteArray(4) }
        for (i in 0..3) {
            for (j in 0..3) {
                result[i][floorMod(j - i, 4)] = state[i][j]
            }
        }
        return result
    }

    private fun mixColumns(state: Array<ByteArray>): Array<ByteArray> {
        val result = Array(4) { ByteArray(4) }
        for (i in 0..3) {
            for (j in 0..3) {
                (0..3).map { multiply((mixColumnsSbox[i][it]).toByte(), state[it][j]) }.forEach { result[i][j] = add(result[i][j], it) }
            }
        }
        return result
    }

    private fun copyArray(dest: Array<ByteArray>, src: Array<ByteArray>) {
        for (i in dest.indices) {
            Arrays.arraycopy(src[i], 0, dest[i], 0, dest[0].size)
        }
    }

    private fun invShiftRows(state: Array<ByteArray>): Array<ByteArray> {
        val result = Array(4) { ByteArray(4) }
        for (i in 0..3) {
            for (j in 0..3) {
                result[i][j] = state[i][floorMod(j - i, 4)]
            }
        }
        return result
    }

    private fun invSubBytes(state: Array<ByteArray>): Array<ByteArray> {
        val result = Array(4) { ByteArray(4) }
        for (i in 0..3) {
            for (j in 0..3) {
                result[i][j] = invSubByte(state[i][j])
            }
        }
        return result
    }

    private fun invMixColumns(state: Array<ByteArray>): Array<ByteArray> {
        val result = Array(4) { ByteArray(4) }
        for (i in 0..3) {
            for (j in 0..3) {
                (0..3).map { multiply((invMixColumnsSbox[i][it]).toByte(), state[it][j]) }.forEach { result[i][j] = add(result[i][j], it) }
            }
        }
        return result
    }

    private fun subByte(b: Byte): Byte {
        val i = b.toInt() and 0xff
        return (sbox[i shr 4][i and 0x0f]).toByte()
    }

    private fun invSubByte(b: Byte): Byte {
        val i = b.toInt() and 0xff
        return (invSbox[i shr 4][i and 0x0f]).toByte()
    }

    private fun xor(a: ByteArray, b: ByteArray): ByteArray {
        val result = ByteArray(4)
        for (i in result.indices) {
            result[i] = (a[i].toInt() and 0xff xor (b[i].toInt() and 0xff)).toByte()
        }
        return result
    }

    private fun increment(b: ByteArray): ByteArray {
        val result = b.copyOf()

        var erg = 1
        for (i in result.indices.reversed()) {
            erg += (result[i].toInt() and 0xff)
            if (erg > 255) {
                result[i] = 0x00.toByte()
                erg -= 255
            } else {
                result[i] = erg.toByte()
                break
            }
        }

        return result
    }

    private fun multiply(a: Byte, b: Byte): Byte {

        var result = 0
        var tmp = a.toInt() and 0xff

        for (i in 0..7) {
            if (b.toInt() and (1 shl i) and 0xff > 0) {
                result = result xor tmp
            }

            if (tmp and (1 shl 7) > 0) {
                tmp = tmp shl 1
                tmp = tmp xor 0x1b
            } else {
                tmp = tmp shl 1
            }
            tmp %= 0x100
        }

        return result.toByte()
    }

    private fun add(a: Byte, b: Byte): Byte {
        return (a.toInt() and 0xff xor (b.toInt() and 0xff)).toByte()
    }

    private fun checkSize(plain: ByteArray) {
        if(plain.size % 16 != 0) {
            throw IllegalArgumentException("plainText size is not a multiple of 128 bit: " + plain.size * 8)
        }
    }

    private fun checkIv(iv: ByteArray) {
        if(iv.isNotEmpty() && iv.size != 16) throw IllegalArgumentException("The initialization vector should have a size of 128 bit: " + iv.size * 8)
    }

    private val sbox = arrayOf(
            intArrayOf(0x63, 0x7c, 0x77, 0x7b, 0xf2, 0x6b, 0x6f, 0xc5, 0x30, 0x01, 0x67, 0x2b, 0xfe, 0xd7, 0xab, 0x76),
            intArrayOf(0xca, 0x82, 0xc9, 0x7d, 0xfa, 0x59, 0x47, 0xf0, 0xad, 0xd4, 0xa2, 0xaf, 0x9c, 0xa4, 0x72, 0xc0),
            intArrayOf(0xb7, 0xfd, 0x93, 0x26, 0x36, 0x3f, 0xf7, 0xcc, 0x34, 0xa5, 0xe5, 0xf1, 0x71, 0xd8, 0x31, 0x15),
            intArrayOf(0x04, 0xc7, 0x23, 0xc3, 0x18, 0x96, 0x05, 0x9a, 0x07, 0x12, 0x80, 0xe2, 0xeb, 0x27, 0xb2, 0x75),
            intArrayOf(0x09, 0x83, 0x2c, 0x1a, 0x1b, 0x6e, 0x5a, 0xa0, 0x52, 0x3b, 0xd6, 0xb3, 0x29, 0xe3, 0x2f, 0x84),
            intArrayOf(0x53, 0xd1, 0x00, 0xed, 0x20, 0xfc, 0xb1, 0x5b, 0x6a, 0xcb, 0xbe, 0x39, 0x4a, 0x4c, 0x58, 0xcf),
            intArrayOf(0xd0, 0xef, 0xaa, 0xfb, 0x43, 0x4d, 0x33, 0x85, 0x45, 0xf9, 0x02, 0x7f, 0x50, 0x3c, 0x9f, 0xa8),
            intArrayOf(0x51, 0xa3, 0x40, 0x8f, 0x92, 0x9d, 0x38, 0xf5, 0xbc, 0xb6, 0xda, 0x21, 0x10, 0xff, 0xf3, 0xd2),
            intArrayOf(0xcd, 0x0c, 0x13, 0xec, 0x5f, 0x97, 0x44, 0x17, 0xc4, 0xa7, 0x7e, 0x3d, 0x64, 0x5d, 0x19, 0x73),
            intArrayOf(0x60, 0x81, 0x4f, 0xdc, 0x22, 0x2a, 0x90, 0x88, 0x46, 0xee, 0xb8, 0x14, 0xde, 0x5e, 0x0b, 0xdb),
            intArrayOf(0xe0, 0x32, 0x3a, 0x0a, 0x49, 0x06, 0x24, 0x5c, 0xc2, 0xd3, 0xac, 0x62, 0x91, 0x95, 0xe4, 0x79),
            intArrayOf(0xe7, 0xc8, 0x37, 0x6d, 0x8d, 0xd5, 0x4e, 0xa9, 0x6c, 0x56, 0xf4, 0xea, 0x65, 0x7a, 0xae, 0x08),
            intArrayOf(0xba, 0x78, 0x25, 0x2e, 0x1c, 0xa6, 0xb4, 0xc6, 0xe8, 0xdd, 0x74, 0x1f, 0x4b, 0xbd, 0x8b, 0x8a),
            intArrayOf(0x70, 0x3e, 0xb5, 0x66, 0x48, 0x03, 0xf6, 0x0e, 0x61, 0x35, 0x57, 0xb9, 0x86, 0xc1, 0x1d, 0x9e),
            intArrayOf(0xe1, 0xf8, 0x98, 0x11, 0x69, 0xd9, 0x8e, 0x94, 0x9b, 0x1e, 0x87, 0xe9, 0xce, 0x55, 0x28, 0xdf),
            intArrayOf(0x8c, 0xa1, 0x89, 0x0d, 0xbf, 0xe6, 0x42, 0x68, 0x41, 0x99, 0x2d, 0x0f, 0xb0, 0x54, 0xbb, 0x16)
    )

    private val invSbox = arrayOf(
            intArrayOf(0x52, 0x09, 0x6a, 0xd5, 0x30, 0x36, 0xa5, 0x38, 0xbf, 0x40, 0xa3, 0x9e, 0x81, 0xf3, 0xd7, 0xfb),
            intArrayOf(0x7c, 0xe3, 0x39, 0x82, 0x9b, 0x2f, 0xff, 0x87, 0x34, 0x8e, 0x43, 0x44, 0xc4, 0xde, 0xe9, 0xcb),
            intArrayOf(0x54, 0x7b, 0x94, 0x32, 0xa6, 0xc2, 0x23, 0x3d, 0xee, 0x4c, 0x95, 0x0b, 0x42, 0xfa, 0xc3, 0x4e),
            intArrayOf(0x08, 0x2e, 0xa1, 0x66, 0x28, 0xd9, 0x24, 0xb2, 0x76, 0x5b, 0xa2, 0x49, 0x6d, 0x8b, 0xd1, 0x25),
            intArrayOf(0x72, 0xf8, 0xf6, 0x64, 0x86, 0x68, 0x98, 0x16, 0xd4, 0xa4, 0x5c, 0xcc, 0x5d, 0x65, 0xb6, 0x92),
            intArrayOf(0x6c, 0x70, 0x48, 0x50, 0xfd, 0xed, 0xb9, 0xda, 0x5e, 0x15, 0x46, 0x57, 0xa7, 0x8d, 0x9d, 0x84),
            intArrayOf(0x90, 0xd8, 0xab, 0x00, 0x8c, 0xbc, 0xd3, 0x0a, 0xf7, 0xe4, 0x58, 0x05, 0xb8, 0xb3, 0x45, 0x06),
            intArrayOf(0xd0, 0x2c, 0x1e, 0x8f, 0xca, 0x3f, 0x0f, 0x02, 0xc1, 0xaf, 0xbd, 0x03, 0x01, 0x13, 0x8a, 0x6b),
            intArrayOf(0x3a, 0x91, 0x11, 0x41, 0x4f, 0x67, 0xdc, 0xea, 0x97, 0xf2, 0xcf, 0xce, 0xf0, 0xb4, 0xe6, 0x73),
            intArrayOf(0x96, 0xac, 0x74, 0x22, 0xe7, 0xad, 0x35, 0x85, 0xe2, 0xf9, 0x37, 0xe8, 0x1c, 0x75, 0xdf, 0x6e),
            intArrayOf(0x47, 0xf1, 0x1a, 0x71, 0x1d, 0x29, 0xc5, 0x89, 0x6f, 0xb7, 0x62, 0x0e, 0xaa, 0x18, 0xbe, 0x1b),
            intArrayOf(0xfc, 0x56, 0x3e, 0x4b, 0xc6, 0xd2, 0x79, 0x20, 0x9a, 0xdb, 0xc0, 0xfe, 0x78, 0xcd, 0x5a, 0xf4),
            intArrayOf(0x1f, 0xdd, 0xa8, 0x33, 0x88, 0x07, 0xc7, 0x31, 0xb1, 0x12, 0x10, 0x59, 0x27, 0x80, 0xec, 0x5f),
            intArrayOf(0x60, 0x51, 0x7f, 0xa9, 0x19, 0xb5, 0x4a, 0x0d, 0x2d, 0xe5, 0x7a, 0x9f, 0x93, 0xc9, 0x9c, 0xef),
            intArrayOf(0xa0, 0xe0, 0x3b, 0x4d, 0xae, 0x2a, 0xf5, 0xb0, 0xc8, 0xeb, 0xbb, 0x3c, 0x83, 0x53, 0x99, 0x61),
            intArrayOf(0x17, 0x2b, 0x04, 0x7e, 0xba, 0x77, 0xd6, 0x26, 0xe1, 0x69, 0x14, 0x63, 0x55, 0x21, 0x0c, 0x7d)
    )

    private val mixColumnsSbox = arrayOf(
            intArrayOf(0x2, 0x3, 0x1, 0x1),
            intArrayOf(0x1, 0x2, 0x3, 0x1),
            intArrayOf(0x1, 0x1, 0x2, 0x3),
            intArrayOf(0x3, 0x1, 0x1, 0x2)
    )

    private val invMixColumnsSbox = arrayOf(
            intArrayOf(0x0e, 0x0b, 0x0d, 0x09),
            intArrayOf(0x09, 0x0e, 0x0b, 0x0d),
            intArrayOf(0x0d, 0x09, 0x0e, 0x0b),
            intArrayOf(0x0b, 0x0d, 0x09, 0x0e)
    )

    private val rcon = intArrayOf(0x00, 0x01, 0x02, 0x04, 0x08, 0x10, 0x20, 0x40, 0x80, 0x1b, 0x36, 0x6c, 0xd8, 0xab, 0x4d)

    private fun floorDiv(x: Int, y: Int): Int {
        var r = x / y
        // if the signs are different and modulo not zero, round down
        if (x xor y < 0 && r * y != x) {
            r--
        }
        return r
    }

    private fun floorMod(x: Int, y: Int): Int {
        return x - floorDiv(x, y) * y
    }
}