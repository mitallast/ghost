package com.github.mitallast.ghost.profile

import com.github.mitallast.ghost.common.codec.Codec
import com.github.mitallast.ghost.common.codec.Message
import com.github.mitallast.ghost.files.Thumb

class UserProfile(
    val id: ByteArray,
    val fullname: String,
    val nickname: String,
    val avatar: Thumb?
) : Message {
    override fun messageId(): Int = messageId

    companion object {
        const val messageId = 0x0400
        val codec = Codec.of(
            ::UserProfile,
            UserProfile::id,
            UserProfile::fullname,
            UserProfile::nickname,
            UserProfile::avatar,
            Codec.bytesCodec(),
            Codec.stringCodec(),
            Codec.stringCodec(),
            Codec.optionCodec(Thumb.codec)
        )
    }
}