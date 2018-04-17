package com.github.mitallast.ghost.common.codec

import kotlinx.io.ByteArrayInputStream
import kotlinx.io.ByteArrayOutputStream
import kotlinx.io.InputStream
import kotlinx.io.OutputStream

interface CodecMessage {
    fun messageId(): Int
}

internal object PrimitiveTag {
    val BOOLEAN = 0x01
    val BYTE = 0x02
    val SHORT = 0x03
    val INT = 0x04
    val LONG = 0x05
    val CHAR = 0x06
    val STRING = 0x07
    val BYTES = 0x08
    val LIST = 0x0A
    val SET = 0x0B
    val ENUM = 0x0C

    val STATIC = 0x1F
    val ANY = 0x2F

    val OBJECT = 0xEF
    val OBJECT_END = 0xFF
}

interface Codec<T> {
    val tag: Int

    fun read(stream: InputStream): T

    fun read(data: ByteArray): T {
        val input = ByteArrayInputStream(data)
        return read(input)
    }

    fun write(stream: OutputStream, value: T)

    fun write(value: T): ByteArray {
        val output = ByteArrayOutputStream()
        write(output, value)
        return output.toByteArray()
    }

    companion object {

        fun <T : CodecMessage> register(code: Int, codec: Codec<T>) {
            AnyCodec.register(code, codec)
        }

        fun booleanCodec(): Codec<Boolean> = BooleanCodec
        fun intCodec(): Codec<Int> = IntCodec
        fun longCodec(): Codec<Long> = LongCodec
        fun stringCodec(): Codec<String> = StringCodec
        fun bytesCodec(): Codec<ByteArray> = ByteArrayCodec

        fun <T : CodecMessage> anyCodec(): Codec<T> = AnyCodec()
        fun <T : Enum<T>> enumCodec(vararg enums: T): Codec<T> = EnumCodec(enums.asIterable())
        fun <T> listCodec(codec: Codec<T>): Codec<List<T>> = ListCodec(codec)
        fun <T> setCodec(codec: Codec<T>): Codec<Set<T>> = SetCodec(codec)

        fun <T> of(value: T): Codec<T> = StaticCodec(value)

        fun <T> field(id: Int, codec: Codec<T>): FieldCodec<T> =
            RequiredFieldCodec(id, codec)

        fun <T> optional(id: Int, codec: Codec<T>): FieldCodec<T?> =
            OptionalFieldCodec(id, codec)

        fun <Type, Param1> of(
            builder: Function1<Param1, Type>,
            lens1: Function1<Type, Param1>,
            codec1: FieldCodec<Param1>
        ): Codec<Type> {
            return Codec1(builder, lens1, codec1)
        }

        fun <Type, Param1, Param2> of(
            builder: Function2<Param1, Param2, Type>,
            lens1: Function1<Type, Param1>,
            lens2: Function1<Type, Param2>,
            codec1: FieldCodec<Param1>,
            codec2: FieldCodec<Param2>
        ): Codec<Type> {
            return Codec2(builder, lens1, lens2, codec1, codec2)
        }

        fun <Type, Param1, Param2, Param3> of(
            builder: Function3<Param1, Param2, Param3, Type>,
            lens1: Function1<Type, Param1>,
            lens2: Function1<Type, Param2>,
            lens3: Function1<Type, Param3>,
            codec1: FieldCodec<Param1>,
            codec2: FieldCodec<Param2>,
            codec3: FieldCodec<Param3>
        ): Codec<Type> {
            return Codec3(builder, lens1, lens2, lens3, codec1, codec2, codec3)
        }

        fun <Type, Param1, Param2, Param3, Param4> of(
            builder: Function4<Param1, Param2, Param3, Param4, Type>,
            lens1: Function1<Type, Param1>,
            lens2: Function1<Type, Param2>,
            lens3: Function1<Type, Param3>,
            lens4: Function1<Type, Param4>,
            codec1: FieldCodec<Param1>,
            codec2: FieldCodec<Param2>,
            codec3: FieldCodec<Param3>,
            codec4: FieldCodec<Param4>
        ): Codec<Type> {
            return Codec4(builder, lens1, lens2, lens3, lens4, codec1, codec2, codec3, codec4)
        }

        fun <Type, Param1, Param2, Param3, Param4, Param5> of(
            builder: Function5<Param1, Param2, Param3, Param4, Param5, Type>,
            lens1: Function1<Type, Param1>,
            lens2: Function1<Type, Param2>,
            lens3: Function1<Type, Param3>,
            lens4: Function1<Type, Param4>,
            lens5: Function1<Type, Param5>,
            codec1: FieldCodec<Param1>,
            codec2: FieldCodec<Param2>,
            codec3: FieldCodec<Param3>,
            codec4: FieldCodec<Param4>,
            codec5: FieldCodec<Param5>
        ): Codec<Type> {
            return Codec5(builder,
                lens1, lens2, lens3, lens4, lens5,
                codec1, codec2, codec3, codec4, codec5)
        }

        fun <Type, Param1, Param2, Param3, Param4, Param5, Param6> of(
            builder: Function6<Param1, Param2, Param3, Param4, Param5, Param6, Type>,
            lens1: Function1<Type, Param1>,
            lens2: Function1<Type, Param2>,
            lens3: Function1<Type, Param3>,
            lens4: Function1<Type, Param4>,
            lens5: Function1<Type, Param5>,
            lens6: Function1<Type, Param6>,
            codec1: FieldCodec<Param1>,
            codec2: FieldCodec<Param2>,
            codec3: FieldCodec<Param3>,
            codec4: FieldCodec<Param4>,
            codec5: FieldCodec<Param5>,
            codec6: FieldCodec<Param6>
        ): Codec<Type> {
            return Codec6(builder,
                lens1, lens2, lens3, lens4, lens5, lens6,
                codec1, codec2, codec3, codec4, codec5, codec6)
        }
    }
}

