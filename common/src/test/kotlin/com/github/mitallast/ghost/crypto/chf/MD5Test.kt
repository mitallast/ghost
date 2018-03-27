package com.github.mitallast.ghost.crypto.chf

import kotlin.test.Test
import kotlin.test.assertEquals

class MD5Test {

    @Test
    fun hash() {
        assertEquals("d41d8cd98f00b204e9800998ecf8427e", MD5.hash(""))
        assertEquals("0cc175b9c0f1b6a831c399e269772661", MD5.hash("a"))
        assertEquals("900150983cd24fb0d6963f7d28e17f72", MD5.hash("abc"))
        assertEquals("c3fcd3d76192e4007dfb496cca67e13b", MD5.hash("abcdefghijklmnopqrstuvwxyz"))
        assertEquals("f96b697d7cb7938d525a2f31aaf161d0", MD5.hash("message digest"))
        assertEquals("d174ab98d277d9f5a5611c2c9f419d9f", MD5.hash("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"))
        assertEquals("57edf4a22be3c955ac49da2e2107b67a", MD5.hash("12345678901234567890123456789012345678901234567890123456789012345678901234567890"))
        assertEquals("5f8cf08267cde2cdc4a331d191b26587", MD5.hash("A quick movement of the enemy will jeopardize six gunboats."))
        assertEquals("3c5184010296cdd5f20e354c5fdd5ee8", MD5.hash("Schweißgequält zündet Typograf Jakob verflixt öde Pangramme an."))
    }
}