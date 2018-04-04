package com.github.mitallast.ghost.e2e

import com.github.mitallast.ghost.common.codec.Codec
import com.github.mitallast.ghost.common.codec.Message

class E2ERequest(
    val from: ByteArray,
    val to: ByteArray,
    val ecdhPublicKey: ByteArray,
    val ecdsaPublicKey: ByteArray,
    val sign: ByteArray
) : Message {

    override fun messageId(): Int = messageId

    companion object {
        const val messageId = 0x0200
        val codec = Codec.of(
            ::E2ERequest,
            E2ERequest::from,
            E2ERequest::to,
            E2ERequest::ecdhPublicKey,
            E2ERequest::ecdsaPublicKey,
            E2ERequest::sign,
            Codec.bytesCodec(),
            Codec.bytesCodec(),
            Codec.bytesCodec(),
            Codec.bytesCodec(),
            Codec.bytesCodec()
        )
    }
}

class E2EResponse(
    val from: ByteArray,
    val to: ByteArray,
    val ecdhPublicKey: ByteArray,
    val ecdsaPublicKey: ByteArray,
    val sign: ByteArray
) : Message {

    override fun messageId(): Int = messageId

    companion object {
        const val messageId = 0x0201
        val codec = Codec.of(
            ::E2EResponse,
            E2EResponse::from,
            E2EResponse::to,
            E2EResponse::ecdhPublicKey,
            E2EResponse::ecdsaPublicKey,
            E2EResponse::sign,
            Codec.bytesCodec(),
            Codec.bytesCodec(),
            Codec.bytesCodec(),
            Codec.bytesCodec(),
            Codec.bytesCodec()
        )
    }
}

class E2EEncrypted(
    val from: ByteArray,
    val to: ByteArray,
    val sign: ByteArray,
    val iv: ByteArray,
    val encrypted: ByteArray
) : Message {

    override fun messageId(): Int = messageId

    companion object {
        const val messageId = 0x0202
        val codec = Codec.of(
            ::E2EEncrypted,
            E2EEncrypted::from,
            E2EEncrypted::to,
            E2EEncrypted::sign,
            E2EEncrypted::iv,
            E2EEncrypted::encrypted,
            Codec.bytesCodec(),
            Codec.bytesCodec(),
            Codec.bytesCodec(),
            Codec.bytesCodec(),
            Codec.bytesCodec()
        )
    }
}