package com.github.mitallast.ghost.crypto.skc

import com.github.mitallast.ghost.crypto.dec.Base64
import com.github.mitallast.ghost.crypto.dec.Hex
import com.github.mitallast.ghost.crypto.utils.Keys
import com.github.mitallast.ghost.crypto.utils.Strings.toByteArray
import com.github.mitallast.ghost.crypto.utils.Strings.fromByteArray
import kotlin.test.Test
import kotlin.test.assertEquals

class AESTest {

    @Test
    fun encryptECB() {
        val plain = "Schweißgequält zündet Typograf Jakob verflixt öde Pangramme an."

        val key128 = ByteArray(16, { i -> (i + 1).toByte() })
        assertEquals("EX9Bx5K388OQwq7FFTrQ2h/7QV1LXlhHvuBMhAlyb8Jrw/L1ZSxhxRbqhsIjbhJBdFcmgU9v4p8zEvEK41lylGHAsPEETReqkgtjH9lmHR0=",
                Base64.encode(AES.encryptECB(toByteArray(plain), key128, Padding.SCHEME.PKCS7)))

        val key256 = ByteArray(32, { i -> (i + 1).toByte() })
        assertEquals("u5PoaqnYSGzB4F8NLZ/hiNj03nAsSmnMHeNSYNTYsoAxMu9vAIfCehhSrPsQ85NlY7HbzlKafYQi61DmUQ6OcAl/Rm7oRQUp839LAmDz8bw=",
                Base64.encode(AES.encryptECB(toByteArray(plain), key256, Padding.SCHEME.PKCS7)))
    }

    @Test
    fun decryptECB() {

        val plain = "Schweißgequält zündet Typograf Jakob verflixt öde Pangramme an."

        val key128 = Keys.randomKey(16)
        assertEquals(plain, fromByteArray(AES.decryptECB(AES.encryptECB(toByteArray(plain), key128, Padding.SCHEME.PKCS7), key128, Padding.SCHEME.PKCS7)))

        val key256 = Keys.randomKey(32)
        assertEquals(plain, fromByteArray(AES.decryptECB(AES.encryptECB(toByteArray(plain), key256, Padding.SCHEME.PKCS7), key256, Padding.SCHEME.PKCS7)))
    }

    @Test
    fun encryptCBC() {

        val plain = "Schweißgequält zündet Typograf Jakob verflixt öde Pangramme an."
        val iv = ByteArray(16, { i -> (i + 1).toByte() })

        val key128 = ByteArray(16, { i -> (i + 1).toByte() })
        assertEquals("AAcOey3Dkx7fnWhTAOlYGTPNuMdHOcIKuV+jzjKq+gkJH3QtAFsBNF65MUr00bO50gd/QGMbVNUFNFg0c4UtFtbI0FpEbpuJ+deWpbuRryA=",
                Base64.encode(AES.encryptCBC(toByteArray(plain), key128, iv, Padding.SCHEME.PKCS7)))

        val key256 = ByteArray(32, { i -> (i + 1).toByte() })
        assertEquals("GLmNf0fBrhhGh0h7YDAhEpuxkuG7K5whxuJM3nSFxEfbql/WwPpgCmc1h4+O4qbqLCHLVZKmQE/WK4B+B/TDj/eEATOAHuMJbIKAtvQU5SE=",
                Base64.encode(AES.encryptCBC(toByteArray(plain), key256, iv, Padding.SCHEME.PKCS7)))
    }

    @Test
    fun decryptCBC() {

        val plain = "Schweißgequält zündet Typograf Jakob verflixt öde Pangramme an."
        val iv = Keys.randomKey(16)

        val key128 = Keys.randomKey(16)
        assertEquals(plain, fromByteArray(AES.decryptCBC(AES.encryptCBC(toByteArray(plain), key128, iv, Padding.SCHEME.PKCS7), key128, iv, Padding.SCHEME.PKCS7)))

        val key256 = Keys.randomKey(32)
        assertEquals(plain, fromByteArray(AES.decryptCBC(AES.encryptCBC(toByteArray(plain), key256, iv, Padding.SCHEME.PKCS7), key256, iv, Padding.SCHEME.PKCS7)))
    }

