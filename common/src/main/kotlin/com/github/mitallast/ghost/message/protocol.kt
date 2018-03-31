package com.github.mitallast.ghost.message

import com.github.mitallast.ghost.common.codec.Codec
import com.github.mitallast.ghost.common.codec.Message

class TextMessage(val text: String) : Message {

    override fun messageId(): Int = messageId

    companion object {
        const val messageId = 200
        val codec = Codec.of(
            ::TextMessage,
            TextMessage::text,
            Codec.stringCodec()
        )
    }
}