package com.github.mitallast.ghost.message

import com.github.mitallast.ghost.common.codec.Codec
import com.github.mitallast.ghost.common.codec.CodecMessage
import com.github.mitallast.ghost.files.EncryptedFile

class Message(
    val date: Long,
    val randomId: Long,
    val sender: ByteArray,
    val content: CodecMessage
) : CodecMessage {

    fun olderThan(that: Message): Boolean {
        return (date < that.date) || (date == that.date && randomId < that.randomId)
    }

    override fun messageId(): Int = messageId

    companion object {
        const val messageId = 0x0600
        val codec = Codec.of(
            ::Message,
            Message::date,
            Message::randomId,
            Message::sender,
            Message::content,
            Codec.field(1, Codec.longCodec()),
            Codec.field(2, Codec.longCodec()),
            Codec.field(3, Codec.bytesCodec()),
            Codec.field(4, Codec.anyCodec())
        )
    }
}

interface MessageContent : CodecMessage

class TextMessage(val text: String) : MessageContent {
    override fun messageId(): Int = messageId

    companion object {
        const val messageId = 0x0601
        val codec = Codec.of(
            ::TextMessage,
            TextMessage::text,
            Codec.field(1, Codec.stringCodec())
        )
    }
}

class ServiceMessage(val text: String) : MessageContent {
    override fun messageId(): Int = messageId

    companion object {
        const val messageId = 0x0602
        val codec = Codec.of(
            ::ServiceMessage,
            ServiceMessage::text,
            Codec.field(1, Codec.stringCodec())
        )
    }
}

class FileMessage(val file: EncryptedFile) : MessageContent {
    override fun messageId(): Int = messageId

    companion object {
        const val messageId = 0x0603
        val codec = Codec.of(
            ::FileMessage,
            FileMessage::file,
            Codec.field(1, EncryptedFile.codec)
        )
    }
}