    @Test
    fun encryptCTR() {
        val plain = Hex.toByteArray("6bc1bee22e409f96e93d7e117393172a")
        val iv = Hex.toByteArray("f0f1f2f3f4f5f6f7f8f9fafbfcfdfeff")

        val key128 = Hex.toByteArray("2b7e151628aed2a6abf7158809cf4f3c")
        assertEquals("874d6191b620e3261bef6864990db6ce", Hex.encode(AES.encryptCTR(plain, key128, iv)))

        val key192 = Hex.toByteArray("8e73b0f7da0e6452c810f32b809079e562f8ead2522c6b7b")
        assertEquals("1abc932417521ca24f2b0459fe7e6e0b", Hex.encode(AES.encryptCTR(plain, key192, iv)))

        val key256 = Hex.toByteArray("603deb1015ca71be2b73aef0857d77811f352c073b6108d72d9810a30914dff4")
        assertEquals("601ec313775789a5b7a7f504bbf3d228", Hex.encode(AES.encryptCTR(plain, key256, iv)))
    }

    @Test
    fun decryptCTR() {
        var ciphertext = Hex.toByteArray("874d6191b620e3261bef6864990db6ce")
        val iv = Hex.toByteArray("f0f1f2f3f4f5f6f7f8f9fafbfcfdfeff")

        val key128 = Hex.toByteArray("2b7e151628aed2a6abf7158809cf4f3c")
        assertEquals("6bc1bee22e409f96e93d7e117393172a", Hex.encode(AES.decryptCTR(ciphertext, key128, iv)))

        ciphertext = Hex.toByteArray("1abc932417521ca24f2b0459fe7e6e0b")
        val key192 = Hex.toByteArray("8e73b0f7da0e6452c810f32b809079e562f8ead2522c6b7b")
        assertEquals("6bc1bee22e409f96e93d7e117393172a", Hex.encode(AES.decryptCTR(ciphertext, key192, iv)))
    }

    @Test
    fun encryptDecryptCTRWithPadding() {

        val plain = "Schweißgequält zündet Typograf Jakob verflixt öde Pangramme an."
        val key128 = Keys.randomKey(16)
        val nonce = Keys.randomKey(16)

        assertEquals(plain, fromByteArray(AES.decryptCTR(
                AES.encryptCTR(toByteArray(plain), key128, nonce, Padding.SCHEME.PKCS7), key128, nonce, Padding.SCHEME.PKCS7)))

        val key192 = Keys.randomKey(24)
        assertEquals(plain, fromByteArray(AES.decryptCTR(
                AES.encryptCTR(toByteArray(plain), key192, nonce, Padding.SCHEME.PKCS7), key192, nonce, Padding.SCHEME.PKCS7)))

        val key256 = Keys.randomKey(32)
        assertEquals(plain, fromByteArray(AES.decryptCTR(
                AES.encryptCTR(toByteArray(plain), key256, nonce, Padding.SCHEME.PKCS7), key256, nonce, Padding.SCHEME.PKCS7)))
    }

    @Test
    fun encryptDecryptCTRWithoutPadding() {

        val plain = "Schweißgequält zündet Typograf Jakob verflixt öde Pangramme an."
        val key128 = Keys.randomKey(16)
        val nonce = Keys.randomKey(16)

        assertEquals(plain, fromByteArray(AES.decryptCTR(
                AES.encryptCTR(toByteArray(plain), key128, nonce), key128, nonce)))

        val key192 = Keys.randomKey(24)
        assertEquals(plain, fromByteArray(AES.decryptCTR(
                AES.encryptCTR(toByteArray(plain), key192, nonce), key192, nonce)))

        val key256 = Keys.randomKey(32)
        assertEquals(plain, fromByteArray(AES.decryptCTR(
                AES.encryptCTR(toByteArray(plain), key256, nonce), key256, nonce)))
    }
}