package com.github.mitallast.ghost.crypto.utils

import kotlin.test.Test
import kotlin.test.assertEquals

class LongsTest {

    @Test
    fun toHex() {
        assertEquals("f", Longs.toHex(0xf))
        assertEquals("ff", Longs.toHex(0xff))
        assertEquals("fff", Longs.toHex(0xfff))
        assertEquals("ffff", Longs.toHex(0xffff))
        assertEquals("fffff", Longs.toHex(0xfffff))
        assertEquals("ffffff", Longs.toHex(0xffffff))
        assertEquals("fffffff", Longs.toHex(0xfffffff))
        assertEquals("ffffffff", Longs.toHex(0xffffffff))
        assertEquals("fffffffff", Longs.toHex(0xfffffffff))
        assertEquals("ffffffffff", Longs.toHex(0xffffffffff))
        assertEquals("fffffffffff", Longs.toHex(0xfffffffffff))
    }
}