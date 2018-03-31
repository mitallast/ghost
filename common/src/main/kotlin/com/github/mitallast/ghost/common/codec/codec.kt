package com.github.mitallast.ghost.common.codec

import kotlinx.io.ByteArrayInputStream
import kotlinx.io.ByteArrayOutputStream
import kotlinx.io.InputStream
import kotlinx.io.OutputStream

interface Message {
    fun messageId(): Int
}

interface Codec<T> {
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

        fun <T : Message> register(code: Int, codec: Codec<T>) {
            AnyCodec.register(code, codec)
        }

        fun booleanCodec(): Codec<Boolean> = BooleanCodec
        fun intCodec(): Codec<Int> = IntCodec
        fun longCodec(): Codec<Long> = LongCodec
        fun stringCodec(): Codec<String> = StringCodec
        fun bytesCodec(): Codec<ByteArray> = ByteArrayCodec

        fun <T : Message> anyCodec(): Codec<T> = AnyCodec()
        fun <T : Enum<T>> enumCodec(vararg enums: T): Codec<T> = EnumCodec(enums.asIterable())
        fun <T> optionCodec(codec: Codec<T>): Codec<T?> = OptionCodec(codec)
        fun <T> listCodec(codec: Codec<T>): Codec<List<T>> = ListCodec(codec)
        fun <T> setCodec(codec: Codec<T>): Codec<Set<T>> = SetCodec(codec)

        fun <T> of(value: T): Codec<T> = StaticCodec(value)

        fun <Type, Param1> of(
            builder: Function1<Param1, Type>,
            lens1: Function1<Type, Param1>,
            codec1: Codec<Param1>
        ): Codec<Type> {
            return Codec1(builder, lens1, codec1)
        }

        fun <Type, Param1, Param2> of(
            builder: Function2<Param1, Param2, Type>,
            lens1: Function1<Type, Param1>,
            lens2: Function1<Type, Param2>,
            codec1: Codec<Param1>,
            codec2: Codec<Param2>
        ): Codec<Type> {
            return Codec2(builder, lens1, lens2, codec1, codec2)
        }

        fun <Type, Param1, Param2, Param3> of(
            builder: Function3<Param1, Param2, Param3, Type>,
            lens1: Function1<Type, Param1>,
            lens2: Function1<Type, Param2>,
            lens3: Function1<Type, Param3>,
            codec1: Codec<Param1>,
            codec2: Codec<Param2>,
            codec3: Codec<Param3>
        ): Codec<Type> {
            return Codec3(builder, lens1, lens2, lens3, codec1, codec2, codec3)
        }

        fun <Type, Param1, Param2, Param3, Param4> of(
            builder: Function4<Param1, Param2, Param3, Param4, Type>,
            lens1: Function1<Type, Param1>,
            lens2: Function1<Type, Param2>,
            lens3: Function1<Type, Param3>,
            lens4: Function1<Type, Param4>,
            codec1: Codec<Param1>,
            codec2: Codec<Param2>,
            codec3: Codec<Param3>,
            codec4: Codec<Param4>
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
            codec1: Codec<Param1>,
            codec2: Codec<Param2>,
            codec3: Codec<Param3>,
            codec4: Codec<Param4>,
            codec5: Codec<Param5>
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
            codec1: Codec<Param1>,
            codec2: Codec<Param2>,
            codec3: Codec<Param3>,
            codec4: Codec<Param4>,
            codec5: Codec<Param5>,
            codec6: Codec<Param6>
        ): Codec<Type> {
            return Codec6(builder,
                lens1, lens2, lens3, lens4, lens5, lens6,
                codec1, codec2, codec3, codec4, codec5, codec6)
        }
    }
}

internal object BooleanCodec : Codec<Boolean> {
    override fun read(stream: InputStream): Boolean {
        return stream.read() > 0
    }

    override fun write(stream: OutputStream, value: Boolean) {
        stream.write(if (value) 1 else 0)
    }
}

