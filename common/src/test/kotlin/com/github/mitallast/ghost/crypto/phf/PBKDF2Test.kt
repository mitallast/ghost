package com.github.mitallast.ghost.crypto.phf

import com.github.mitallast.ghost.crypto.dec.Hex
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PBKDF2Test {

    @Test
    fun dkLen() {
        val salt = ByteArray(20, { i -> (i + 1).toByte() })
        assertEquals("371337bebfe79667fe9596d5d3af97cb668de64f63", Hex.encode(PBKDF2.hash(PBKDF2.HMAC_SHA1, "password", salt, 10000, 21)))
    }

    @Test
    fun hash() {

        var salt = ByteArray(20, { i -> (i + 1).toByte() })
        assertEquals("371337bebfe79667fe9596d5d3af97cb668de64f", Hex.encode(PBKDF2.hash(PBKDF2.HMAC_SHA1, "password", salt, 10000, 20)))

        salt = ByteArray(32, { i -> (i + 1).toByte() })
        assertEquals("71be04772a30dc1f42d06a84547af6dfffe9a2c206641df8257de67a4d7b548b", Hex.encode(PBKDF2.hash(PBKDF2.HMAC_SHA256, "password", salt, 10000, 32)))

        salt = ByteArray(64, { i -> (i + 1).toByte() })
        assertEquals("ba70466626579fe212c21246783129cd0823939334bb26ec67ad264a1559ed5a0bff95fa7a3f9a23472d6acfff78684b69b69f220a83476628d7a990703d37bb",
            Hex.encode(PBKDF2.hash(PBKDF2.HMAC_SHA512, "password", salt, 10000, 64)))
    }

    @Test
    fun verify() {

        var salt = PBKDF2.salt(20)
        var hashed = PBKDF2.hash(PBKDF2.HMAC_SHA1, "password", salt, 10000, 20)
        assertTrue(PBKDF2.verify(PBKDF2.HMAC_SHA1, "password", hashed, salt, 10000, 20))

        salt = PBKDF2.salt(32)
        hashed = PBKDF2.hash(PBKDF2.HMAC_SHA256, "password", salt, 10000, 32)
        assertTrue(PBKDF2.verify(PBKDF2.HMAC_SHA256, "password", hashed, salt, 10000, 32))

        salt = PBKDF2.salt(64)
        hashed = PBKDF2.hash(PBKDF2.HMAC_SHA512, "password", salt, 10000, 64)
        assertTrue(PBKDF2.verify(PBKDF2.HMAC_SHA512, "password", hashed, salt, 10000, 64))

        salt = PBKDF2.salt(64)
        hashed = PBKDF2.hash(PBKDF2.HMAC_SHA512, "A quick movement of the enemy will jeopardize six gunboats.", salt, 10000, 64)
        assertTrue(PBKDF2.verify(PBKDF2.HMAC_SHA512, "A quick movement of the enemy will jeopardize six gunboats.", hashed, salt, 10000, 64))

        salt = PBKDF2.salt(64)
        hashed = PBKDF2.hash(PBKDF2.HMAC_SHA512, "Schweißgequält zündet Typograf Jakob verflixt öde Pangramme an.", salt, 10000, 64)
        assertTrue(PBKDF2.verify(PBKDF2.HMAC_SHA512, "Schweißgequält zündet Typograf Jakob verflixt öde Pangramme an.", hashed, salt, 10000, 64))
    }

    @Test
    fun salt() {
        assertEquals(32, PBKDF2.salt(32).size)
        assertEquals(64, PBKDF2.salt(64).size)
    }
}