package com.github.mitallast.ghost.crypto.utils

object Integers {
    private val hex = "0123456789abcdef"

    fun toHexString(value: Int): String {
        val sb = StringBuilder(8)
        var i = 28
        while (i >= 4) {
            val b = ((value shr i) and 0x0000000F)
            if (b == 0) {
                i -= 4
            } else {
                break
            }
        }
        while (i >= 0) {
            val b = ((value shr i) and 0x0000000F)
            sb.append(hex[b])
            i -= 4
        }
        return sb.toString()
    }

    fun toBinary(value: Int): String {
        val sb = StringBuilder(32)
        fun appendByte(b: Int) {
            sb.append(if (b and 0x8 > 0) '1' else '0')
            sb.append(if (b and 0x4 > 0) '1' else '0')
            sb.append(if (b and 0x2 > 0) '1' else '0')
            sb.append(if (b and 0x1 > 0) '1' else '0')
        }

        var i = 28
        while (i >= 4) {
            val b = ((value shr i) and 0x0000000F)
            if (b == 0) {
                i -= 4
            } else {
                break
            }
        }
        while (i >= 0) {
            val b = ((value shr i) and 0x0000000F)
            appendByte(b)
            i -= 4
        }

        return sb.toString()
    }

    fun bitCount(i: Int): Int {
        var i = i
        // HD, Figure 5-2
        i = i - (i.ushr(1) and 0x55555555)
        i = (i and 0x33333333) + (i.ushr(2) and 0x33333333)
        i = i + i.ushr(4) and 0x0f0f0f0f
        i = i + i.ushr(8)
        i = i + i.ushr(16)
        return i and 0x3f
    }

    fun rotateLeft(i: Int, distance: Int): Int {
        return i shl distance or i.ushr(-distance)
    }

    fun numberOfLeadingZeros(i: Int): Int {
        var i = i
        // HD, Figure 5-6
        if (i == 0)
            return 32
        var n = 1
        if (i.ushr(16) == 0) {
            n += 16
            i = i shl 16
        }
        if (i.ushr(24) == 0) {
            n += 8
            i = i shl 8
        }
        if (i.ushr(28) == 0) {
            n += 4
            i = i shl 4
        }
        if (i.ushr(30) == 0) {
            n += 2
            i = i shl 2
        }
        n -= i.ushr(31)
        return n
    }

    fun numberOfTrailingZeros(i: Int): Int {
        var i = i
        // HD, Figure 5-14
        var y: Int
        if (i == 0) return 32
        var n = 31
        y = i shl 16
        if (y != 0) {
            n = n - 16
            i = y
        }
        y = i shl 8
        if (y != 0) {
            n = n - 8
            i = y
        }
        y = i shl 4
        if (y != 0) {
            n = n - 4
            i = y
        }
        y = i shl 2
        if (y != 0) {
            n = n - 2
            i = y
        }
        return n - (i shl 1).ushr(31)
    }

    fun parseInt(value: String, radix: Int): Int {
        return value.toInt(radix)
    }
}