internal object IntCodec : Codec<Int> {
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
        stream.write((value shr 46 and 0xFF).toInt())
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
}

internal object ByteArrayCodec : Codec<ByteArray> {
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
}

internal class EnumCodec<T : Enum<T>>(enums: Iterable<T>) : Codec<T> {
    private val ordMap: Map<Int, T> = enums.associateBy { it.ordinal }

    override fun read(stream: InputStream): T {
        val ord = IntCodec.read(stream)
        return ordMap[ord]!!
    }

    override fun write(stream: OutputStream, value: T) {
        IntCodec.write(stream, value.ordinal)
    }
}

internal class OptionCodec<T>(private val codec: Codec<T>) : Codec<T?> {

    override fun read(stream: InputStream): T? {
        return if (BooleanCodec.read(stream)) {
            codec.read(stream)
        } else {
            null
        }
    }

    override fun write(stream: OutputStream, value: T?) {
        if (value == null) {
            BooleanCodec.write(stream, false)
        } else {
            BooleanCodec.write(stream, true)
            codec.write(stream, value)
        }
    }
}

internal class ListCodec<T>(private val codec: Codec<T>) : Codec<List<T>> {

    override fun read(stream: InputStream): List<T> {
        val size = IntCodec.read(stream)
        return List(size, { codec.read(stream) })
    }

    override fun write(stream: OutputStream, value: List<T>) {
        IntCodec.write(stream, value.size)
        value.forEach { i -> codec.write(stream, i) }
    }
}

internal class SetCodec<Type>(private val codec: Codec<Type>) : Codec<Set<Type>> {

    override fun read(stream: InputStream): Set<Type> {
        val size = IntCodec.read(stream)
        return List(size, { codec.read(stream) }).toSet()
    }

    override fun write(stream: OutputStream, value: Set<Type>) {
        IntCodec.write(stream, value.size)
        value.forEach { i -> codec.write(stream, i) }
    }
}

internal class Codec1<Type, Param1>(
    private val builder: Function1<Param1, Type>,
    private val lens1: Function1<Type, Param1>,
    private val codec1: Codec<Param1>
) : Codec<Type> {

    override fun read(stream: InputStream): Type {
        val param1 = codec1.read(stream)
        return builder.invoke(param1)
    }

    override fun write(stream: OutputStream, value: Type) {
        codec1.write(stream, lens1.invoke(value))
    }
}

internal class Codec2<Type, Param1, Param2>(
    private val builder: Function2<Param1, Param2, Type>,
    private val lens1: Function1<Type, Param1>,
    private val lens2: Function1<Type, Param2>,
    private val codec1: Codec<Param1>,
    private val codec2: Codec<Param2>
) : Codec<Type> {

    override fun read(stream: InputStream): Type {
        val param1 = codec1.read(stream)
        val param2 = codec2.read(stream)
        return builder.invoke(param1, param2)
    }

    override fun write(stream: OutputStream, value: Type) {
        codec1.write(stream, lens1.invoke(value))
        codec2.write(stream, lens2.invoke(value))
    }
}

internal class Codec3<Type, Param1, Param2, Param3>(
    private val builder: Function3<Param1, Param2, Param3, Type>,
    private val lens1: Function1<Type, Param1>,
    private val lens2: Function1<Type, Param2>,
    private val lens3: Function1<Type, Param3>,
    private val codec1: Codec<Param1>,
    private val codec2: Codec<Param2>,
    private val codec3: Codec<Param3>
) : Codec<Type> {

    override fun read(stream: InputStream): Type {
        val param1 = codec1.read(stream)
        val param2 = codec2.read(stream)
        val param3 = codec3.read(stream)
        return builder.invoke(param1, param2, param3)
    }

    override fun write(stream: OutputStream, value: Type) {
        codec1.write(stream, lens1.invoke(value))
        codec2.write(stream, lens2.invoke(value))
        codec3.write(stream, lens3.invoke(value))
    }
}

