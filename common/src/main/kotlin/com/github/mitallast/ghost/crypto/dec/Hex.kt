package com.github.mitallast.ghost.crypto.dec

import com.github.mitallast.ghost.crypto.utils.Integers
import com.github.mitallast.ghost.crypto.utils.Longs
import com.github.mitallast.ghost.crypto.utils.Strings

object Hex {

    fun toBinary(hex: String): String {
        var i = 0
        var erg = ""
        while (i < hex.length) {
            val str = hex.substring(i, i + 2)
            erg += Binary.paddingBinary(Longs.toBinary(str.toLong(16)), 8)
            i+=2
        }
        return erg
    }

    fun toDecimal(hex: String): String {
        var i = 0
        var erg = ""
        while (i < hex.length) {
            val str = hex.substring(i, i + 2)
            erg += str.toLong(16).toString()
            i+=2
        }
        return erg
    }

    fun toText(hex: String): String {
        var i = 0
        var erg = ""
        while (i < hex.length) {
            val str = hex.substring(i, i + 2)
            erg += str.toInt(16).toChar()
            i+=2
        }
        return erg
    }

    fun encode(chars: String): String {
        var erg = ""
        for (i in chars.indices) {
            if (chars[i].toInt() and 0xff < 0x10) erg += "0"
            erg += Integers.toHexString(chars[i].toInt())
        }
        return erg
    }

    fun encode(bytes: ByteArray): String {
        var buf = ""
        var i = 0
        while (i < bytes.size) {
            if (bytes[i].toInt() and 0xff < 0x10) buf += "0"
            buf += Longs.toHex((bytes[i].toInt() and 0xff).toLong())
            i++
        }
        return buf
    }

    fun toByteArray(s: String): ByteArray {
        val len = s.length
        val data = ByteArray(len / 2)
        var i = 0
        while (i < len) {
            data[i / 2] = ((Strings.digit(s[i]) shl 4) + Strings.digit(s[i + 1])).toByte()
            i += 2
        }
        return data
    }
}