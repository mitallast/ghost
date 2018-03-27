package com.github.mitallast.ghost.crypto.skc

import com.github.mitallast.ghost.crypto.dec.Hex
import com.github.mitallast.ghost.crypto.utils.Keys
import com.github.mitallast.ghost.crypto.utils.Strings.fromByteArray
import com.github.mitallast.ghost.crypto.utils.Strings.toByteArray
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class SerpentTest {

    @Test
    fun encryptECB() {

        assertEquals("264e5481eff42a4606abda06c0bfda3d",
            Hex.encode(Serpent.encryptECB(Hex.toByteArray("00000000000000000000000000000000"), Hex.toByteArray("80000000000000000000000000000000"))))

        assertEquals("563e2cf8740a27c164804560391e9b27",
            Hex.encode(Serpent.encryptECB(Hex.toByteArray("00112233445566778899AABBCCDDEEFF"), Hex.toByteArray("000102030405060708090A0B0C0D0E0F"))))

        assertEquals("9e274ead9b737bb21efcfca548602689",
            Hex.encode(Serpent.encryptECB(Hex.toByteArray("00000000000000000000000000000000"), Hex.toByteArray("800000000000000000000000000000000000000000000000"))))

        assertEquals("ea024714ad5c4d84ea024714ad5c4d84",
            Hex.encode(Serpent.encryptECB(Hex.toByteArray("E0208BE278E21420C4B1B9747788A954"), Hex.toByteArray("2BD6459F82C5B300952C49104881FF482BD6459F82C5B300"))))

        assertEquals("a223aa1288463c0e2be38ebd825616c0",
            Hex.encode(Serpent.encryptECB(Hex.toByteArray("00000000000000000000000000000000"), Hex.toByteArray("8000000000000000000000000000000000000000000000000000000000000000"))))

        assertEquals("ea024714ad5c4d84ea024714ad5c4d84",
            Hex.encode(Serpent.encryptECB(Hex.toByteArray("677C8DFAA08071743FD2B415D1B28AF2"), Hex.toByteArray("2BD6459F82C5B300952C49104881FF482BD6459F82C5B300952C49104881FF48"))))

        val plain = "Schweißgequält zündet Typograf Jakob verflixt öde Pangramme an."
        val key128 = ByteArray(16, { i -> (i + 1).toByte() })
        val key192 = ByteArray(24, { i -> (i + 1).toByte() })
        val key256 = ByteArray(32, { i -> (i + 1).toByte() })

        assertEquals("c5e01c86f1bd5ce22b1e1874d5b5711a5cec3eac4de6e85bcf175d4e13a0dff2156e141c35a0cb59535d5079c160b65a82bb1008849031e26ac70fdb5343ada310e63daf15d229c25aa91870c869caf7",
            Hex.encode(Serpent.encryptECB(toByteArray(plain), key128, Padding.SCHEME.PKCS7)))

        assertEquals("dc89e697db514daa303ed9f4bd70cf32416dd5013b18458ed5b350451e8f6bcfd428071e13f4d7e8042ece6ac640cd0ed6ca4115d94ed9528ab91f0e9192244feac70b970d9acc2b88040a1ae3124964",
            Hex.encode(Serpent.encryptECB(toByteArray(plain), key192, Padding.SCHEME.PKCS7)))

        assertEquals("7b09d46d4eede733fb6f0d946bd8938ee424004d3c5badbed2958ddb79c5af543adb387f1c05227e1c7569a5fc97f86e16541de08859aac77f61108cf96cfdd4f16e41d9f2a3a59fc1becfddd8219e60",
            Hex.encode(Serpent.encryptECB(toByteArray(plain), key256, Padding.SCHEME.PKCS7)))
    }

    @Test
    fun encryptCBC() {

        val iv = ByteArray(16, { i -> (i + 1).toByte() })

        assertEquals("0509721d767068a2be6537c55f2b1093",
            Hex.encode(Serpent.encryptCBC(Hex.toByteArray("00000000000000000000000000000000"), Hex.toByteArray("80000000000000000000000000000000"), iv)))

        assertEquals("51638e30889f25459322a11d46f0d83f",
            Hex.encode(Serpent.encryptCBC(Hex.toByteArray("00112233445566778899AABBCCDDEEFF"), Hex.toByteArray("000102030405060708090A0B0C0D0E0F"), iv)))

        assertEquals("ce52c1b09f899b8f60e13ec7d23cb855",
            Hex.encode(Serpent.encryptCBC(Hex.toByteArray("00000000000000000000000000000000"), Hex.toByteArray("800000000000000000000000000000000000000000000000"), iv)))

        assertEquals("6199566e82ffbf452df1b42dd3618e65",
            Hex.encode(Serpent.encryptCBC(Hex.toByteArray("E0208BE278E21420C4B1B9747788A954"), Hex.toByteArray("2BD6459F82C5B300952C49104881FF482BD6459F82C5B300"), iv)))

        assertEquals("7a2bc3d0b2ed19ecc6afe02f90e18e62",
            Hex.encode(Serpent.encryptCBC(Hex.toByteArray("00000000000000000000000000000000"), Hex.toByteArray("8000000000000000000000000000000000000000000000000000000000000000"), iv)))

        assertEquals("2353bd8468843bdf511065d9f9169692",
            Hex.encode(Serpent.encryptCBC(Hex.toByteArray("677C8DFAA08071743FD2B415D1B28AF2"), Hex.toByteArray("2BD6459F82C5B300952C49104881FF482BD6459F82C5B300952C49104881FF48"), iv)))

        val plain = "Schweißgequält zündet Typograf Jakob verflixt öde Pangramme an."
        val key128 = ByteArray(16, { i -> (i + 1).toByte() })
        val key192 = ByteArray(24, { i -> (i + 1).toByte() })
        val key256 = ByteArray(32, { i -> (i + 1).toByte() })

        assertEquals("a084d0281bc8ac4b3e9d027327afef79ded7d0dbde6dee3a2399e17b920b2f34386109c9cedbb29b8f06393202003579e0b5548813457c2e289bcc75bd14b1a2b02c07e61f4f9e05d46f77a4e103af90",
            Hex.encode(Serpent.encryptCBC(toByteArray(plain), key128, iv, Padding.SCHEME.PKCS7)))

        assertEquals("3e893cb66b78d37095e5e6af63e6b9a2c3f15e2ad16de15ea469f8bc144f69bfd9105b6e2b4b8b785a4e3836e5b8d8e430643a71fb89996c2cf1308c14088c3cb1aebb6eb67c42aff1255a2625ffb51e",
            Hex.encode(Serpent.encryptCBC(toByteArray(plain), key192, iv, Padding.SCHEME.PKCS7)))

        assertEquals("8293653ab23bf075ed29111a482096addddc78ad93af385df0fd343f631739cdb3f8cf46cf6cbc99997ac4f44ac9b2db16f72d1221db959283f1ab761d627cd4780134afe17801e902c6ceef27cdd282",
            Hex.encode(Serpent.encryptCBC(toByteArray(plain), key256, iv, Padding.SCHEME.PKCS7)))
    }

    @Test
    fun decryptECB() {

        var ciphertext = Serpent.encryptECB(Hex.toByteArray("00000000000000000000000000000000"), Hex.toByteArray("80000000000000000000000000000000"))
        assertEquals("00000000000000000000000000000000", Hex.encode(Serpent.decryptECB(ciphertext, Hex.toByteArray("80000000000000000000000000000000"))))

        ciphertext = Serpent.encryptECB(Hex.toByteArray("00112233445566778899AABBCCDDEEFF"), Hex.toByteArray("000102030405060708090A0B0C0D0E0F"))
        assertEquals("00112233445566778899aabbccddeeff", Hex.encode(Serpent.decryptECB(ciphertext, Hex.toByteArray("000102030405060708090A0B0C0D0E0F"))))

        ciphertext = Serpent.encryptECB(Hex.toByteArray("00000000000000000000000000000000"), Hex.toByteArray("800000000000000000000000000000000000000000000000"))
        assertEquals("00000000000000000000000000000000", Hex.encode(Serpent.decryptECB(ciphertext, Hex.toByteArray("800000000000000000000000000000000000000000000000"))))

        ciphertext = Serpent.encryptECB(Hex.toByteArray("E0208BE278E21420C4B1B9747788A954"), Hex.toByteArray("2BD6459F82C5B300952C49104881FF482BD6459F82C5B300"))
        assertEquals("e0208be278e21420c4b1b9747788a954", Hex.encode(Serpent.decryptECB(ciphertext, Hex.toByteArray("2BD6459F82C5B300952C49104881FF482BD6459F82C5B300"))))

        ciphertext = Serpent.encryptECB(Hex.toByteArray("00000000000000000000000000000000"), Hex.toByteArray("8000000000000000000000000000000000000000000000000000000000000000"))
        assertEquals("00000000000000000000000000000000", Hex.encode(Serpent.decryptECB(ciphertext, Hex.toByteArray("8000000000000000000000000000000000000000000000000000000000000000"))))

        ciphertext = Serpent.encryptECB(Hex.toByteArray("677C8DFAA08071743FD2B415D1B28AF2"), Hex.toByteArray("2BD6459F82C5B300952C49104881FF482BD6459F82C5B300952C49104881FF48"))
        assertEquals("677c8dfaa08071743fd2b415d1b28af2", Hex.encode(Serpent.decryptECB(ciphertext, Hex.toByteArray("2BD6459F82C5B300952C49104881FF482BD6459F82C5B300952C49104881FF48"))))

        val plain = "Schweißgequält zündet Typograf Jakob verflixt öde Pangramme an."
        val key128 = ByteArray(16, { i -> (i + 1).toByte() })
        val key192 = ByteArray(24, { i -> (i + 1).toByte() })
        val key256 = ByteArray(32, { i -> (i + 1).toByte() })

        ciphertext = Serpent.encryptECB(toByteArray(plain), key128, Padding.SCHEME.PKCS7)
        assertEquals(plain, fromByteArray(Serpent.decryptECB(ciphertext, key128, Padding.SCHEME.PKCS7)))

        ciphertext = Serpent.encryptECB(toByteArray(plain), key192, Padding.SCHEME.PKCS7)
        assertEquals(plain, fromByteArray(Serpent.decryptECB(ciphertext, key192, Padding.SCHEME.PKCS7)))

        ciphertext = Serpent.encryptECB(toByteArray(plain), key256, Padding.SCHEME.PKCS7)
        assertEquals(plain, fromByteArray(Serpent.decryptECB(ciphertext, key256, Padding.SCHEME.PKCS7)))
    }

    @Test
    fun decryptCBC() {

        val iv = ByteArray(16, { i -> (i + 1).toByte() })

        assertEquals("00000000000000000000000000000000",
            Hex.encode(Serpent.decryptCBC(Hex.toByteArray("0509721d767068a2be6537c55f2b1093"), Hex.toByteArray("80000000000000000000000000000000"), iv)))

        assertEquals("00112233445566778899aabbccddeeff",
            Hex.encode(Serpent.decryptCBC(Hex.toByteArray("51638e30889f25459322a11d46f0d83f"), Hex.toByteArray("000102030405060708090A0B0C0D0E0F"), iv)))

        assertEquals("00000000000000000000000000000000",
            Hex.encode(Serpent.decryptCBC(Hex.toByteArray("ce52c1b09f899b8f60e13ec7d23cb855"), Hex.toByteArray("800000000000000000000000000000000000000000000000"), iv)))

        assertEquals("e0208be278e21420c4b1b9747788a954",
            Hex.encode(Serpent.decryptCBC(Hex.toByteArray("6199566e82ffbf452df1b42dd3618e65"), Hex.toByteArray("2BD6459F82C5B300952C49104881FF482BD6459F82C5B300"), iv)))

        assertEquals("00000000000000000000000000000000",
            Hex.encode(Serpent.decryptCBC(Hex.toByteArray("7a2bc3d0b2ed19ecc6afe02f90e18e62"), Hex.toByteArray("8000000000000000000000000000000000000000000000000000000000000000"), iv)))

        assertEquals("677c8dfaa08071743fd2b415d1b28af2",
            Hex.encode(Serpent.decryptCBC(Hex.toByteArray("2353bd8468843bdf511065d9f9169692"), Hex.toByteArray("2BD6459F82C5B300952C49104881FF482BD6459F82C5B300952C49104881FF48"), iv)))

        val plain = "Schweißgequält zündet Typograf Jakob verflixt öde Pangramme an."
        var key128 = ByteArray(16, { i -> (i + 1).toByte() })
        var key192 = ByteArray(24, { i -> (i + 1).toByte() })
        var key256 = ByteArray(32, { i -> (i + 1).toByte() })

        assertEquals(plain, fromByteArray(Serpent.decryptCBC(
            Hex.toByteArray("a084d0281bc8ac4b3e9d027327afef79ded7d0dbde6dee3a2399e17b920b2f34386109c9cedbb29b8f06393202003579e0b5548813457c2e289bcc75bd14b1a2b02c07e61f4f9e05d46f77a4e103af90"),
            key128, iv, Padding.SCHEME.PKCS7)))

        assertEquals(plain, fromByteArray(Serpent.decryptCBC(
            Hex.toByteArray("3e893cb66b78d37095e5e6af63e6b9a2c3f15e2ad16de15ea469f8bc144f69bfd9105b6e2b4b8b785a4e3836e5b8d8e430643a71fb89996c2cf1308c14088c3cb1aebb6eb67c42aff1255a2625ffb51e"),
            key192, iv, Padding.SCHEME.PKCS7)))

        assertEquals(plain, fromByteArray(Serpent.decryptCBC(
            Hex.toByteArray("8293653ab23bf075ed29111a482096addddc78ad93af385df0fd343f631739cdb3f8cf46cf6cbc99997ac4f44ac9b2db16f72d1221db959283f1ab761d627cd4780134afe17801e902c6ceef27cdd282"),
            key256, iv, Padding.SCHEME.PKCS7)))

        key128 = Keys.randomKey(16)
        key192 = Keys.randomKey(24)
        key256 = Keys.randomKey(32)

        var ciphertext = Serpent.encryptECB(toByteArray(plain), key128, Padding.SCHEME.PKCS7)
        assertEquals(plain, fromByteArray(Serpent.decryptECB(ciphertext, key128, Padding.SCHEME.PKCS7)))

        ciphertext = Serpent.encryptECB(toByteArray(plain), key192, Padding.SCHEME.PKCS7)
        assertEquals(plain, fromByteArray(Serpent.decryptECB(ciphertext, key192, Padding.SCHEME.PKCS7)))

        ciphertext = Serpent.encryptECB(toByteArray(plain), key256, Padding.SCHEME.PKCS7)
        assertEquals(plain, fromByteArray(Serpent.decryptECB(ciphertext, key256, Padding.SCHEME.PKCS7)))
    }

    @Test
    fun invalidKeySize() {
        var ex: IllegalArgumentException? = null
        try {
            Serpent.encryptECB(toByteArray("123"), toByteArray("456"))
        } catch (e: IllegalArgumentException) {
            ex = e
        }
        assertNotNull(ex)
    }
}