package com.github.mitallast.ghost.crypto.pkc

import com.github.mitallast.ghost.crypto.utils.BigInteger
import com.github.mitallast.ghost.crypto.utils.Strings.fromByteArray
import com.github.mitallast.ghost.crypto.utils.Strings.toByteArray
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class RSATest {

    @Test
    fun signAndVerifyMD5() {
        val keyPair = RSA.keyPair(1024)
        val signed = RSA.sign(toByteArray("Message to sign"), keyPair.privateKey, RSA.SIGH.MD5)
        assertTrue(RSA.verify(toByteArray("Message to sign"), signed, keyPair.publicKey, RSA.SIGH.MD5))
    }

    @Test
    fun signAndVerifySHA1() {
        val keyPair = RSA.keyPair(1024)
        val signed = RSA.sign(toByteArray("Message to sign"), keyPair.privateKey, RSA.SIGH.SHA1)
        assertTrue(RSA.verify(toByteArray("Message to sign"), signed, keyPair.publicKey, RSA.SIGH.SHA1))
    }

    @Test
    fun signAndVerifySHA256() {
        val keyPair = RSA.keyPair(1024)
        val signed = RSA.sign(toByteArray("Message to sign"), keyPair.privateKey)
        assertTrue(RSA.verify(toByteArray("Message to sign"), signed, keyPair.publicKey))
    }

    @Test
    fun signAndVerifySHA512() {
        val keyPair = RSA.keyPair(1024)
        val signed = RSA.sign(toByteArray("Message to sign"), keyPair.privateKey, RSA.SIGH.SHA512)
        assertTrue(RSA.verify(toByteArray("Message to sign"), signed, keyPair.publicKey, RSA.SIGH.SHA512))
    }

    @Test
    fun invalidEScheme() {
        var ex: Exception? = null
        try {
            val keyPair = RSA.keyPair(1024)
            RSA.encrypt(toByteArray(""), keyPair.publicKey, RSA.RSAES.OAEP)
        } catch (e: IllegalArgumentException) {
            ex = e
        }
        assertNotNull(ex)
    }

    @Test
    fun invalidDScheme() {
        var ex: Exception? = null
        try {
            val keyPair = RSA.keyPair(1024)
            RSA.decrypt(toByteArray(""), keyPair.privateKey, RSA.RSAES.OAEP)
        } catch (e: IllegalArgumentException) {
            ex = e
        }
        assertNotNull(ex)
    }

    @Test
    fun i2ospError() {
        var ex: Exception? = null
        try {
            RSA.i2osp(BigInteger.TEN, 0)
        } catch (e: IllegalArgumentException) {
            ex = e
        }
        assertNotNull(ex)
    }

    @Test
    fun encryptError1() {
        var ex: Exception? = null
        try {
            val tobig = ByteArray(257, { i -> (i + 1).toByte() })
            val keyPair = RSA.keyPair(1024)
            RSA.encrypt(tobig, keyPair.publicKey)
        } catch (e: IllegalArgumentException) {
            ex = e
        }
        assertNotNull(ex)
    }

    @Test
    fun encryptError2() {
        var ex: Exception? = null
        try {
            val tobig = ByteArray(257, { i -> (i + 1).toByte() })
            val keyPair = RSA.keyPair(1024)
            RSA.decrypt(tobig, keyPair.privateKey)
        } catch (e: IllegalArgumentException) {
            ex = e
        }
        assertNotNull(ex)
    }

    @Test
    fun encrypt1024() {
        val plain = "Schweißgequält zündet Typograf Jakob verflixt öde Pangramme an."
        val keyPair = RSA.keyPair(1024)
        val enc = RSA.encrypt(toByteArray(plain), keyPair.publicKey)
        assertEquals(plain, fromByteArray(RSA.decrypt(enc, keyPair.privateKey)))
    }

    @Test
    fun encrypt2048() {
        val plain = "Schweißgequält zündet Typograf Jakob verflixt öde Pangramme an."
        val keyPair = RSA.keyPair(2048)
        val enc = RSA.encrypt(toByteArray(plain), keyPair.publicKey)
        assertEquals(plain, fromByteArray(RSA.decrypt(enc, keyPair.privateKey)))
    }

    @Test
    fun encrypt4096() {
        val plain = "Schweißgequält zündet Typograf Jakob verflixt öde Pangramme an."
        val keyPair = RSA.keyPair(4096)
        val source = toByteArray(plain)
        val enc = RSA.encrypt(source, keyPair.publicKey)
        val dec = RSA.decrypt(enc, keyPair.privateKey)
        val res = fromByteArray(dec)
        assertEquals(plain, res)
    }
}