package com.github.mitallast.ghost.e2e

import com.github.mitallast.ghost.common.codec.Codec
import com.github.mitallast.ghost.common.codec.CodecMessage

class E2EAuthRequest(
    val from: ByteArray,
    val to: ByteArray,
    val ecdhPublicKey: ByteArray,
    val ecdsaPublicKey: ByteArray,
    val sign: ByteArray
) : CodecMessage {

    override fun messageId(): Int = messageId

    companion object {
        const val messageId = 0x0200
        val codec = Codec.of(
            ::E2EAuthRequest,
            E2EAuthRequest::from,
            E2EAuthRequest::to,
            E2EAuthRequest::ecdhPublicKey,
            E2EAuthRequest::ecdsaPublicKey,
            E2EAuthRequest::sign,
            Codec.bytesCodec(),
            Codec.bytesCodec(),
            Codec.bytesCodec(),
            Codec.bytesCodec(),
            Codec.bytesCodec()
        )
    }
}

class E2EAuthResponse(
    val from: ByteArray,
    val to: ByteArray,
    val ecdhPublicKey: ByteArray,
    val ecdsaPublicKey: ByteArray,
    val sign: ByteArray
) : CodecMessage {

    override fun messageId(): Int = messageId

    companion object {
        const val messageId = 0x0201
        val codec = Codec.of(
            ::E2EAuthResponse,
            E2EAuthResponse::from,
            E2EAuthResponse::to,
            E2EAuthResponse::ecdhPublicKey,
            E2EAuthResponse::ecdsaPublicKey,
            E2EAuthResponse::sign,
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
) : CodecMessage {

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

    fun toComplete(): E2EComplete = E2EComplete(
            from,
            to,
            sign,
            iv,
            encrypted
    )
}

class E2EComplete(
        val from: ByteArray,
        val to: ByteArray,
        val sign: ByteArray,
        val iv: ByteArray,
        val encrypted: ByteArray
) : CodecMessage {

    override fun messageId(): Int = messageId

    companion object {
        const val messageId = 0x0203
        val codec = Codec.of(
                ::E2EComplete,
                E2EComplete::from,
                E2EComplete::to,
                E2EComplete::sign,
                E2EComplete::iv,
                E2EComplete::encrypted,
                Codec.bytesCodec(),
                Codec.bytesCodec(),
                Codec.bytesCodec(),
                Codec.bytesCodec(),
                Codec.bytesCodec()
        )
    }

    fun toEncrypted(): E2EEncrypted = E2EEncrypted(
        from,
        to,
        sign,
        iv,
        encrypted
    )
}

// @todo add validation data for auth response
// @todo request canceled