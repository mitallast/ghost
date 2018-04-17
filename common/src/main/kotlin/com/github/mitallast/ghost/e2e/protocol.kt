package com.github.mitallast.ghost.e2e

import com.github.mitallast.ghost.common.codec.Codec
import com.github.mitallast.ghost.common.codec.CodecMessage

/*
1) alice send auth request
Alice -> Bob: E2EAuthRequest

2) Bob respond to auth request
Bob->Bob: validate request
Bob->Bob: enter offline password
Bob->Bob: generate secret key
Bob->Bob: generate validation data
Bob->Bob: encrypt validation data
Bob->Alice: E2EAuthResponse

3) Alice complete auth
Alice->Alice: validate response
Alice->Alice: enter offline password
Alice->Alice: generate secret key
Alice->Alice: decrypt Bob profile
Alice->Alice: validate secret key
Alice->Alice: encrypt profile
Alice->Bob: E2EAuthComplete
*/

class E2EAuthRequest(
    val ecdhPublicKey: ByteArray,
    val ecdsaPublicKey: ByteArray,
    val sign: ByteArray
) : CodecMessage {

    override fun messageId(): Int = messageId

    companion object {
        const val messageId = 0x0200
        val codec = Codec.of(
            ::E2EAuthRequest,
            E2EAuthRequest::ecdhPublicKey,
            E2EAuthRequest::ecdsaPublicKey,
            E2EAuthRequest::sign,
            Codec.field(1, Codec.bytesCodec()),
            Codec.field(2, Codec.bytesCodec()),
            Codec.field(3, Codec.bytesCodec())
        )
    }
}

class E2EAuthResponse(
    val ecdhPublicKey: ByteArray,
    val ecdsaPublicKey: ByteArray,
    val sign: ByteArray,
    val iv: ByteArray,
    val encrypted: ByteArray
) : CodecMessage {

    override fun messageId(): Int = messageId

    companion object {
        const val messageId = 0x0201
        val codec = Codec.of(
            ::E2EAuthResponse,
            E2EAuthResponse::ecdhPublicKey,
            E2EAuthResponse::ecdsaPublicKey,
            E2EAuthResponse::sign,
            E2EAuthResponse::iv,
            E2EAuthResponse::encrypted,
            Codec.field(1, Codec.bytesCodec()),
            Codec.field(2, Codec.bytesCodec()),
            Codec.field(3, Codec.bytesCodec()),
            Codec.field(4, Codec.bytesCodec()),
            Codec.field(5, Codec.bytesCodec())
        )
    }
}

class E2EEncrypted(
    val sign: ByteArray,
    val iv: ByteArray,
    val encrypted: ByteArray
) : CodecMessage {

    override fun messageId(): Int = messageId

    companion object {
        const val messageId = 0x0202
        val codec = Codec.of(
            ::E2EEncrypted,
            E2EEncrypted::sign,
            E2EEncrypted::iv,
            E2EEncrypted::encrypted,
            Codec.field(1, Codec.bytesCodec()),
            Codec.field(2, Codec.bytesCodec()),
            Codec.field(3, Codec.bytesCodec())
        )
    }
}

object E2EAuthCanceled : CodecMessage {
    override fun messageId(): Int = 0x0204

    val codec = Codec.of(this)
}