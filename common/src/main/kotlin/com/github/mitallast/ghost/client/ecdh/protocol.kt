package com.github.mitallast.ghost.client.ecdh

import com.github.mitallast.ghost.common.codec.Message
import com.github.mitallast.ghost.common.codec.Codec

class ECDHRequest(
    val ecdhPublicKey: ByteArray,
    val ecdsaPublicKey: ByteArray,
    val sign: ByteArray
) : Message {

    override fun messageId(): Int = messageId

    companion object {
        const val messageId = 0x0100
        val codec = Codec.of(
            ::ECDHRequest,
            ECDHRequest::ecdhPublicKey,
            ECDHRequest::ecdsaPublicKey,
            ECDHRequest::sign,
            Codec.bytesCodec(),
            Codec.bytesCodec(),
            Codec.bytesCodec()
        )
    }
}

class ECDHResponse(
    val auth: ByteArray,
    val ecdhPublicKey: ByteArray,
    val sign: ByteArray
) : Message {

    override fun messageId(): Int = messageId

    companion object {
        const val messageId = 0x0101
        val codec = Codec.of(
            ::ECDHResponse,
            ECDHResponse::auth,
            ECDHResponse::ecdhPublicKey,
            ECDHResponse::sign,
            Codec.bytesCodec(),
            Codec.bytesCodec(),
            Codec.bytesCodec()
        )
    }
}

class ECDHReconnect(
    val auth: ByteArray,
    val sign: ByteArray
) : Message {

    override fun messageId(): Int = messageId

    companion object {
        const val messageId = 0x0102
        val codec = Codec.of(
            ::ECDHReconnect,
            ECDHReconnect::auth,
            ECDHReconnect::sign,
            Codec.bytesCodec(),
            Codec.bytesCodec()
        )
    }
}

class ECDHEncrypted(
    val auth: ByteArray,
    val sign: ByteArray,
    val iv: ByteArray,
    val encrypted: ByteArray
) : Message {

    override fun messageId(): Int = messageId

    companion object {
        const val messageId = 0x0103
        val codec = Codec.of(
            ::ECDHEncrypted,
            ECDHEncrypted::auth,
            ECDHEncrypted::sign,
            ECDHEncrypted::iv,
            ECDHEncrypted::encrypted,
            Codec.bytesCodec(),
            Codec.bytesCodec(),
            Codec.bytesCodec(),
            Codec.bytesCodec()
        )
    }
}