internal class Codec4<Type, Param1, Param2, Param3, Param4>(
    private val builder: Function4<Param1, Param2, Param3, Param4, Type>,
    private val lens1: Function1<Type, Param1>,
    private val lens2: Function1<Type, Param2>,
    private val lens3: Function1<Type, Param3>,
    private val lens4: Function1<Type, Param4>,
    private val codec1: Codec<Param1>,
    private val codec2: Codec<Param2>,
    private val codec3: Codec<Param3>,
    private val codec4: Codec<Param4>
) : Codec<Type> {

    override fun read(stream: InputStream): Type {
        val param1 = codec1.read(stream)
        val param2 = codec2.read(stream)
        val param3 = codec3.read(stream)
        val param4 = codec4.read(stream)
        return builder.invoke(param1, param2, param3, param4)
    }

    override fun write(stream: OutputStream, value: Type) {
        codec1.write(stream, lens1.invoke(value))
        codec2.write(stream, lens2.invoke(value))
        codec3.write(stream, lens3.invoke(value))
        codec4.write(stream, lens4.invoke(value))
    }
}

internal class Codec5<Type, Param1, Param2, Param3, Param4, Param5>(
    private val builder: Function5<Param1, Param2, Param3, Param4, Param5, Type>,
    private val lens1: Function1<Type, Param1>,
    private val lens2: Function1<Type, Param2>,
    private val lens3: Function1<Type, Param3>,
    private val lens4: Function1<Type, Param4>,
    private val lens5: Function1<Type, Param5>,
    private val codec1: Codec<Param1>,
    private val codec2: Codec<Param2>,
    private val codec3: Codec<Param3>,
    private val codec4: Codec<Param4>,
    private val codec5: Codec<Param5>
) : Codec<Type> {

    override fun read(stream: InputStream): Type {
        val param1 = codec1.read(stream)
        val param2 = codec2.read(stream)
        val param3 = codec3.read(stream)
        val param4 = codec4.read(stream)
        val param5 = codec5.read(stream)
        return builder.invoke(param1, param2, param3, param4, param5)
    }

    override fun write(stream: OutputStream, value: Type) {
        codec1.write(stream, lens1.invoke(value))
        codec2.write(stream, lens2.invoke(value))
        codec3.write(stream, lens3.invoke(value))
        codec4.write(stream, lens4.invoke(value))
        codec5.write(stream, lens5.invoke(value))
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
    private val codec1: Codec<Param1>,
    private val codec2: Codec<Param2>,
    private val codec3: Codec<Param3>,
    private val codec4: Codec<Param4>,
    private val codec5: Codec<Param5>,
    private val codec6: Codec<Param6>
) : Codec<Type> {

    override fun read(stream: InputStream): Type {
        val param1 = codec1.read(stream)
        val param2 = codec2.read(stream)
        val param3 = codec3.read(stream)
        val param4 = codec4.read(stream)
        val param5 = codec5.read(stream)
        val param6 = codec6.read(stream)
        return builder.invoke(param1, param2, param3, param4, param5, param6)
    }

    override fun write(stream: OutputStream, value: Type) {
        codec1.write(stream, lens1.invoke(value))
        codec2.write(stream, lens2.invoke(value))
        codec3.write(stream, lens3.invoke(value))
        codec4.write(stream, lens4.invoke(value))
        codec5.write(stream, lens5.invoke(value))
        codec6.write(stream, lens6.invoke(value))
    }
}

internal class StaticCodec<T>(private val value: T) : Codec<T> {

    override fun read(stream: InputStream): T {
        return value
    }

    override fun write(stream: OutputStream, value: T) {}
}

@Suppress("UNCHECKED_CAST")
internal class AnyCodec<T : Message> : Codec<T> {

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

        fun <T : Message> register(code: Int, codec: Codec<T>) {
            require(!idToCodecMap.containsKey(code))
            idToCodecMap[code] = codec
        }
    }
}