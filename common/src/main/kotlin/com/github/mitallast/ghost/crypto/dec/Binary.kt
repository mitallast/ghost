package com.github.mitallast.ghost.crypto.dec

import com.github.mitallast.ghost.crypto.utils.Integers
import com.github.mitallast.ghost.crypto.utils.Strings

object Binary {

    fun toDecimal(binary: String): String {
        var i = 0
        var erg = ""
        while (i < binary.length) {
            val str = binary.substring(i, i + 8)
            erg += str.toInt(2)
            i+=8
        }
        return erg
    }

    fun toHex(binary: String): String {
        var i = 0
        var erg = ""
        while (i < binary.length) {
            val str = binary.substring(i, i + 8)
            erg += Integers.toHexString(str.toInt(2))
            i+=8
        }
        return erg
    }

    fun toText(binary: String): String {
        var i = 0
        var erg = ""
        while (i < binary.length) {
            val str = binary.substring(i, i + 8)
            erg += str.toInt(2).toChar()
            i+=8
        }
        return erg
    }

    fun encode(str: String): String = Text.toBinary(str)

    fun encode(bytes: ByteArray): String = encode(Strings.fromByteArray(bytes))

    internal fun paddingBinary(bin: String, length: Int): String {
        var erg = bin
        while (erg.length < length) erg = "0" + erg
        return erg
    }
}