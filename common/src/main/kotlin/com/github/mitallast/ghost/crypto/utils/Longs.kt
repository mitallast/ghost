package com.github.mitallast.ghost.crypto.utils

object Longs {
    private val hex = "0123456789abcdef"

    fun toHex(value: Long): String {
        val sb = StringBuilder(16)
        var i = 60
        var b: Int
        while (i >= 4) {
            b = ((value shr i) and 0x0000000F).toInt()
            if (b == 0) {
                i -= 4
            } else {
                break
            }
        }
        while (i >= 0) {
            b = ((value shr i) and 0x0000000F).toInt()
            sb.append(hex[b])
            i -= 4
        }
        return sb.toString()
    }

    fun toBinary(value: Long): String {
        val sb = StringBuilder(64)

        fun appendByte(b: Long) {
            sb.append(if (b and 0x8 > 0) '1' else '0')
            sb.append(if (b and 0x4 > 0) '1' else '0')
            sb.append(if (b and 0x2 > 0) '1' else '0')
            sb.append(if (b and 0x1 > 0) '1' else '0')
        }

        var i = 60
        var b: Long
        while (i >= 4) {
            b = (value shr i) and 0x0000000F
            if (b == 0L) {
                i -= 4
            } else {
                break
            }
        }
        while (i >= 0) {
            b = (value shr i) and 0x0000000F
            appendByte(b)
            i -= 4
        }

        return sb.toString()
    }

    fun toString(value: Long, radix: Int): String {
        return when (radix) {
            2 -> toBinary(value)
            10 -> value.toString()
            16 -> toHex(value)
            else -> throw IllegalArgumentException()
        }
    }

    fun numberOfLeadingZeros(i: Long): Int {
        if (i == 0L)
            return 64
        var n = 1
        var x = i.ushr(32).toInt()
        if (x == 0) {
            n += 32
            x = i.toInt()
        }
        if (x.ushr(16) == 0) {
            n += 16
            x = x shl 16
        }
        if (x.ushr(24) == 0) {
            n += 8
            x = x shl 8
        }
        if (x.ushr(28) == 0) {
            n += 4
            x = x shl 4
        }
        if (x.ushr(30) == 0) {
            n += 2
            x = x shl 2
        }
        n -= x.ushr(31)
        return n
    }

    fun signum(i: Long): Int {
        // HD, Section 2-7
        return (i shr 63 or (-i).ushr(63)).toInt()
    }
}