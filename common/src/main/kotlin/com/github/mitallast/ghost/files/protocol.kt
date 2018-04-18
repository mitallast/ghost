package com.github.mitallast.ghost.files

import com.github.mitallast.ghost.common.codec.Codec
import com.github.mitallast.ghost.common.codec.CodecMessage

class Thumb(
    val width: Int,
    val height: Int,
    val thumb: ByteArray
) : CodecMessage {
    override fun messageId(): Int = messageId

    companion object {
        const val messageId = 0x0500
        val codec = Codec.of(
            ::Thumb,
            Thumb::width,
            Thumb::height,
            Thumb::thumb,
            Codec.field(1, Codec.intCodec()),
            Codec.field(2, Codec.intCodec()),
            Codec.field(3, Codec.bytesCodec())
        )
    }
}

class EncryptedFile(
    val name: String,
    val size: Int,
    val mimetype: String,
    val address: String,
    val secretKey: ByteArray,
    val verifyKey: ByteArray,
    val sign: ByteArray,
    val iv: ByteArray
) : CodecMessage {

    override fun messageId(): Int = messageId

    companion object {
        const val messageId = 0x0501
        val codec = Codec.of(
            ::EncryptedFile,
            EncryptedFile::name,
            EncryptedFile::size,
            EncryptedFile::mimetype,
            EncryptedFile::address,
            EncryptedFile::secretKey,
            EncryptedFile::verifyKey,
            EncryptedFile::sign,
            EncryptedFile::iv,
            Codec.field(1, Codec.stringCodec()),
            Codec.field(2, Codec.intCodec()),
            Codec.field(3, Codec.stringCodec()),
            Codec.field(4, Codec.stringCodec()),
            Codec.field(5, Codec.bytesCodec()),
            Codec.field(6, Codec.bytesCodec()),
            Codec.field(7, Codec.bytesCodec()),
            Codec.field(8, Codec.bytesCodec())
        )
    }
}