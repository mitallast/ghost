package com.github.mitallast.ghost.client.crypto

import kotlinx.io.ByteArrayOutputStream
import org.khronos.webgl.Uint8Array
import org.khronos.webgl.get

object BERTags {
    const val BOOLEAN = 0x01
    const val INTEGER = 0x02
    const val BIT_STRING = 0x03
    const val OCTET_STRING = 0x04
    const val NULL = 0x05
    const val OBJECT_IDENTIFIER = 0x06
    const val EXTERNAL = 0x08
    const val ENUMERATED = 0x0a // decimal 10
    const val SEQUENCE = 0x10 // decimal 16
    const val SEQUENCE_OF = 0x10 // for completeness - used to model a SEQUENCE of the same type.
    const val SET = 0x11 // decimal 17
    const val SET_OF = 0x11 // for completeness - used to model a SET of the same type.


    const val NUMERIC_STRING = 0x12 // decimal 18
    const val PRINTABLE_STRING = 0x13 // decimal 19
    const val T61_STRING = 0x14 // decimal 20
    const val VIDEOTEX_STRING = 0x15 // decimal 21
    const val IA5_STRING = 0x16 // decimal 22
    const val UTC_TIME = 0x17 // decimal 23
    const val GENERALIZED_TIME = 0x18 // decimal 24
    const val GRAPHIC_STRING = 0x19 // decimal 25
    const val VISIBLE_STRING = 0x1a // decimal 26
    const val GENERAL_STRING = 0x1b // decimal 27
    const val UNIVERSAL_STRING = 0x1c // decimal 28
    const val BMP_STRING = 0x1e // decimal 30
    const val UTF8_STRING = 0x0c // decimal 12

    const val CONSTRUCTED = 0x20 // decimal 32
    const val APPLICATION = 0x40 // decimal 64
    const val TAGGED = 0x80 // decimal 128
}

object ASN1 {
    class InputStream(val s: Uint8Array) {
        private var offset = 0

        private fun read(): Int {
            // console.log("read $offset = ${s[offset]}")
            if (offset >= s.length) {
                return -1
            } else {
                return s[offset++].toInt()
            }
        }

        private fun slice(length: Int): InputStream {
            val slice = s.subarray(offset, offset + length)
            // console.log("slice $offset $length", HEX.toHex(slice))
            offset += length
            return InputStream(slice)
        }

        private fun array(): Uint8Array {
            val slice = s.subarray(offset, s.length)
            // console.log("array $offset", HEX.toHex(slice))
            offset = s.length
            return slice
        }

        fun readObject(): ASN1Primitive? {
            val tag = read()
            // console.log("read object tag=$tag")
            if (tag <= 0) {
                if (tag == 0) {
                    throw RuntimeException("unexpected end-of-contents marker")
                }
                return null
            }
            val tagNo = readTagNumber(tag)
            val isConstructed = tag and BERTags.CONSTRUCTED != 0

            val length = readLength()
            if (length < 0) {
                if (!isConstructed) {
                    throw RuntimeException("indefinite-length primitive encoding encountered")
                }
                throw RuntimeException("not implemented")
            } else {
                return buildObject(tag, tagNo, length)
            }
        }

        private fun buildObject(tag: Int, tagNo: Int, length: Int): ASN1Primitive {
            // console.log("build object tag=$tag tagNo=$tagNo length=$length")
            val isConstructed = tag and BERTags.CONSTRUCTED != 0

            val defIn = slice(length)

            if ((tag and BERTags.APPLICATION) != 0) {
                throw RuntimeException("not implemented")
            }
            if ((tag and BERTags.TAGGED) != 0) {
                throw RuntimeException("not implemented")
            }
            if (isConstructed) {
                when (tagNo) {
                    BERTags.SEQUENCE -> {
                        // console.log("build sequence")
                        val vector = ArrayList<ASN1Primitive>()
                        do {
                            val o = defIn.readObject()
                            if (o != null) {
                                vector.add(o)
                            }
                        } while (o != null)
                        // console.log("SEQUENCE", vector)
                        return ASN1Sequence(vector)
                    }
                    else -> throw RuntimeException("not implemented")
                }
            }

            return defIn.createPrimitiveDERObject(tagNo)
        }

        private fun createPrimitiveDERObject(tagNo: Int): ASN1Primitive {
            return when (tagNo) {
                BERTags.INTEGER -> {
                    ASN1Integer(array())
                }
                BERTags.OBJECT_IDENTIFIER -> {
                    val data = array()
                    // console.log("OBJECT_IDENTIFIER", HEX.toHex(data))
                    ASN1ObjectIdentifier(data)
                }
                BERTags.OCTET_STRING -> {
                    DEROctetString(array())
                }
                BERTags.BIT_STRING -> {
                    val padBits = read()
                    val data = array()
                    if (data.length != 0) {
                        if (padBits > 0 && padBits < 8) {
                            if (data[data.length - 1].toInt() != (data[data.length - 1].toInt() and (0xff shl padBits))) {
                                return DLBitString(data, padBits)
                            }
                        }
                    }
                    DERBitString(data, padBits)
                }
                else -> throw RuntimeException("unexpected tag no $tagNo")
            }
        }

