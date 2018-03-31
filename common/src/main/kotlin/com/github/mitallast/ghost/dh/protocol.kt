package com.github.mitallast.ghost.dh

import com.github.mitallast.ghost.common.codec.Message
import com.github.mitallast.ghost.common.codec.Codec

class ECDHRequest(
    val ECDHPublicKey: ByteArray,
    val ECDSAPublicKey: ByteArray,
    val sign: ByteArray
) : Message {

    override fun messageId(): Int = messageId

    companion object {
        const val messageId = 100
        val codec = Codec.of(
            ::ECDHRequest,
            ECDHRequest::ECDHPublicKey,
            ECDHRequest::ECDSAPublicKey,
            ECDHRequest::sign,
            Codec.bytesCodec(),
            Codec.bytesCodec(),
            Codec.bytesCodec()
        )
    }
}

class ECDHResponse(
    val ECDHPublicKey: ByteArray,
    val sign: ByteArray
) : Message {

    override fun messageId(): Int = messageId

    companion object {
        const val messageId = 101
        val codec = Codec.of(
            ::ECDHResponse,
            ECDHResponse::ECDHPublicKey,
            ECDHResponse::sign,
            Codec.bytesCodec(),
            Codec.bytesCodec()
        )
    }
}

class ECDHEncrypted(
    val sign: ByteArray,
    val iv: ByteArray,
    val encrypted: ByteArray
) : Message {

    override fun messageId(): Int = messageId

    companion object {
        const val messageId = 102
        val codec = Codec.of(
            ::ECDHEncrypted,
            ECDHEncrypted::sign,
            ECDHEncrypted::iv,
            ECDHEncrypted::encrypted,
            Codec.bytesCodec(),
            Codec.bytesCodec(),
            Codec.bytesCodec()
        )
    }
}