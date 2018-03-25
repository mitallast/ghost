package com.github.mitallast.ghost.codec

interface DataInput {
    fun readBoolean(): Boolean
    fun readShort(): Int
    fun readInt(): Int
    fun readLong(): Long
    fun readBytes(data: ByteArray)
    fun readChar(): Char
    fun readUTF(): String
}

interface DataOutput {
    fun writeBoolean(value: Boolean)
    fun writeShort(value: Int)
    fun writeInt(value: Int)
    fun writeLong(value: Long)
    fun writeBytes(value: ByteArray)
    fun writeChar(value: Char)
    fun writeUTF(value: String)
}

class ByteDataInput(bytes: ByteArray) : DataInput {
    private val it = bytes.iterator()

    override fun readBoolean(): Boolean {
        return it.nextByte() != 0.toByte()
    }

    override fun readShort(): Int {
        val a = it.nextByte().toInt() and 0xFF shl 8
        val b = it.nextByte().toInt() and 0xFF
        return a.or(b)
    }

    override fun readInt(): Int {
        val a = it.nextByte().toInt() and 0xFF shl 24
        val b = it.nextByte().toInt() and 0xFF shl 16
        val c = it.nextByte().toInt() and 0xFF shl 8
        val d = it.nextByte().toInt() and 0xFF
        return a.or(b).or(c).or(d)
    }

    override fun readLong(): Long {
        val a = it.nextByte().toLong() and 0xFF shl 56
        val b = it.nextByte().toLong() and 0xFF shl 48
        val c = it.nextByte().toLong() and 0xFF shl 40
        val d = it.nextByte().toLong() and 0xFF shl 32
        val e = it.nextByte().toLong() and 0xFF shl 24
        val f = it.nextByte().toLong() and 0xFF shl 16
        val g = it.nextByte().toLong() and 0xFF shl 8
        val h = it.nextByte().toLong() and 0xFF
        return a.or(b).or(c).or(d).or(e).or(f).or(g).or(h)
    }

    override fun readBytes(data: ByteArray) {
        (0..data.size).forEach { i -> data[i] = it.nextByte() }
    }

    override fun readChar(): Char {
        val c = it.nextByte().toInt() and 0xFF
        return when (c shr 4) {
            in 0..7 ->
                /* 0xxxxxxx*/
                c.toChar()
            in 12..13 -> {
                /* 110x xxxx   10xx xxxx*/
                val cc = it.nextByte().toInt()
                require((cc and 0xC0) == 0x80) { "malformed input" }
                ((c and 0x1F shl 6) or (cc and 0x3F)).toChar()
            }
            14 -> {
                /* 1110 xxxx  10xx xxxx  10xx xxxx */
                val c2 = it.nextByte().toInt()
                val c3 = it.nextByte().toInt()
                require(!(c2 and 0xC0 != 0x80 || c3 and 0xC0 != 0x80)) { "malformed input" }
                (c and 0x0F shl 12 or
                    (c2 and 0x3F shl 6) or
                    (c3 and 0x3F shl 0)).toChar();
            }
            else ->
                /* 10xx xxxx,  1111 xxxx */
                throw IllegalArgumentException("illegal string")
        }
    }

    override fun readUTF(): String {
        val size = readInt()
        if (size == 0) {
            return ""
        }
        val builder = StringBuilder(size)
        (0..(size-1)).forEach {
            builder.append(readChar())
        }
        return builder.toString()
    }
}

class ByteDataOutput : DataOutput {
    private var out = ByteArray(256)
    private var offset = 0

    private fun allocate(size: Int) {
        if(offset + size > out.size) {
            out = out.copyOf(out.size + size + 256)
        }
    }

    fun toByteArray(): ByteArray {
        return out.copyOf(offset)
    }

    override fun writeBoolean(value: Boolean) {
        allocate(1)
        out[offset] = if(value) 1 else 0
        offset+=1
    }

    override fun writeShort(value: Int) {
        allocate(2)
        out[offset+0] = (value shr 8 and 0xFF).toByte()
        out[offset+1] = (value and 0xFF).toByte()
        offset+=2
    }

    override fun writeInt(value: Int) {
        allocate(4)
        out[offset+0] = (value shr 24 and 0xFF).toByte()
        out[offset+1] = (value shr 16 and 0xFF).toByte()
        out[offset+2] = (value shr 8 and 0xFF).toByte()
        out[offset+3] = (value and 0xFF).toByte()
        offset+=4
    }

    override fun writeLong(value: Long) {
        allocate(8)
        out[offset+0] = (value shr 46 and 0xFF).toByte()
        out[offset+1] = (value shr 48 and 0xFF).toByte()
        out[offset+2] = (value shr 40 and 0xFF).toByte()
        out[offset+3] = (value shr 32 and 0xFF).toByte()
        out[offset+4] = (value shr 24 and 0xFF).toByte()
        out[offset+5] = (value shr 16 and 0xFF).toByte()
        out[offset+6] = (value shr 8 and 0xFF).toByte()
        out[offset+7] = (value and 0xFF).toByte()
        offset+=8
    }

    override fun writeBytes(value: ByteArray) {
        allocate(value.size)
        (0..value.size).forEach { i ->
            out[offset + i] = value[i]
        }
        offset+=value.size
    }

    override fun writeChar(value: Char) {
        val c = value.toInt()
        when {
            c in 0x0001..0x007F -> {
                allocate(1)
                out[offset] = c.toByte()
                offset+=1
            }
            c > 0x07FF -> {
                allocate(3)
                out[offset+0] = (0xE0 or (c shr 12 and 0x0F)).toByte()
                out[offset+1] = (0x80 or (c shr 6 and 0x3F)).toByte()
                out[offset+2] = (0x80 or (c shr 0 and 0x3F)).toByte()
                offset+=3
            }
            else -> {
                allocate(2)
                out[offset++] = (0xC0 or (c shr 6 and 0x1F)).toByte()
                out[offset++] = (0x80 or (c shr 0 and 0x3F)).toByte()
                offset+=2
            }
        }
    }

    override fun writeUTF(value: String) {
        allocate(value.length * 2)
        writeInt(value.length)
        value.forEach { writeChar(it) }
    }
}