internal object BooleanCodec : Codec<Boolean> {
    override val tag: Int = PrimitiveTag.BOOLEAN

    override fun read(stream: InputStream): Boolean {
        return stream.read() > 0
    }

    override fun write(stream: OutputStream, value: Boolean) {
        stream.write(if (value) 1 else 0)
    }
}

internal object IntCodec : Codec<Int> {
    override val tag: Int = PrimitiveTag.INT

    override fun read(stream: InputStream): Int {
        val a = stream.read() and 0xFF shl 24
        val b = stream.read() and 0xFF shl 16
        val c = stream.read() and 0xFF shl 8
        val d = stream.read() and 0xFF
        return a.or(b).or(c).or(d)
    }

    override fun write(stream: OutputStream, value: Int) {
        stream.write(value shr 24 and 0xFF)
        stream.write(value shr 16 and 0xFF)
        stream.write(value shr 8 and 0xFF)
        stream.write(value and 0xFF)
    }
}

internal object LongCodec : Codec<Long> {
    override val tag: Int = PrimitiveTag.LONG

    override fun read(stream: InputStream): Long {
        val a = stream.read().toLong() and 0xFF shl 56
        val b = stream.read().toLong() and 0xFF shl 48
        val c = stream.read().toLong() and 0xFF shl 40
        val d = stream.read().toLong() and 0xFF shl 32
        val e = stream.read().toLong() and 0xFF shl 24
        val f = stream.read().toLong() and 0xFF shl 16
        val g = stream.read().toLong() and 0xFF shl 8
        val h = stream.read().toLong() and 0xFF
        return a.or(b).or(c).or(d).or(e).or(f).or(g).or(h)
    }

    override fun write(stream: OutputStream, value: Long) {
        stream.write((value shr 56 and 0xFF).toInt())
        stream.write((value shr 48 and 0xFF).toInt())
        stream.write((value shr 40 and 0xFF).toInt())
        stream.write((value shr 32 and 0xFF).toInt())
        stream.write((value shr 24 and 0xFF).toInt())
        stream.write((value shr 16 and 0xFF).toInt())
        stream.write((value shr 8 and 0xFF).toInt())
        stream.write((value and 0xFF).toInt())
    }
}

internal object CharCodec : Codec<Char> {
    override val tag: Int = PrimitiveTag.CHAR

    override fun read(stream: InputStream): Char {
        val c = stream.read() and 0xFF
        return when (c shr 4) {
            in 0..7 ->
                /* 0xxxxxxx*/
                c.toChar()
            in 12..13 -> {
                /* 110x xxxx   10xx xxxx*/
                val cc = stream.read()
                require((cc and 0xC0) == 0x80) { "malformed input" }
                ((c and 0x1F shl 6) or (cc and 0x3F)).toChar()
            }
            14 -> {
                /* 1110 xxxx  10xx xxxx  10xx xxxx */
                val c2 = stream.read()
                val c3 = stream.read()
                require(!(c2 and 0xC0 != 0x80 || c3 and 0xC0 != 0x80)) { "malformed input" }
                (c and 0x0F shl 12 or
                    (c2 and 0x3F shl 6) or
                    (c3 and 0x3F shl 0)).toChar()
            }
            else ->
                /* 10xx xxxx,  1111 xxxx */
                throw IllegalArgumentException("illegal string")
        }
    }

