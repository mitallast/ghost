package com.github.mitallast.ghost.files

import com.github.mitallast.ghost.common.codec.Codec
import com.github.mitallast.ghost.common.codec.Message

class Thumb(
    val width: Int,
    val height: Int,
    val thumb: ByteArray
) : Message {
    override fun messageId(): Int = messageId

    companion object {
        const val messageId = 0x0500
        val codec = Codec.of(
            ::Thumb,
            Thumb::width,
            Thumb::height,
            Thumb::thumb,
            Codec.intCodec(),
            Codec.intCodec(),
            Codec.bytesCodec()
        )
    }
}