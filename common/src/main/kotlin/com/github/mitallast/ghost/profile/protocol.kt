package com.github.mitallast.ghost.profile

import com.github.mitallast.ghost.common.codec.Codec
import com.github.mitallast.ghost.common.codec.CodecMessage
import com.github.mitallast.ghost.files.Thumb

class UserProfile(
    val id: ByteArray,
    val fullname: String,
    val nickname: String,
    val avatar: Thumb?
) : CodecMessage {
    override fun messageId(): Int = messageId

    companion object {
        const val messageId = 0x0400
        val codec = Codec.of(
            ::UserProfile,
            UserProfile::id,
            UserProfile::fullname,
            UserProfile::nickname,
            UserProfile::avatar,
            Codec.field(1, Codec.bytesCodec()),
            Codec.field(2, Codec.stringCodec()),
            Codec.field(3, Codec.stringCodec()),
            Codec.optional(4, Thumb.codec)
        )
    }
}