    override fun write(stream: OutputStream, value: Char) {
        val c = value.toInt()
        when {
            c in 0x0000..0x007F -> {
                stream.write(c)
            }
            c > 0x07FF -> {
                stream.write(0xE0 or (c shr 12 and 0x0F))
                stream.write(0x80 or (c shr 6 and 0x3F))
                stream.write(0x80 or (c shr 0 and 0x3F))
            }
            else -> {
                stream.write(0xC0 or (c shr 6 and 0x1F))
                stream.write(0x80 or (c shr 0 and 0x3F))
            }
        }
    }
}

internal object StringCodec : Codec<String> {
    override val tag: Int = PrimitiveTag.STRING

    override fun read(stream: InputStream): String {
        val size = IntCodec.read(stream)
        if (size == 0) {
            return ""
        }
        val builder = StringBuilder(size)
        (0..(size - 1)).forEach { builder.append(CharCodec.read(stream)) }
        return builder.toString()
    }

    override fun write(stream: OutputStream, value: String) {
        IntCodec.write(stream, value.length)
        value.forEach { CharCodec.write(stream, it) }
    }

    fun skip(stream: InputStream) {
        val size = IntCodec.read(stream)
        if (size > 0) {
            (0..(size - 1)).forEach { CharCodec.read(stream) }
        }
    }
}

internal object ByteArrayCodec : Codec<ByteArray> {
    override val tag: Int = PrimitiveTag.BYTES

    private val empty = ByteArray(0)

    override fun read(stream: InputStream): ByteArray {
        val size = IntCodec.read(stream)
        return if (size > 0) {
            val data = ByteArray(size)
            stream.read(data)
            data
        } else {
            empty
        }
    }

    override fun write(stream: OutputStream, value: ByteArray) {
        IntCodec.write(stream, value.size)
        if (value.isNotEmpty()) {
            stream.write(value)
        }
    }

    fun skip(stream: InputStream) {
        val size = IntCodec.read(stream)
        if (size > 0) {
            stream.skip(size.toLong())
        }
    }
}

internal class EnumCodec<T : Enum<T>>(enums: Iterable<T>) : Codec<T> {
    override val tag: Int = PrimitiveTag.ENUM

    private val ordMap: Map<Int, T> = enums.associateBy { it.ordinal }

    override fun read(stream: InputStream): T {
        val ord = IntCodec.read(stream)
        return ordMap[ord]!!
    }

    override fun write(stream: OutputStream, value: T) {
        IntCodec.write(stream, value.ordinal)
    }

    companion object {
        fun skip(stream: InputStream) {
            stream.skip(4)
        }
    }
}

internal class ListCodec<T>(private val codec: Codec<T>) : Codec<List<T>> {
    override val tag: Int = PrimitiveTag.LIST

    override fun read(stream: InputStream): List<T> {
        val size = IntCodec.read(stream)
        return if (size > 0) {
            val tag = stream.read()
            if (tag != codec.tag) {
                throw IllegalStateException("Unexpected tag $tag, expected ${codec.tag}")
            }
            List(size, { codec.read(stream) })
        } else {
            emptyList()
        }
    }

    override fun write(stream: OutputStream, value: List<T>) {
        IntCodec.write(stream, value.size)
        if (value.isNotEmpty()) {
            stream.write(codec.tag)
            value.forEach { i -> codec.write(stream, i) }
        }
    }

    companion object {
        fun skip(stream: InputStream) {
            val size = IntCodec.read(stream)
            if (size > 0) {
                val tag = stream.read()
                for (i in 0 until size) {
                    SkipCodec.skipTag(tag, stream)
                }
            }
        }
    }
}

internal class SetCodec<Type>(private val codec: Codec<Type>) : Codec<Set<Type>> {
    override val tag: Int = PrimitiveTag.SET

    override fun read(stream: InputStream): Set<Type> {
        val size = IntCodec.read(stream)
        return if (size > 0) {
            val tag = stream.read()
            if (tag != codec.tag) {
                throw IllegalStateException("Unexpected tag $tag, expected ${codec.tag}")
            }
            List(size, { codec.read(stream) }).toSet()
        } else {
            emptySet()
        }
    }

