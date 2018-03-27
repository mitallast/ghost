package com.github.mitallast.ghost.crypto.skc

import com.github.mitallast.ghost.crypto.utils.Arrays

object Serpent {

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

        val rounds = if (plaintext.size % 16 > 0) plaintext.size / 16 + 1 else plaintext.size / 16
        for (i in 0 until rounds) {

            if(mode == Mode.CBC) {
                xorBlock(plaintext, i * 16, initvector)
            }

            val result = encryptBlock(plaintext, i * 16, keys)
            Arrays.arraycopy(result, 0, ciphertext, 16 * i, 16)
            if(mode == Mode.CBC) {
                initvector = result
            }
        }
        return ciphertext
    }

    private fun decrypt(chipertext: ByteArray, key: ByteArray, iv: ByteArray = ByteArray(0), mode: Mode): ByteArray {

        val keys = expandKey(key)
        val plaintext = ByteArray(chipertext.size)
        val rounds = if (chipertext.size % 16 > 0) chipertext.size / 16 + 1 else chipertext.size / 16
        var initvector = iv

        for (i in 0 until rounds) {
            val result = decryptBlock(chipertext, i * 16, keys)
            Arrays.arraycopy(result, 0, plaintext, 16 * i, 16)

            if(mode == Mode.CBC) {
                val tmp = chipertext.copyOfRange(i*16, i*16+16)
                xorBlock(plaintext, i*16, initvector)
                initvector = tmp.copyOf()
            }
        }

        return plaintext
    }

    private fun expandKey(key: ByteArray): IntArray {

        val w = IntArray(4 * (ROUNDS + 1))
        var limit = key.size / 4

        var t: Int
        var i = 0
        var j = 0
        while (i < limit) {
            w[i] = integerify(key, i * 4)
            i++
        }

        if (i < 8) {
            w[i] = 1
        }

        for (s in 8 until 16) {
            t = w[j] xor w[s - 5] xor w[s - 3] xor w[s - 1] xor PHI xor j++
            w[s] = t shl 11 or t.ushr(21)
        }

        i = 0
        j = 8
        while (i < 8) {
            w[i++] = w[j++]
        }
        limit = 4 * (ROUNDS + 1)

        while (i < limit) {
            t = w[i - 8] xor w[i - 5] xor w[i - 3] xor w[i - 1] xor PHI xor i
            w[i] = t shl 11 or t.ushr(21)
            i++
        }

        for (k in 0 until ROUNDS + 1) {
            val state = intArrayOf(0, 0, 0, 0)
            sboxify(intArrayOf(w[4 * k], w[4 * k + 1], w[4 * k + 2], w[4 * k + 3]), state, (ROUNDS + 3 - k) % 8)
            makeKey(w, state, k)
        }

        return w
    }

    private fun encryptBlock(input: ByteArray, offset: Int, sessionKey: IntArray): ByteArray {

        val state = intArrayOf(
                integerify(input, offset),
                integerify(input, offset + 4),
                integerify(input, offset + 8),
                integerify(input, offset + 12)
        )

        for (i in 0 until 4) {

            xor(state, sessionKey, i, 0)
            sb0(state)
            lt(state)
            xor(state, sessionKey, i, 1)
            sb1(state)
            lt(state)
            xor(state, sessionKey, i, 2)
            sb2(state)
            lt(state)
            xor(state, sessionKey, i, 3)
            sb3(state)
            lt(state)
            xor(state, sessionKey, i, 4)
            sb4(state)
            lt(state)
            xor(state, sessionKey, i, 5)
            sb5(state)
            lt(state)
            xor(state, sessionKey, i, 6)
            sb6(state)
            lt(state)
            xor(state, sessionKey, i, 7)

            if (i < 3) {
                sb7(state)
                lt(state)
            }
        }

        end(state)
        xor(state, sessionKey, 3, 8)

        return getBytes(state)
    }

    private fun decryptBlock(input: ByteArray, offset: Int, sessionKey: IntArray): ByteArray {

        val state = intArrayOf(
                sessionKey[128] xor integerify(input, offset),
                sessionKey[129] xor integerify(input, offset + 4),
                sessionKey[130] xor integerify(input, offset + 8),
                sessionKey[131] xor integerify(input, offset + 12)
        )

        for (i in 4 downTo 1) {

            sb7inv(state)
            xorinv(state, sessionKey, i, 0)
            ilt(state)

            sb6inv(state)
            xorinv(state, sessionKey, i, 4)
            ilt(state)

            sb5inv(state)
            xorinv(state, sessionKey, i, 8)
            ilt(state)

            sb4inv(state)
            xorinv(state, sessionKey, i, 12)
            ilt(state)

            sb3inv(state)
            xorinv(state, sessionKey, i, 16)
            ilt(state)

            sb2inv(state)
            xorinv(state, sessionKey, i, 20)
            ilt(state)

            sb1inv(state)
            xorinv(state, sessionKey, i, 24)
            ilt(state)

            sb0inv(state)
            xorinv(state, sessionKey, i, 28)


            if (i > 1) {
                ilt(state)
            }
        }

        return getBytes(state)
    }

    private fun xorBlock(block: ByteArray, offset: Int, iv: ByteArray) {
        for (i in iv.indices) {
            block[i+offset] = (block[i+offset].toInt() xor iv[i].toInt()).toByte()
        }
    }

    private fun makeKey(w: IntArray, state: IntArray, i: Int) {
        w[4 * i] = state[0]
        w[4 * i + 1] = state[1]
        w[4 * i + 2] = state[2]
        w[4 * i + 3] = state[3]
    }

    private fun integerify(b: ByteArray, num: Int): Int {
        var n = b[num].toInt() and 0xff shl 0
        n = n or (b[num + 1].toInt() and 0xff shl 8)
        n = n or (b[num + 2].toInt() and 0xff shl 16)
        n = n or (b[num + 3].toInt() and 0xff shl 24)
        return n
    }

    private fun xor(state: IntArray, sessionKey: IntArray, round: Int, off: Int) {
        val offset = (round * 8) + off
        state[0] = state[0] xor sessionKey[offset * 4]
        state[1] = state[1] xor sessionKey[offset * 4 + 1]
        state[2] = state[2] xor sessionKey[offset * 4 + 2]
        state[3] = state[3] xor sessionKey[offset * 4 + 3]
    }

    private fun xorinv(state: IntArray, sessionKey: IntArray, round: Int, off: Int) {
        val offset = (round * 32) - off - 1
        state[0] = state[0] xor sessionKey[offset - 3]
        state[1] = state[1] xor sessionKey[offset - 2]
        state[2] = state[2] xor sessionKey[offset - 1]
        state[3] = state[3] xor sessionKey[offset]
    }

    private fun sboxify(inArray: IntArray, state: IntArray, sboxIdx: Int) {
        val sb = sbox[sboxIdx]
        for (l in 0 until 32) {
            val si = sb[inArray[0].ushr(l) and 0x01 or (inArray[1].ushr(l) and 0x01 shl 1) or (inArray[2].ushr(l) and 0x01 shl 2) or (inArray[3].ushr(l) and 0x01 shl 3)]
            state[0] = state[0] or (si and 0x01 shl l)
            state[1] = state[1] or (si.ushr(1) and 0x01 shl l)
            state[2] = state[2] or (si.ushr(2) and 0x01 shl l)
            state[3] = state[3] or (si.ushr(3) and 0x01 shl l)
        }
    }

    private fun sb0(state: IntArray) {

        val y3 = (state[0] or state[3]) xor (state[1] xor state[2])
        val y2 = ((state[0] xor state[1]) and (state[1] or state[2])) xor (state[3] and (state[2] or y3))
        val y0 = ((state[0] xor state[3]) xor ((state[1] or state[2]) xor (((state[0] xor state[1]) and (state[1] or state[2])) and y2))).inv()
        val y1 = (state[2] xor state[3]) xor (y0 xor (state[1] and (state[0] xor state[3])))

        toState(state, y0, y1, y2, y3)
    }

    private fun sb1(state: IntArray) {

        val y2 = (state[2] xor state[3]) xor (state[0] or (state[1].inv()))
        val y3 = (((state[0] or state[3]) and (state[2] xor state[3])) xor (state[1] or (state[3] and (state[0] xor state[2])))).inv()
        val y1 = (state[1] and state[3]) xor (y2 xor ((state[0] or state[3]) xor (((state[0] or state[3]) and (state[2] xor state[3])) xor (state[1] or (state[3] and (state[0] xor state[2]))))))
        val y0 = state[2] xor ((state[0] or (state[1].inv())) and ((((state[0] or state[3]) and (state[2] xor state[3])) xor (state[1] or (state[3] and (state[0] xor state[2])))) or y1))

        toState(state, y0, y1, y2, y3)
    }

    private fun sb2(state: IntArray) {

        val y0 = (state[0] xor state[1]) xor state[3] xor (state[0] or state[2])
        val y1 = ((state[0] xor state[1]) or ((state[3] xor (state[0] or state[2])) xor (state[1] or (state[2] xor y0)))) xor ((state[0] or state[2]) and (state[1] xor (state[2] xor y0)))
        val y3 = ((state[3] xor (state[0] or state[2])) xor (state[1] or (state[2] xor y0))).inv()
        val y2 = (state[0] or state[3]) xor state[1] xor ((state[3] xor (state[0] or state[2])) xor (state[1] or (state[2] xor y0)) xor y1)

        toState(state, y0, y1, y2, y3)
    }

    private fun sb3(state: IntArray) {

        val y3 = (state[2] or (state[0] and state[1])) xor (state[1] xor (state[3] xor ((state[0] xor state[2]) and (state[0] or state[3]))))
        val y2 = (state[2] or (state[0] and state[1])) xor ((state[0] or state[3]) xor (state[3] and (state[1] or (state[0] and state[3]))))
        val y0 = (state[0] or (state[3] xor ((state[0] xor state[2]) and (state[0] or state[3])))) xor (state[1] and (state[3] or y3))
        val y1 = (state[1] or (state[0] and state[3])) xor ((state[0] xor state[2]) and (state[0] or state[3]))

        toState(state, y0, y1, y2, y3)
    }

    private fun sb4(state: IntArray) {

        val y3 = (state[0] xor (state[1] or state[2])) xor (state[3] and (state[0] or state[1]))
        val y2 = ((state[1] and state[2]) or (state[0] xor (state[1] or state[2]))) xor (y3 and (state[1] xor state[3]))
        val y1 = (state[0] and (state[3] or (state[0] xor (state[1] or state[2])))) xor ((state[1] and state[2]) or ((state[1] xor state[3]) xor (y3 and (state[1] xor state[3]))))
        val y0 = ((state[2] xor (state[3] and (state[0] or state[1]))) xor ((state[1] xor state[3]) and (state[3] or (state[0] xor (state[1] or state[2]))))).inv()

        toState(state, y0, y1, y2, y3)
    }

    private fun sb5(state: IntArray) {

        val y0 = ((state[0] and (state[1] xor state[3])) xor (state[2] xor (state[1] or state[3]))).inv()
        val y2 = (state[1] or ((state[0] and (state[1] xor state[3])) xor (state[2] xor (state[1] or state[3])))) xor ((state[0] xor (state[1] xor state[3])) or (state[3] xor (state[3] or y0)))
        val y1 = (state[0] xor (state[1] xor state[3])) xor (state[3] or y0)
        val y3 = ((state[0] and (state[1] xor state[3])) or y0) xor ((state[1] xor state[3]) xor (state[1] or (state[0] xor (state[1] xor state[3]))))

        toState(state, y0, y1, y2, y3)
    }

    private fun sb6(state: IntArray) {

        val y1 = ((state[0] and state[3]) xor (state[1] xor state[2])).inv()
        val y2 = ((state[0] or state[2]) xor (((state[0] xor state[3]) and (state[1] or state[2])) xor (state[1] and y1))).inv()
        val y3 = (state[2] xor (state[1] or state[3])) xor ((state[0] xor state[3]) and (state[1] or state[2]))
        val y0 = (state[0] xor state[1]) xor (y2 xor (y1 and (state[0] xor state[3])))

        toState(state, y0, y1, y2, y3)
    }

    private fun sb7(state: IntArray) {

        val y3 = (state[0] and (state[3].inv())) xor (state[2] xor (state[1] or (state[0] and state[2])))
        val y1 = (state[3] or (state[0] and state[1])) xor (state[0] xor (state[2] or y3))
        val y0 = (state[2] xor (state[0] and state[1])) xor ((state[3].inv()) or ((state[0] and state[2]) xor y1))
        val y2 = state[0] xor (((state[1] or (state[0] and state[2])) and y3) or (state[1] xor y1))

        toState(state, y0, y1, y2, y3)
    }

    private fun sb7inv(state: IntArray) {

        val y3 = (state[2] or (state[0] and state[1])) xor (state[3] and (state[0] or state[1]))
        val y1 = state[0] xor ((state[1] xor (state[3] and (state[0] or state[1]))) or ((state[3] xor y3).inv()))
        val y0 = (state[2] xor (state[1] xor (state[3] and (state[0] or state[1])))) xor (state[3] or y1)
        val y2 = (state[2] and (state[0] or state[3])) xor ((state[0] and state[1]) or (state[1] xor state[3]))

        toState(state, y0, y1, y2, y3)
    }

    private fun sb6inv(state: IntArray) {

        val y1 = (state[1] xor state[3]) xor (state[0] or (state[2].inv()))
        val y0 = ((state[0] and (state[1] or (state[2].inv()))) xor (state[3] or (state[1] and (state[0] xor state[2])))).inv()
        val y3 = (state[0] xor y1) xor ((state[0] and (state[1] or (state[2].inv()))) xor ((state[0] xor state[2]) and (state[3] or (state[1] and (state[0] xor state[2])))))
        val y2 = (state[3] or (state[2].inv())) xor ((state[0] xor state[2]) xor (state[1] and y0))

        toState(state, y0, y1, y2, y3)
    }

    private fun sb5inv(state: IntArray) {

        val y0 = (state[0] xor state[3]) xor (state[1] and (state[2] xor (state[0] and state[3])))
        val y1 = ((state[0] and state[3]) xor y0) xor (state[1] or (state[0] and state[2]))
        val y3 = (state[2] xor (state[0] and state[3])) xor ((state[1].inv()) or (state[0] and y0))
        val y2 = (state[1] xor state[3]) xor ((state[2] xor (state[0] and state[3])) xor (y0 or y1))

        toState(state, y0, y1, y2, y3)
    }

    private fun sb4inv(state: IntArray) {

        val y1 = (state[2] xor state[3]) xor (state[0] and (state[1] xor (state[2] or state[3])))
        val y3 = (state[0] and (state[1] or state[3])) xor (state[3] xor (state[1] xor (state[2] or state[3])))
        val y2 = ((state[1] or state[3]) xor (y1 or ((state[0] and (state[1] or state[3])).inv()))) xor (state[2] or (state[0] xor (state[0] and (state[1] xor (state[2] or state[3])))))
        val y0 = (state[0] xor (state[1] xor (state[2] or state[3]))) xor (y1 or ((state[0] and (state[1] or state[3])).inv()))

        toState(state, y0, y1, y2, y3)
    }

    private fun sb3inv(state: IntArray) {

        val y2 = (state[0] xor state[3]) xor ((state[1] xor (state[0] or state[3])) and (state[2] xor (state[0] or state[3])))
        val y0 = (state[1] and (state[2] or state[3])) xor (state[2] xor (state[0] or state[3]))
        val y1 = state[1] xor ((state[0] xor (state[2] xor (state[0] or state[3]))) and (y0 or (state[0] xor state[3])))
        val y3 = ((state[2] or state[3]) xor (state[0] xor state[3])) xor (state[1] or (state[0] and y2))

        toState(state, y0, y1, y2, y3)
    }

    private fun sb2inv(state: IntArray) {

        val y0 = (state[0] xor state[3]) xor (state[1] or (state[2] xor state[3]))
        val y3 = (state[1] and (state[0] or state[2])) xor ((state[3].inv()) or (state[0] and state[2]))
        val y1 = ((state[0] or state[2]) and (state[2] xor state[3])) xor (state[1] and (state[3] or y0))
        val y2 = (y0 xor y1) xor (((state[3].inv()) or (state[0] and state[2])) xor (state[2] and y3))

        toState(state, y0, y1, y2, y3)
    }

    private fun sb1inv(state: IntArray) {

        val y2 = ((state[3] or (state[0] and state[2])) xor ((state[0] xor state[1]) and (state[0] or (state[2] xor (state[1] or state[3]))))).inv()
        val y1 = ((state[2] xor (state[1] or state[3])) or (state[0] and state[2])) xor (state[3] and (state[1] xor ((state[0] xor state[1]) and (state[0] or (state[2] xor (state[1] or state[3]))))))
        val y3 = (state[0] xor state[1]) xor (state[2] xor (state[1] or state[3]))
        val y0 = (state[0] or y2) xor (state[2] xor (((state[0] xor state[1]) and (state[0] or (state[2] xor (state[1] or state[3])))) xor y1))

        toState(state, y0, y1, y2, y3)
    }

    private fun sb0inv(state: IntArray) {

        val y2 = ((state[0] or state[1]) xor (state[2] xor state[3])).inv()
        val y1 = ((state[1] or state[2]) and (state[1] xor state[3])) xor (state[0] or (state[2] and (state[2] xor state[3])))
        val y3 = ((state[1] or state[2]) xor (state[3] or y2)) xor (y1 xor (state[0] or ((state[0] or state[1]) xor (state[2] xor state[3]))))
        val y0 = (state[0] xor state[2]) xor (((state[1] or state[2]) xor (state[3] or y2)) or (((state[0] or state[1]) xor (state[2] xor state[3])) and (y1 xor (state[0] or ((state[0] or state[1]) xor (state[2] xor state[3]))))))

        toState(state, y0, y1, y2, y3)
    }

    private fun end(state: IntArray) {

        val y3 = (state[0] and (state[3].inv())) xor (state[2] xor (state[1] or (state[0] and state[2])))
        val y1 = (state[3] or (state[0] and state[1])) xor (state[0] xor (state[2] or y3))
        val y0 = (state[2] xor (state[0] and state[1])) xor ((state[3].inv()) or ((state[0] and state[2]) xor y1))
        val y2 = state[0] xor (((state[1] or (state[0] and state[2])) and y3) or (state[1] xor y1))

        toState(state, y0, y1, y2, y3)
    }

    private fun lt(state: IntArray) {
        state[0] = state[0] shl 13 or (state[0]).ushr(19)
        state[2] = state[2] shl 3 or (state[2]).ushr(29)
        state[1] = state[1] xor state[0] xor state[2]
        state[3] = state[3] xor state[2] xor (state[0] shl 3)
        state[1] = state[1] shl 1 or state[1].ushr(31)
        state[3] = state[3] shl 7 or state[3].ushr(25)
        state[0] = state[0] xor state[1] xor state[3]
        state[2] = state[2] xor state[3] xor (state[1] shl 7)
        state[0] = state[0] shl 5 or state[0].ushr(27)
        state[2] = state[2] shl 22 or state[2].ushr(10)
    }

    private fun ilt(state: IntArray) {
        state[2] = state[2] shl 32 - 22 or state[2].ushr(22)
        state[0] = state[0] shl 32 - 5 or state[0].ushr(5)
        state[2] = state[2] xor state[3] xor (state[1] shl 7)
        state[0] = state[0] xor state[1] xor state[3]
        state[3] = state[3] shl 32 - 7 or state[3].ushr(7)
        state[1] = state[1] shl 32 - 1 or state[1].ushr(1)
        state[3] = state[3] xor state[2] xor (state[0] shl 3)
        state[1] = state[1] xor state[0] xor state[2]
        state[2] = state[2] shl 32 - 3 or state[2].ushr(3)
        state[0] = state[0] shl 32 - 13 or state[0].ushr(13)
    }

    private fun toState(state: IntArray, y0: Int, y1: Int, y2: Int, y3: Int) {
        state[0] = y0
        state[1] = y1
        state[2] = y2
        state[3] = y3
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

    private fun checkSize(plain: ByteArray) {
        if (plain.size % 16 != 0) {
            throw IllegalArgumentException("plainText size is not a multiple of 128 bit: " + plain.size * 8)
        }
    }

    private fun checkIv(iv: ByteArray) {
        if(iv.isNotEmpty() && iv.size != 16) throw IllegalArgumentException("The initialization vector should have a size of 128 bit: " + iv.size * 8)
    }

    private val sbox = arrayOf(
            intArrayOf(3, 8, 15, 1, 10, 6, 5, 11, 14, 13, 4, 2, 7, 0, 9, 12), // SO
            intArrayOf(15, 12, 2, 7, 9, 0, 5, 10, 1, 11, 14, 8, 6, 13, 3, 4), // S1
            intArrayOf(8, 6, 7, 9, 3, 12, 10, 15, 13, 1, 14, 4, 0, 11, 5, 2), // S2
            intArrayOf(0, 15, 11, 8, 12, 9, 6, 3, 13, 1, 2, 4, 10, 7, 5, 14), // S3
            intArrayOf(1, 15, 8, 3, 12, 0, 11, 6, 2, 5, 4, 10, 9, 14, 7, 13), // S4
            intArrayOf(15, 5, 2, 11, 4, 10, 9, 12, 0, 3, 14, 8, 13, 6, 7, 1), // S5
            intArrayOf(7, 2, 12, 5, 8, 4, 6, 11, 14, 9, 1, 15, 13, 3, 10, 0), // S6
            intArrayOf(1, 13, 15, 0, 14, 8, 2, 11, 7, 4, 12, 10, 9, 3, 5, 6)) // S7

    private const val ROUNDS = 32
    private const val PHI = -0x61c88647
}