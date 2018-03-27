package com.github.mitallast.ghost.crypto.dec

import kotlin.test.Test
import kotlin.test.assertEquals

class DecimalTest {

    @Test
    fun toBinary() {
        assertEquals("0011010100100100100010010110101010111111", Decimal.toBinary(228246252223))
    }

    @Test
    fun toHex() {
        assertEquals("3524896abf", Decimal.toHex(228246252223))
    }
}