    override fun write(stream: OutputStream, value: Set<Type>) {
        IntCodec.write(stream, value.size)
        if (value.isNotEmpty()) {
            stream.write(codec.tag)
            value.forEach { i -> codec.write(stream, i) }
        }
    }

    companion object {
        fun skip(stream: InputStream) {
            val size = IntCodec.read(stream)
            if (size > 0) {
                val tag = stream.read()
                for (i in 0 until size) {
                    SkipCodec.skipTag(tag, stream)
                }
            }
        }
    }
}

internal class StaticCodec<T>(private val value: T) : Codec<T> {
    override val tag: Int = PrimitiveTag.STATIC

    override fun read(stream: InputStream): T {
        return value
    }

    override fun write(stream: OutputStream, value: T) = Unit
}

@Suppress("UNCHECKED_CAST")
internal class AnyCodec<T : CodecMessage> : Codec<T> {
    override val tag: Int = PrimitiveTag.ANY

    override fun read(stream: InputStream): T {
        val id = IntCodec.read(stream)
        require(id >= 0)
        val codec = idToCodecMap[id] as Codec<T>?
        requireNotNull(codec, { "class $id not registered" })
        return codec!!.read(stream)
    }

    override fun write(stream: OutputStream, value: T) {
        val id = value.messageId()
        require(id >= 0)
        val codec = idToCodecMap[id] as Codec<T>
        requireNotNull(codec)
        IntCodec.write(stream, id)
        codec.write(stream, value)
    }

    companion object {
        private val idToCodecMap = HashMap<Int, Codec<*>>()

        fun <T : CodecMessage> register(code: Int, codec: Codec<T>) {
            require(!idToCodecMap.containsKey(code))
            idToCodecMap[code] = codec
        }

        fun skip(stream: InputStream) {
            stream.skip(4)
            SkipCodec.skipObject(stream)
        }
    }
}

interface FieldCodec<T> {
    val field: Int
    val required: Boolean

    fun read(last: Int, stream: InputStream): Pair<Int, T>
    fun read(stream: InputStream): Pair<Int, T>
    fun write(stream: OutputStream, value: T)
}

class RequiredFieldCodec<T>(
    override val field: Int,
    val codec: Codec<T>
) : FieldCodec<T> {

    override fun read(last: Int, stream: InputStream): Pair<Int, T> {
        if (last < field) {
            return read(stream)
        } else {
            throw IllegalStateException("Field $field:${codec.tag} not found")
        }
    }

    override fun read(stream: InputStream): Pair<Int, T> {
        while (true) {
            val id = stream.read()
            when {
                id < field -> SkipCodec.skipTagged(stream)
                id == field -> {
                    val tag = stream.read()
                    if (tag != codec.tag) {
                        throw IllegalStateException("Unexpected tag $tag, expected ${codec.tag}")
                    }
                    return Pair(id, codec.read(stream))
                }
                else -> throw IllegalStateException("Field $field:${codec.tag} not found")
            }
        }
    }

    override fun write(stream: OutputStream, value: T) {
        stream.write(field)
        stream.write(codec.tag)
        codec.write(stream, value)
    }

    override val required = true
}

class OptionalFieldCodec<T>(
    override val field: Int,
    val codec: Codec<T>
) : FieldCodec<T?> {

    override fun read(last: Int, stream: InputStream): Pair<Int, T?> {
        return if (last < field) {
            read(stream)
        } else {
            Pair(last, null)
        }
    }

    override fun read(stream: InputStream): Pair<Int, T?> {
        while (true) {
            val id = stream.read()
            when {
                id < field -> SkipCodec.skipTagged(stream)
                id == field -> {
                    val tag = stream.read()
                    if (tag != codec.tag) {
                        throw IllegalStateException("Unexpected tag $tag, expected ${codec.tag}")
                    }
                    return Pair(id, codec.read(stream))
                }
                else -> return Pair(id, null)
            }
        }
    }

    override fun write(stream: OutputStream, value: T?) {
        if (value != null) {
            stream.write(field)
            stream.write(codec.tag)
            codec.write(stream, value)
        }
    }

    override val required = false
}

internal object SkipCodec {
    fun skipTagged(stream: InputStream) {
        val tag = stream.read()
        skipTag(tag, stream)
    }

