package com.github.mitallast.ghost.crypto.dec

import com.github.mitallast.ghost.crypto.utils.Integers

object Text {

    fun toBinary(text: String): String {
        var erg = ""
        for (i in text.indices)
            erg += Binary.paddingBinary(Integers.toBinary(text[i].toInt()), 8)
        return erg
    }

    fun toDecimal(text: String): String {
        var erg = ""
        for (i in text.indices)
            erg += text[i].toInt()
        return erg
    }

    fun toHex(text: String): String {
        return Hex.encode(text)
    }
}