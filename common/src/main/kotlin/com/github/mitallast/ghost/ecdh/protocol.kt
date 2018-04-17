package com.github.mitallast.ghost.ecdh

import com.github.mitallast.ghost.common.codec.CodecMessage
import com.github.mitallast.ghost.common.codec.Codec

class ECDHRequest(
    val ecdhPublicKey: ByteArray,
    val ecdsaPublicKey: ByteArray,
    val sign: ByteArray
) : CodecMessage {

    override fun messageId(): Int = messageId

    companion object {
        const val messageId = 0x0100
        val codec = Codec.of(
            ::ECDHRequest,
            ECDHRequest::ecdhPublicKey,
            ECDHRequest::ecdsaPublicKey,
            ECDHRequest::sign,
            Codec.field(1, Codec.bytesCodec()),
            Codec.field(2, Codec.bytesCodec()),
            Codec.field(3, Codec.bytesCodec())
        )
    }
}

class ECDHResponse(
    val auth: ByteArray,
    val ecdhPublicKey: ByteArray,
    val sign: ByteArray
) : CodecMessage {

    override fun messageId(): Int = messageId

    companion object {
        const val messageId = 0x0101
        val codec = Codec.of(
            ::ECDHResponse,
            ECDHResponse::auth,
            ECDHResponse::ecdhPublicKey,
            ECDHResponse::sign,
            Codec.field(1, Codec.bytesCodec()),
            Codec.field(2, Codec.bytesCodec()),
            Codec.field(3, Codec.bytesCodec())
        )
    }
}

class ECDHReconnect(
    val auth: ByteArray,
    val sign: ByteArray
) : CodecMessage {

    override fun messageId(): Int = messageId

    companion object {
        const val messageId = 0x0102
        val codec = Codec.of(
            ::ECDHReconnect,
            ECDHReconnect::auth,
            ECDHReconnect::sign,
            Codec.field(1, Codec.bytesCodec()),
            Codec.field(2, Codec.bytesCodec())
        )
    }
}

class ECDHEncrypted(
    val auth: ByteArray,
    val sign: ByteArray,
    val iv: ByteArray,
    val encrypted: ByteArray
) : CodecMessage {

    override fun messageId(): Int = messageId

    companion object {
        const val messageId = 0x0103
        val codec = Codec.of(
            ::ECDHEncrypted,
            ECDHEncrypted::auth,
            ECDHEncrypted::sign,
            ECDHEncrypted::iv,
            ECDHEncrypted::encrypted,
            Codec.field(1, Codec.bytesCodec()),
            Codec.field(2, Codec.bytesCodec()),
            Codec.field(3, Codec.bytesCodec()),
            Codec.field(4, Codec.bytesCodec())
        )
    }
}