    fun skipTag(tag: Int, stream: InputStream) {
        when (tag) {
            PrimitiveTag.BOOLEAN -> stream.skip(1)
            PrimitiveTag.BYTE -> stream.skip(1)
            PrimitiveTag.SHORT -> stream.skip(2)
            PrimitiveTag.INT -> stream.skip(4)
            PrimitiveTag.LONG -> stream.skip(8)
            PrimitiveTag.CHAR -> CharCodec.read(stream)
            PrimitiveTag.STRING -> StringCodec.skip(stream)
            PrimitiveTag.BYTES -> ByteArrayCodec.skip(stream)
            PrimitiveTag.LIST -> ListCodec.skip(stream)
            PrimitiveTag.SET -> SetCodec.skip(stream)
            PrimitiveTag.ENUM -> EnumCodec.skip(stream)
            PrimitiveTag.STATIC -> Unit
            PrimitiveTag.ANY -> AnyCodec.skip(stream)
            PrimitiveTag.OBJECT -> skipObject(stream)
            else -> throw IllegalStateException("Unexpected field type tag: $tag")
        }
    }

    fun skipObject(stream: InputStream) {
        do {
            val id = stream.read()
            if (id < PrimitiveTag.OBJECT_END) {
                skipTagged(stream)
            }
        } while (id != PrimitiveTag.OBJECT_END)
    }

    fun skipObject(last: Int, stream: InputStream) {
        if (last < PrimitiveTag.OBJECT_END) {
            skipObject(stream)
        }
    }
}

internal class Codec1<Type, Param1>(
    private val builder: Function1<Param1, Type>,
    private val lens1: Function1<Type, Param1>,
    private val codec1: FieldCodec<Param1>
) : Codec<Type> {
    override val tag: Int = PrimitiveTag.OBJECT

    override fun read(stream: InputStream): Type {
        val (last1, param1) = codec1.read(stream)
        SkipCodec.skipObject(last1, stream)
        return builder.invoke(param1)
    }

    override fun write(stream: OutputStream, value: Type) {
        codec1.write(stream, lens1.invoke(value))
        stream.write(PrimitiveTag.OBJECT_END)
    }
}

internal class Codec2<Type, Param1, Param2>(
    private val builder: Function2<Param1, Param2, Type>,
    private val lens1: Function1<Type, Param1>,
    private val lens2: Function1<Type, Param2>,
    private val codec1: FieldCodec<Param1>,
    private val codec2: FieldCodec<Param2>
) : Codec<Type> {
    override val tag: Int = PrimitiveTag.OBJECT

    override fun read(stream: InputStream): Type {
        val (last1, param1) = codec1.read(stream)
        val (last2, param2) = codec2.read(last1, stream)
        SkipCodec.skipObject(last2, stream)
        return builder.invoke(param1, param2)
    }

    override fun write(stream: OutputStream, value: Type) {
        codec1.write(stream, lens1.invoke(value))
        codec2.write(stream, lens2.invoke(value))
        stream.write(PrimitiveTag.OBJECT_END)
    }
}

internal class Codec3<Type, Param1, Param2, Param3>(
    private val builder: Function3<Param1, Param2, Param3, Type>,
    private val lens1: Function1<Type, Param1>,
    private val lens2: Function1<Type, Param2>,
    private val lens3: Function1<Type, Param3>,
    private val codec1: FieldCodec<Param1>,
    private val codec2: FieldCodec<Param2>,
    private val codec3: FieldCodec<Param3>
) : Codec<Type> {
    override val tag: Int = PrimitiveTag.OBJECT

    override fun read(stream: InputStream): Type {
        val (last1, param1) = codec1.read(stream)
        val (last2, param2) = codec2.read(last1, stream)
        val (last3, param3) = codec3.read(last2, stream)
        SkipCodec.skipObject(last3, stream)
        return builder.invoke(param1, param2, param3)
    }

    override fun write(stream: OutputStream, value: Type) {
        codec1.write(stream, lens1.invoke(value))
        codec2.write(stream, lens2.invoke(value))
        codec3.write(stream, lens3.invoke(value))
        stream.write(PrimitiveTag.OBJECT_END)
    }
}

