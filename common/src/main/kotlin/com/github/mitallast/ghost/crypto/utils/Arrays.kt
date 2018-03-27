package com.github.mitallast.ghost.crypto.utils

object Arrays {
    fun arraycopy(src: LongArray, srcPos: Int,
                  dest: LongArray, destPos: Int,
                  length: Int) {

        if(src === dest) {
            arraycopy(src.copyOf(), srcPos, dest, destPos, length)
            return
        }

        var i = 0
        while (i < length) {
            dest[i + destPos] = src[i + srcPos]
            i++
        }
    }

    fun arraycopy(src: IntArray, srcPos: Int,
                  dest: IntArray, destPos: Int,
                  length: Int) {
        if(src === dest) {
            arraycopy(src.copyOf(), srcPos, dest, destPos, length)
            return
        }

        var i = 0
        while (i < length) {
            dest[i + destPos] = src[i + srcPos]
            i++
        }
    }

    fun arraycopy(src: ByteArray, srcPos: Int,
                  dest: ByteArray, destPos: Int,
                  length: Int) {

        if(src === dest) {
            arraycopy(src.copyOf(), srcPos, dest, destPos, length)
            return
        }

        var i = 0
        while (i < length) {
            dest[i + destPos] = src[i + srcPos]
            i++
        }
    }

    fun equals(a: ByteArray, b: ByteArray): Boolean {
        return a.contentEquals(b)
    }

    fun fill(a: LongArray, `val`: Long) {
        var i = 0
        val len = a.size
        while (i < len) {
            a[i] = `val`
            i++
        }
    }

    fun fill(a: IntArray, `val`: Int) {
        var i = 0
        val len = a.size
        while (i < len) {
            a[i] = `val`
            i++
        }
    }

    fun fill(a: LongArray, fromIndex: Int, toIndex: Int, `val`: Long) {
        for (i in fromIndex until toIndex)
            a[i] = `val`
    }

    fun fill(a: IntArray, fromIndex: Int, toIndex: Int, `val`: Int) {
        for (i in fromIndex until toIndex)
            a[i] = `val`
    }
}