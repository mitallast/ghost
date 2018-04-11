package com.github.mitallast.ghost.message

import com.github.mitallast.ghost.common.codec.Codec
import com.github.mitallast.ghost.common.codec.CodecMessage

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
            Codec.longCodec(),
            Codec.longCodec(),
            Codec.bytesCodec(),
            Codec.anyCodec()
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
            Codec.stringCodec()
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
            Codec.stringCodec()
        )
    }
}

class EncryptedFileMessage(
    val name: String,
    val size: Int,
    val mimetype: String,
    val address: String,
    val sign: ByteArray,
    val iv: ByteArray
) : MessageContent {

    override fun messageId(): Int = messageId

    companion object {
        const val messageId = 0x0603
        val codec = Codec.of(
            ::EncryptedFileMessage,
            EncryptedFileMessage::name,
            EncryptedFileMessage::size,
            EncryptedFileMessage::mimetype,
            EncryptedFileMessage::address,
            EncryptedFileMessage::sign,
            EncryptedFileMessage::iv,
            Codec.stringCodec(),
            Codec.intCodec(),
            Codec.stringCodec(),
            Codec.stringCodec(),
            Codec.bytesCodec(),
            Codec.bytesCodec()
        )
    }
}