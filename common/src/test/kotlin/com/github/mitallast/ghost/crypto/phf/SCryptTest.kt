package com.github.mitallast.ghost.crypto.phf

import com.github.mitallast.ghost.crypto.dec.Base64
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SCryptTest {

    @Test
    fun hash() {

        val salt = byteArrayOf(1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16)

        assertEquals("PVf48BxQCrBUTHTnwC9X27CY1WgQPfi9uCFeKzQWKao=",
                Base64.encode(SCrypt.hash("A quick movement of the enemy will jeopardize six gunboats.", salt, 16384, 8, 1)))

        assertEquals("3c5rIdu4DWfDQLa704xc4cbKEQujKIg2hj2RHhiDQow=",
                Base64.encode(SCrypt.hash("Schweißgequält zündet Typograf Jakob verflixt öde Pangramme an.", salt, 16384, 8, 1)))
    }

    @Test
    fun verify() {
        val salt = SCrypt.salt(64)

        var hashed = SCrypt.hash("A quick movement of the enemy will jeopardize six gunboats.", salt, 16384, 8, 1)
        assertTrue(SCrypt.verify("A quick movement of the enemy will jeopardize six gunboats.", hashed, salt, 16384, 8, 1))

        hashed = SCrypt.hash("Schweißgequält zündet Typograf Jakob verflixt öde Pangramme an.", salt, 16384, 8, 1)
        assertTrue(SCrypt.verify("Schweißgequält zündet Typograf Jakob verflixt öde Pangramme an.", hashed, salt, 16384, 8, 1))
    }

    @Test
    fun salt() {
        assertEquals(32, SCrypt.salt(32).size)
        assertEquals(64, SCrypt.salt(64).size)
    }
}