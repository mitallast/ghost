package com.github.mitallast.ghost.crypto.skc

import com.github.mitallast.ghost.crypto.dec.Hex
import com.github.mitallast.ghost.crypto.utils.Keys
import com.github.mitallast.ghost.crypto.utils.Strings.fromByteArray
import com.github.mitallast.ghost.crypto.utils.Strings.toByteArray
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class TwofishTest {

    @Test
    fun encryptECB() {

        assertEquals("5449eca008ff5921155f598af4ced4d0",
            Hex.encode(Twofish.encryptECB(Hex.toByteArray("816D5BD0FAE35342BF2A7412C246F752"), Hex.toByteArray("6363977DE839486297E661C6C9D668EB"))))

        assertEquals("ae8109bfda85c1f2c5038b34ed691bff",
            Hex.encode(Twofish.encryptECB(Hex.toByteArray("3AF6F7CE5BD35EF18BEC6FA787AB506B"), Hex.toByteArray("D1079B789F666649B6BD7D1629F1F77E7AFF7A70CA2FF28A"))))

        assertEquals("e69465770505d7f80ef68ca38ab3a3d6",
            Hex.encode(Twofish.encryptECB(Hex.toByteArray("3059D6D61753B958D92F4781C8640E58"), Hex.toByteArray("6CB4561C40BF0A9705931CB6D408E7FA90AFE91BB288544F2C32DC239B2635E6"))))

        val plain = "Schweißgequält zündet Typograf Jakob verflixt öde Pangramme an."
        val key128 = ByteArray(16, { i -> (i + 1).toByte() })
        val key192 = ByteArray(24, { i -> (i + 1).toByte() })
        val key256 = ByteArray(32, { i -> (i + 1).toByte() })

        assertEquals("e762cda24a9aebede80e709a5e41dd2093a7f974fc93a553b777d053d1c58e34eefb534724c26bd914c30a6e7dcc38e98f4415e0b3cdc47b55cb60612380017f92f2fc3b2e49ba9d0d43267adcb14c97",
            Hex.encode(Twofish.encryptECB(toByteArray(plain), key128, Padding.SCHEME.PKCS7)))

        assertEquals("41d1cfc7f0d3e33c959f34fda62863c0812c794d87f04c6c2077cd13c18131c50ae6e0890525ebc9140ec9f118c6f4d3e7a12361f3ab64a4fa15965b1633507d645c8362074bb259c59f40cf2e795948",
            Hex.encode(Twofish.encryptECB(toByteArray(plain), key192, Padding.SCHEME.PKCS7)))

        assertEquals("cedfd97f116346c49373811e194d1a72496b16d1653f01ed23118721ddd8c2a059f9f1f56e94f94c7e6fa644490f17d2fbb6574168eed5c6e9ceddf37776db9b49bbc24754993ed2aca896cdfda8f398",
            Hex.encode(Twofish.encryptECB(toByteArray(plain), key256, Padding.SCHEME.PKCS7)))
    }

    @Test
    fun encryptCBC() {

        val iv = ByteArray(16, { i -> (i + 1).toByte() })

        assertEquals("12e1fe58c30497ba5a451171d9c1d5e1",
            Hex.encode(Twofish.encryptCBC(Hex.toByteArray("816D5BD0FAE35342BF2A7412C246F752"), Hex.toByteArray("6363977DE839486297E661C6C9D668EB"), iv)))

        assertEquals("6ed0c83a75bb4f2ec79a339895b2ba8f",
            Hex.encode(Twofish.encryptCBC(Hex.toByteArray("3AF6F7CE5BD35EF18BEC6FA787AB506B"), Hex.toByteArray("D1079B789F666649B6BD7D1629F1F77E7AFF7A70CA2FF28A"), iv)))

        assertEquals("5ea30253ac4635b59f1457f4536b542e",
            Hex.encode(Twofish.encryptCBC(Hex.toByteArray("3059D6D61753B958D92F4781C8640E58"), Hex.toByteArray("6CB4561C40BF0A9705931CB6D408E7FA90AFE91BB288544F2C32DC239B2635E6"), iv)))

        val plain = "Schweißgequält zündet Typograf Jakob verflixt öde Pangramme an."
        val key128 = ByteArray(16, { i -> (i + 1).toByte() })
        val key192 = ByteArray(24, { i -> (i + 1).toByte() })
        val key256 = ByteArray(32, { i -> (i + 1).toByte() })

        assertEquals("f985b5e1709daaed363399739ded336bd8ce52688de081228d4a78988e4a77d30b6c67501e0cfdffd562437e7d997bf9064cb4f359530f690f758be5293a597d90ddba5a199e4523715e8c4cb91de236",
            Hex.encode(Twofish.encryptCBC(toByteArray(plain), key128, iv, Padding.SCHEME.PKCS7)))

        assertEquals("11a96e351a9b3c63d995d350971e8f17cc1e2e0973c4ebe5830bd2bdd55fea5a1c7191f4fe226a3cfe386a7a7003269b08dd81140d78255f0f850c727f942667b727c1126180b7249196ec13ce34f2a2",
            Hex.encode(Twofish.encryptCBC(toByteArray(plain), key192, iv, Padding.SCHEME.PKCS7)))

        assertEquals("a080b9f2f3d23a6d15ba0d88845bfbfb7655ddee03c239f3f4b8b0952e6aecb2b6ca79261abec23eb7ab12a27207317ae3f0f750716f665dd6b9e4434a12c8a169e33e2f78ba720613161a5a013e5085",
            Hex.encode(Twofish.encryptCBC(toByteArray(plain), key256, iv, Padding.SCHEME.PKCS7)))
    }

    @Test
    fun decryptECB() {

        assertEquals("816d5bd0fae35342bf2a7412c246f752",
            Hex.encode(Twofish.decryptECB(Hex.toByteArray("5449eca008ff5921155f598af4ced4d0"), Hex.toByteArray("6363977DE839486297E661C6C9D668EB"))))

        assertEquals("3af6f7ce5bd35ef18bec6fa787ab506b",
            Hex.encode(Twofish.decryptECB(Hex.toByteArray("ae8109bfda85c1f2c5038b34ed691bff"), Hex.toByteArray("D1079B789F666649B6BD7D1629F1F77E7AFF7A70CA2FF28A"))))

        assertEquals("3059d6d61753b958d92f4781c8640e58",
            Hex.encode(Twofish.decryptECB(Hex.toByteArray("e69465770505d7f80ef68ca38ab3a3d6"), Hex.toByteArray("6CB4561C40BF0A9705931CB6D408E7FA90AFE91BB288544F2C32DC239B2635E6"))))

        val plain = "Schweißgequält zündet Typograf Jakob verflixt öde Pangramme an."
        val key128 = ByteArray(16, { i -> (i + 1).toByte() })
        val key192 = ByteArray(24, { i -> (i + 1).toByte() })
        val key256 = ByteArray(32, { i -> (i + 1).toByte() })

        assertEquals(plain, fromByteArray(Twofish.decryptECB(
            Hex.toByteArray("e762cda24a9aebede80e709a5e41dd2093a7f974fc93a553b777d053d1c58e34eefb534724c26bd914c30a6e7dcc38e98f4415e0b3cdc47b55cb60612380017f92f2fc3b2e49ba9d0d43267adcb14c97"),
            key128, Padding.SCHEME.PKCS7)))

        assertEquals(plain, fromByteArray(Twofish.decryptECB(
            Hex.toByteArray("41d1cfc7f0d3e33c959f34fda62863c0812c794d87f04c6c2077cd13c18131c50ae6e0890525ebc9140ec9f118c6f4d3e7a12361f3ab64a4fa15965b1633507d645c8362074bb259c59f40cf2e795948"),
            key192, Padding.SCHEME.PKCS7)))

        assertEquals(plain, fromByteArray(Twofish.decryptECB(
            Hex.toByteArray("cedfd97f116346c49373811e194d1a72496b16d1653f01ed23118721ddd8c2a059f9f1f56e94f94c7e6fa644490f17d2fbb6574168eed5c6e9ceddf37776db9b49bbc24754993ed2aca896cdfda8f398"),
            key256, Padding.SCHEME.PKCS7)))

        var ciphertext = Twofish.encryptECB(toByteArray(plain), key128, Padding.SCHEME.PKCS7)
        assertEquals(plain, fromByteArray(Twofish.decryptECB(ciphertext, key128, Padding.SCHEME.PKCS7)))

        ciphertext = Twofish.encryptECB(toByteArray(plain), key192, Padding.SCHEME.PKCS7)
        assertEquals(plain, fromByteArray(Twofish.decryptECB(ciphertext, key192, Padding.SCHEME.PKCS7)))

        ciphertext = Twofish.encryptECB(toByteArray(plain), key256, Padding.SCHEME.PKCS7)
        assertEquals(plain, fromByteArray(Twofish.decryptECB(ciphertext, key256, Padding.SCHEME.PKCS7)))
    }

    @Test
    fun decryptCBC() {

        val iv = ByteArray(16, { i -> (i + 1).toByte() })

        assertEquals("816d5bd0fae35342bf2a7412c246f752",
            Hex.encode(Twofish.decryptCBC(Hex.toByteArray("12e1fe58c30497ba5a451171d9c1d5e1"), Hex.toByteArray("6363977DE839486297E661C6C9D668EB"), iv)))

        assertEquals("3af6f7ce5bd35ef18bec6fa787ab506b",
            Hex.encode(Twofish.decryptCBC(Hex.toByteArray("6ed0c83a75bb4f2ec79a339895b2ba8f"), Hex.toByteArray("D1079B789F666649B6BD7D1629F1F77E7AFF7A70CA2FF28A"), iv)))

        assertEquals("3059d6d61753b958d92f4781c8640e58",
            Hex.encode(Twofish.decryptCBC(Hex.toByteArray("5ea30253ac4635b59f1457f4536b542e"), Hex.toByteArray("6CB4561C40BF0A9705931CB6D408E7FA90AFE91BB288544F2C32DC239B2635E6"), iv)))

        val plain = "Schweißgequält zündet Typograf Jakob verflixt öde Pangramme an."
        var key128 = ByteArray(16, { i -> (i + 1).toByte() })
        var key192 = ByteArray(24, { i -> (i + 1).toByte() })
        var key256 = ByteArray(32, { i -> (i + 1).toByte() })

        assertEquals(plain, fromByteArray(Twofish.decryptCBC(
            Hex.toByteArray("f985b5e1709daaed363399739ded336bd8ce52688de081228d4a78988e4a77d30b6c67501e0cfdffd562437e7d997bf9064cb4f359530f690f758be5293a597d90ddba5a199e4523715e8c4cb91de236"),
            key128, iv, Padding.SCHEME.PKCS7)))

        assertEquals(plain, fromByteArray(Twofish.decryptCBC(
            Hex.toByteArray("11a96e351a9b3c63d995d350971e8f17cc1e2e0973c4ebe5830bd2bdd55fea5a1c7191f4fe226a3cfe386a7a7003269b08dd81140d78255f0f850c727f942667b727c1126180b7249196ec13ce34f2a2"),
            key192, iv, Padding.SCHEME.PKCS7)))

        assertEquals(plain, fromByteArray(Twofish.decryptCBC(
            Hex.toByteArray("a080b9f2f3d23a6d15ba0d88845bfbfb7655ddee03c239f3f4b8b0952e6aecb2b6ca79261abec23eb7ab12a27207317ae3f0f750716f665dd6b9e4434a12c8a169e33e2f78ba720613161a5a013e5085"),
            key256, iv, Padding.SCHEME.PKCS7)))

        key128 = Keys.randomKey(16)
        key192 = Keys.randomKey(24)
        key256 = Keys.randomKey(32)

        var ciphertext = Twofish.encryptECB(toByteArray(plain), key128, Padding.SCHEME.PKCS7)
        assertEquals(plain, fromByteArray(Twofish.decryptECB(ciphertext, key128, Padding.SCHEME.PKCS7)))

        ciphertext = Twofish.encryptECB(toByteArray(plain), key192, Padding.SCHEME.PKCS7)
        assertEquals(plain, fromByteArray(Twofish.decryptECB(ciphertext, key192, Padding.SCHEME.PKCS7)))

        ciphertext = Twofish.encryptECB(toByteArray(plain), key256, Padding.SCHEME.PKCS7)
        assertEquals(plain, fromByteArray(Twofish.decryptECB(ciphertext, key256, Padding.SCHEME.PKCS7)))
    }

    @Test
    fun invalidPlaintextSize() {
        var ex: Exception? = null
        try {
            Twofish.encryptECB(toByteArray("123"), toByteArray("456"))
        } catch (e: Exception) {
            ex = e
        }
        assertNotNull(ex)
    }

    @Test
    fun invalidKeySizeToSmall() {
        var ex: Exception? = null
        try {
            Twofish.checkKey(Keys.randomKey(7))
        } catch (e: Exception) {
            ex = e
        }
        assertNotNull(ex)
    }

    @Test
    fun invalidKeySizeToBig() {
        var ex: Exception? = null
        try {
            Twofish.checkKey(Keys.randomKey(33))
        } catch (e: Exception) {
            ex = e
        }
    }

    @Test
    fun invalidKeySizeModulo8() {
        var ex: Exception? = null
        try {
            Twofish.checkKey(Keys.randomKey(20))
        } catch (e: Exception) {
            ex = e
        }
    }

    @Test
    fun validKeySize() {
        Twofish.checkKey(Keys.randomKey(8))
        Twofish.checkKey(Keys.randomKey(16))
        Twofish.checkKey(Keys.randomKey(24))
        Twofish.checkKey(Keys.randomKey(32))
    }
}