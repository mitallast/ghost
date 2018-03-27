package com.github.mitallast.ghost.crypto.dec

import com.github.mitallast.ghost.crypto.utils.Longs

object Decimal {

    fun toBinary(decimal: Long): String {
        var erg = Longs.toBinary(decimal)
        if(erg.length % 2 != 0) erg = "0" + erg
        return erg
    }

    fun toHex(decimal: Long): String {
        return Longs.toHex(decimal)
    }

}