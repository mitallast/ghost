package com.github.mitallast.ghost

import com.github.mitallast.ghost.codec.Message
import com.github.mitallast.ghost.codec.Codec

data class HelloMessage(
    val id: Long,
    val text: String
) : Message {
    override fun messageId(): Int = messageId

    companion object {
        const val messageId = 1
        val codec = Codec.of(
            ::HelloMessage,
            HelloMessage::id,
            HelloMessage::text,
            Codec.longCodec(),
            Codec.stringCodec()
        )
        init {
            Codec.register(messageId, codec)
        }
    }
}