        private fun readTagNumber(tag: Int): Int {
            var tagNo = tag and 0x1F
            if (tagNo == 0x1F) {
                tagNo = 0
                var b = read()
                if ((b and 0x7F) == 0) {
                    throw RuntimeException("corrupted stream - invalid high tag number found")
                }

                while ((b >= 0) && ((b and 0x80) != 0)) {
                    tagNo = tagNo or (b and 0x7F)
                    tagNo = tagNo shl 7
                    b = read()
                }

                if (b < 0) {
                    throw RuntimeException("EOF found inside tag value.")
                }
                tagNo = tagNo or (b and 0x7f)
            }
            return tagNo
        }

        fun readLength(): Int {
            var length = read()
            if (length < 0) {
                throw RuntimeException("EOF found when length expected")
            }
            if (length == 0x80) {
                return -1
            }
            if (length > 127) {
                val size = length and 0x7f
                // Note: The invalid long form "0xff" (see X.690 8.1.3.5c) will be caught here
                if (size > 4) {
                    throw RuntimeException("DER length more than 4 bytes: " + size)
                }
                length = 0
                for (i in 0 until size) {
                    val next = read()
                    if (next < 0) {
                        throw RuntimeException("EOF found reading length")
                    }
                    length = (length shl 8) + next
                    if (length < 0) {
                        throw RuntimeException("corrupted stream - negative length found")
                    }
                }
            }
            return length
        }
    }

    class OutputStream {
        private val out = ByteArrayOutputStream()

        fun write(byte: Int) {
            out.write(byte)
        }

        fun write(bytes: Uint8Array) {
            for (i in 0 until bytes.length) {
                out.write(bytes[i].toInt())
            }
        }

        fun writeLength(length: Int) {
            if (length > 127) {
                var size = 1
                var value = length

                do {
                    value = value ushr 8
                    if (value != 0) {
                        size++
                    }

                } while (value != 0)

                write((size or 0x80).toByte().toInt())

                var i = (size - 1) * 8
                while (i >= 0) {
                    write((length shr i).toByte().toInt())
                    i -= 8
                }
            } else {
                write(length.toByte().toInt())
            }
        }

        fun toByteArray(): ByteArray {
            return out.toByteArray()
        }
    }

    interface ASN1Primitive {
        fun encode(stream: OutputStream)
        fun encodedLength(): Int

        fun calculateBodyLength(length: Int): Int {
            var count = 1

            if (length > 127) {
                var size = 1
                var value = length

                do {
                    value = value ushr 8
                    if (value != 0) {
                        size++
                    }
                } while (value != 0)

                var i = (size - 1) * 8
                while (i >= 0) {
                    count++
                    i -= 8
                }
            }

            return count
        }

        fun calculateTagLength(tagNo: Int): Int {
            var tagNo = tagNo
            var length = 1

            if (tagNo >= 31) {
                if (tagNo < 128) {
                    length++
                } else {
                    val stack = ByteArray(5)
                    var pos = stack.size

                    stack[--pos] = (tagNo and 0x7F).toByte()

                    do {
                        tagNo = tagNo shr 7
                        stack[--pos] = (tagNo and 0x7F or 0x80).toByte()
                    } while (tagNo > 127)

                    length += stack.size - pos
                }
            }

            return length
        }
    }

    class ASN1Sequence(val vector: ArrayList<ASN1Primitive>) : ASN1Primitive {
        override fun encode(stream: OutputStream) {
            val len = bodyLength()
            stream.write(BERTags.SEQUENCE or BERTags.CONSTRUCTED)
            stream.writeLength(len)
            for (p in vector) {
                p.encode(stream)
            }
        }

        override fun encodedLength(): Int {
            val len = bodyLength()
            return 1 + len + calculateBodyLength(len)
        }

        private fun bodyLength(): Int {
            var length = 0
            for (p in vector) {
                length += p.encodedLength()
            }
            return length;
        }
    }

    class ASN1Integer(val value: Uint8Array) : ASN1Primitive {
        override fun encode(stream: OutputStream) {
            stream.write(BERTags.INTEGER)
            stream.writeLength(value.length)
            stream.write(value)
        }

        override fun encodedLength(): Int {
            return 1 + calculateBodyLength(value.length) + value.length
        }
    }

    class ASN1ObjectIdentifier(val value: Uint8Array) : ASN1Primitive {
        override fun encode(stream: OutputStream) {
            stream.write(BERTags.OBJECT_IDENTIFIER)
            stream.writeLength(value.length)
            stream.write(value)
        }

        override fun encodedLength(): Int {
            return 1 + calculateBodyLength(value.length) + value.length
        }
    }

    class DEROctetString(val value: Uint8Array) : ASN1Primitive {
        override fun encode(stream: OutputStream) {
            stream.write(BERTags.OCTET_STRING)
            stream.writeLength(value.length)
            stream.write(value)
        }

        override fun encodedLength(): Int {
            return 1 + calculateBodyLength(value.length) + value.length
        }
    }

    class DERBitString(val data: Uint8Array, val padBits: Int) : ASN1Primitive {
        override fun encode(stream: OutputStream) {
            stream.write(BERTags.BIT_STRING)
            stream.writeLength(data.length + 1)
            stream.write(padBits)
            stream.write(data)
        }

        override fun encodedLength(): Int {
            return 1 + calculateBodyLength(data.length + 1) + data.length + 1
        }
    }

    class DLBitString(val data: Uint8Array, val padBits: Int) : ASN1Primitive {

        override fun encode(stream: OutputStream) {
            stream.write(BERTags.BIT_STRING)
            stream.writeLength(data.length + 1)
            stream.write(padBits)
            stream.write(data)
        }
        override fun encodedLength(): Int {
            return 1 + calculateBodyLength(data.length + 1) + data.length + 1
        }

    }
}