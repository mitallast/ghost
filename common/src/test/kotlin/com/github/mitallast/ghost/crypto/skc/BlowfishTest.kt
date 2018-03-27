package com.github.mitallast.ghost.crypto.skc

import com.github.mitallast.ghost.crypto.dec.Hex
import com.github.mitallast.ghost.crypto.utils.Strings.fromByteArray
import com.github.mitallast.ghost.crypto.utils.Strings.toByteArray
import kotlin.test.Test
import kotlin.test.assertEquals

class BlowfishTest {

    @Test
    fun encrypt() {

        val plain = "Schweißgequält zündet Typograf Jakob verflixt öde Pangramme an."
        val key128 = ByteArray(16, { i -> (i + 1).toByte() })
        val key256 = ByteArray(32, { i -> (i + 1).toByte() })
        val key448 = ByteArray(56, { i -> (i + 1).toByte() })
        val iv = ByteArray(8, { i -> (i + 1).toByte() })

        assertEquals("ca5d938bba4e8e1b6157508bc7ad4c5abf13113ce7ef7edaed24444cc73578f2c6f8fcfda72d0b1781844776ae9497c22ad4581b074bbddb9b66bf0e3d5a4bfb6ad60ba8bc056385",
                Hex.encode(Blowfish.encryptECB(toByteArray(plain), key128, Padding.SCHEME.PKCS5)))

        assertEquals("ed02815de3b8269653650aeb1321daa40aa053ecb4255c3d3c130995d0a06d58c763b331ae91a2d63fbe9725ef8e4d0b1630ee4cf7f173918a42b7f3e0f12ca72fcac23068f730ad",
                Hex.encode(Blowfish.encryptCBC(toByteArray(plain), key128, iv, Padding.SCHEME.PKCS5)))

        assertEquals("b9e7f10eb35b90cec9ed85d89511e26011bf1ef42b5e09edf87cb5bad7894589b7e109070366e51a931627fd320f2cc324f8558b198db6f22fde97bf9fa86faca7ed75cb4da7348d",
                Hex.encode(Blowfish.encryptECB(toByteArray(plain), key256, Padding.SCHEME.PKCS5)))

        assertEquals("0b4e6cf4c48e0107b1741bd25f2d0d69e03cd46913d539ba6078789dd71c21e6bb18680e442d69b42dc3615f799056d9c3e208474c96ecf8aa6376587987075e207a1c1274ce41e8",
                Hex.encode(Blowfish.encryptCBC(toByteArray(plain), key256, iv, Padding.SCHEME.PKCS5)))

        assertEquals("5b40583865d085f0c9dcb97f4e6ebd30610905f40cae7547201350bb6e28e8d9f3fe6cd338bb0f61b9dbec5159d1a5b32bb4b2cb8b58384b6dbc0a4ed4dea13939f9e715642222cd",
                Hex.encode(Blowfish.encryptECB(toByteArray(plain), key448, Padding.SCHEME.PKCS5)))

        assertEquals("1d7774606210939f9616dde23eb7da294a890a8da7c0d742610dbeadb46ee619a70db0d25183a04be764cc3528f71d1ae35be1151b6043a334d28acf4b3713e4d97bfd579e176362",
                Hex.encode(Blowfish.encryptCBC(toByteArray(plain), key448, iv, Padding.SCHEME.PKCS5)))
    }

    @Test
    fun decrypt() {

        val plain = "Schweißgequält zündet Typograf Jakob verflixt öde Pangramme an."
        val key128 = ByteArray(16, { i -> (i + 1).toByte() })
        val key256 = ByteArray(32, { i -> (i + 1).toByte() })
        val key448 = ByteArray(56, { i -> (i + 1).toByte() })
        val iv = ByteArray(8, { i -> (i + 1).toByte() })

        var ciphertext = Blowfish.encryptECB(toByteArray(plain), key128, Padding.SCHEME.PKCS5)
        assertEquals(plain, fromByteArray(Blowfish.decryptECB(ciphertext, key128, Padding.SCHEME.PKCS5)))

        ciphertext = Blowfish.encryptECB(toByteArray(plain), key256, Padding.SCHEME.PKCS5)
        assertEquals(plain, fromByteArray(Blowfish.decryptECB(ciphertext, key256, Padding.SCHEME.PKCS5)))

        ciphertext = Blowfish.encryptECB(toByteArray(plain), key448, Padding.SCHEME.PKCS5)
        assertEquals(plain, fromByteArray(Blowfish.decryptECB(ciphertext, key448, Padding.SCHEME.PKCS5)))

        ciphertext = Blowfish.encryptCBC(toByteArray(plain), key128, iv, Padding.SCHEME.PKCS5)
        assertEquals(plain, fromByteArray(Blowfish.decryptCBC(ciphertext, key128, iv, Padding.SCHEME.PKCS5)))

        ciphertext = Blowfish.encryptCBC(toByteArray(plain), key256, iv, Padding.SCHEME.PKCS5)
        assertEquals(plain, fromByteArray(Blowfish.decryptCBC(ciphertext, key256, iv, Padding.SCHEME.PKCS5)))

        ciphertext = Blowfish.encryptCBC(toByteArray(plain), key448, iv, Padding.SCHEME.PKCS5)
        assertEquals(plain, fromByteArray(Blowfish.decryptCBC(ciphertext, key448, iv, Padding.SCHEME.PKCS5)))
    }

    @Test
    fun encryptECB128() {

        assertEquals("6b5c5a9c5d9e0a5a",
                Hex.encode(Blowfish.encryptECB(Hex.toByteArray("ffffffffffffffff"), Hex.toByteArray("fedcba9876543210"))))

        assertEquals("a25e7856cf2651eb",
                Hex.encode(Blowfish.encryptECB(Hex.toByteArray("51454b582ddf440a"), Hex.toByteArray("3849674c2602319e"))))

        assertEquals("ffffffffffffffff",
                Hex.encode(Blowfish.decryptECB(Hex.toByteArray("6b5c5a9c5d9e0a5a"), Hex.toByteArray("fedcba9876543210"))))

        assertEquals("51454b582ddf440a",
                Hex.encode(Blowfish.decryptECB(Hex.toByteArray("a25e7856cf2651eb"), Hex.toByteArray("3849674c2602319e"))))

    }

    @Test
    fun encryptCBC128() {

        assertEquals("7d9d0f14f0177e6235e32f0c97b37a5f",
                Hex.encode(Blowfish.encryptCBC(
                        Hex.toByteArray("452031C1E4FADA8E0FDDDE1A8A5B83F0"),
                        Hex.toByteArray("584023641ABA6176"),
                        Hex.toByteArray("004BD6EF09176062"))))

        assertEquals("73e184b02df518aad1df9a37054586ad",
                Hex.encode(Blowfish.encryptCBC(
                        Hex.toByteArray("7555AE39F59B87BDFEBD1D7E17CBCA41"),
                        Hex.toByteArray("025816164629B007"),
                        Hex.toByteArray("480D39006EE762F2"))))
    }
}