internal class Codec4<Type, Param1, Param2, Param3, Param4>(
    private val builder: Function4<Param1, Param2, Param3, Param4, Type>,
    private val lens1: Function1<Type, Param1>,
    private val lens2: Function1<Type, Param2>,
    private val lens3: Function1<Type, Param3>,
    private val lens4: Function1<Type, Param4>,
    private val codec1: FieldCodec<Param1>,
    private val codec2: FieldCodec<Param2>,
    private val codec3: FieldCodec<Param3>,
    private val codec4: FieldCodec<Param4>
) : Codec<Type> {
    override val tag: Int = PrimitiveTag.OBJECT

    override fun read(stream: InputStream): Type {
        val (last1, param1) = codec1.read(stream)
        val (last2, param2) = codec2.read(last1, stream)
        val (last3, param3) = codec3.read(last2, stream)
        val (last4, param4) = codec4.read(last3, stream)
        SkipCodec.skipObject(last4, stream)
        return builder.invoke(param1, param2, param3, param4)
    }

    override fun write(stream: OutputStream, value: Type) {
        codec1.write(stream, lens1.invoke(value))
        codec2.write(stream, lens2.invoke(value))
        codec3.write(stream, lens3.invoke(value))
        codec4.write(stream, lens4.invoke(value))
        stream.write(PrimitiveTag.OBJECT_END)
    }
}

internal class Codec5<Type, Param1, Param2, Param3, Param4, Param5>(
    private val builder: Function5<Param1, Param2, Param3, Param4, Param5, Type>,
    private val lens1: Function1<Type, Param1>,
    private val lens2: Function1<Type, Param2>,
    private val lens3: Function1<Type, Param3>,
    private val lens4: Function1<Type, Param4>,
    private val lens5: Function1<Type, Param5>,
    private val codec1: FieldCodec<Param1>,
    private val codec2: FieldCodec<Param2>,
    private val codec3: FieldCodec<Param3>,
    private val codec4: FieldCodec<Param4>,
    private val codec5: FieldCodec<Param5>
) : Codec<Type> {
    override val tag: Int = PrimitiveTag.OBJECT

    override fun read(stream: InputStream): Type {
        val (last1, param1) = codec1.read(stream)
        val (last2, param2) = codec2.read(last1, stream)
        val (last3, param3) = codec3.read(last2, stream)
        val (last4, param4) = codec4.read(last3, stream)
        val (last5, param5) = codec5.read(last4, stream)
        SkipCodec.skipObject(last5, stream)
        return builder.invoke(param1, param2, param3, param4, param5)
    }

    override fun write(stream: OutputStream, value: Type) {
        codec1.write(stream, lens1.invoke(value))
        codec2.write(stream, lens2.invoke(value))
        codec3.write(stream, lens3.invoke(value))
        codec4.write(stream, lens4.invoke(value))
        codec5.write(stream, lens5.invoke(value))
        stream.write(PrimitiveTag.OBJECT_END)
    }
}

internal class Codec6<Type, Param1, Param2, Param3, Param4, Param5, Param6>(
    private val builder: Function6<Param1, Param2, Param3, Param4, Param5, Param6, Type>,
    private val lens1: Function1<Type, Param1>,
    private val lens2: Function1<Type, Param2>,
    private val lens3: Function1<Type, Param3>,
    private val lens4: Function1<Type, Param4>,
    private val lens5: Function1<Type, Param5>,
    private val lens6: Function1<Type, Param6>,
    private val codec1: FieldCodec<Param1>,
    private val codec2: FieldCodec<Param2>,
    private val codec3: FieldCodec<Param3>,
    private val codec4: FieldCodec<Param4>,
    private val codec5: FieldCodec<Param5>,
    private val codec6: FieldCodec<Param6>
) : Codec<Type> {
    override val tag: Int = PrimitiveTag.OBJECT

    override fun read(stream: InputStream): Type {
        val (last1, param1) = codec1.read(stream)
        val (last2, param2) = codec2.read(last1, stream)
        val (last3, param3) = codec3.read(last2, stream)
        val (last4, param4) = codec4.read(last3, stream)
        val (last5, param5) = codec5.read(last4, stream)
        val (last6, param6) = codec6.read(last5, stream)
        SkipCodec.skipObject(last6, stream)
        return builder.invoke(param1, param2, param3, param4, param5, param6)
    }

    override fun write(stream: OutputStream, value: Type) {
        codec1.write(stream, lens1.invoke(value))
        codec2.write(stream, lens2.invoke(value))
        codec3.write(stream, lens3.invoke(value))
        codec4.write(stream, lens4.invoke(value))
        codec5.write(stream, lens5.invoke(value))
        codec6.write(stream, lens6.invoke(value))
        stream.write(PrimitiveTag.OBJECT_END)
    }
}
