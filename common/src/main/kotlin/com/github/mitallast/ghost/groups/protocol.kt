package com.github.mitallast.ghost.groups

import com.github.mitallast.ghost.common.codec.Codec
import com.github.mitallast.ghost.common.codec.CodecMessage
import com.github.mitallast.ghost.files.Thumb
import com.github.mitallast.ghost.profile.UserProfile

class GroupJoin(
    val group: ByteArray,
    val title: String,
    val avatar: Thumb?,
    val members: List<UserProfile>,
    val secret: ByteArray
) : CodecMessage {
    override fun messageId(): Int = messageId

    companion object {
        const val messageId = 0x0700
        val codec = Codec.of(
            ::GroupJoin,
            GroupJoin::group,
            GroupJoin::title,
            GroupJoin::avatar,
            GroupJoin::members,
            GroupJoin::secret,
            Codec.field(1, Codec.bytesCodec()),
            Codec.field(2, Codec.stringCodec()),
            Codec.optional(3, Thumb.codec),
            Codec.field(4, Codec.listCodec(UserProfile.codec)),
            Codec.field(5, Codec.bytesCodec())
        )
    }
}

class GroupEncrypted(
    val group: ByteArray,
    val iv: ByteArray,
    val encrypted: ByteArray
) : CodecMessage {
    override fun messageId(): Int = messageId

    companion object {
        const val messageId = 0x0703
        val codec = Codec.of(
            ::GroupEncrypted,
            GroupEncrypted::group,
            GroupEncrypted::iv,
            GroupEncrypted::encrypted,
            Codec.field(1, Codec.bytesCodec()),
            Codec.field(2, Codec.bytesCodec()),
            Codec.field(3, Codec.bytesCodec())
        )
    }
}

class GroupJoined(
    val group: ByteArray,
    val member: UserProfile
) : CodecMessage {
    override fun messageId(): Int = messageId

    companion object {
        const val messageId = 0x0701
        val codec = Codec.of(
            ::GroupJoined,
            GroupJoined::group,
            GroupJoined::member,
            Codec.field(1, Codec.bytesCodec()),
            Codec.field(2, UserProfile.codec)
        )
    }
}

class GroupLeaved(
    val group: ByteArray,
    val member: ByteArray
) : CodecMessage {
    override fun messageId(): Int = messageId

    companion object {
        const val messageId = 0x0702
        val codec = Codec.of(
            ::GroupLeaved,
            GroupLeaved::group,
            GroupLeaved::member,
            Codec.field(1, Codec.bytesCodec()),
            Codec.field(2, Codec.bytesCodec())
        )
    }
}

