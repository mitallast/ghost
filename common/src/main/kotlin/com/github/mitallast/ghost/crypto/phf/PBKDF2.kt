package com.github.mitallast.ghost.crypto.phf

import com.github.mitallast.ghost.crypto.mac.HMAC
import com.github.mitallast.ghost.crypto.utils.Arrays
import com.github.mitallast.ghost.crypto.utils.CryptoRandom
import com.github.mitallast.ghost.crypto.utils.Strings
import kotlin.experimental.xor
import kotlin.math.pow

object PBKDF2 {

    const val HMAC_SHA1 = "sha1"
    const val HMAC_SHA256 = "sha256"
    const val HMAC_SHA512 = "sha512"

    fun hash(prf: String, password: String, salt: ByteArray, iterations: Int, dkLen: Int): ByteArray {

        return when (prf) {
            "sha1" -> pbkdf2(Strings.toByteArray(password), salt, iterations, dkLen, 20, HMAC::sha1)
            "sha256" -> pbkdf2(Strings.toByteArray(password), salt, iterations, dkLen, 32, HMAC::sha256)
            "sha512" -> pbkdf2(Strings.toByteArray(password), salt, iterations, dkLen, 64, HMAC::sha512)
            else -> throw IllegalArgumentException("pseudorandom function $prf is not supported")
        }
    }

    fun verify(prf: String, password: String, hashed: ByteArray, salt: ByteArray, iterations: Int, dkLen: Int): Boolean {

        val verify = when (prf) {
            "sha1" -> pbkdf2(Strings.toByteArray(password), salt, iterations, dkLen, 20, HMAC::sha1)
            "sha256" -> pbkdf2(Strings.toByteArray(password), salt, iterations, dkLen, 32, HMAC::sha256)
            "sha512" -> pbkdf2(Strings.toByteArray(password), salt, iterations, dkLen, 64, HMAC::sha512)
            else -> throw IllegalArgumentException("pseudorandom function $prf is not supported")
        }

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

    internal fun pbkdf2(password: ByteArray, salt: ByteArray, iterations: Int, dkLen: Int, hLen: Int, hmacFunction: (key: ByteArray, message: ByteArray) -> ByteArray): ByteArray {

        if (dkLen > (((2.0).pow(32.0)) - 1) * hLen) {
            throw IllegalArgumentException("derived key is too long")
        }

        val l = ceil(dkLen, hLen)
        val r = dkLen - (l - 1) * hLen
        val t = ByteArray(l * hLen)
        var offset = 0

        for (blockNum in 1..l) {
            f(t, password, hmacFunction, salt, iterations, blockNum, offset, hLen)
            offset += hLen
        }

        if (r < hLen) {
            val derivedKey = ByteArray(dkLen)
            Arrays.arraycopy(t, 0, derivedKey, 0, dkLen)
            return derivedKey
        }
        return t
    }

    private fun ceil(a: Int, b: Int): Int {
        val erg = a / b
        return if (a % b > 0) {
            erg + 1
        } else erg
    }

    private fun int(dest: ByteArray, offset: Int, i: Int) {
        dest[offset + 0] = (i shr 24 and 0xff).toByte()
        dest[offset + 1] = (i shr 16 and 0xff).toByte()
        dest[offset + 2] = (i shr 8 and 0xff).toByte()
        dest[offset + 3] = (i shr 0 and 0xff).toByte()
    }

    private fun xor(dest: ByteArray, src: ByteArray) {
        for (i in dest.indices) {
            dest[i] = dest[i] xor src[i]
        }
    }

    private fun f(dest: ByteArray, password: ByteArray, hmacFunction: (key: ByteArray, message: ByteArray) -> ByteArray, salt: ByteArray, iterations: Int, index: Int, offset: Int, hLen: Int) {

        val uc = ByteArray(hLen)
        var ui = salt.copyOf(salt.size + 4)
        int(ui, salt.size, index)

        for (i in 0 until iterations) {
            ui = hmacFunction(password, ui)
            xor(uc, ui)
        }
        Arrays.arraycopy(uc, 0, dest, offset, hLen)
    }
}