package com.github.mitallast.ghost.updates

import com.github.mitallast.ghost.common.codec.Codec
import com.github.mitallast.ghost.common.codec.CodecMessage

class Update(
    val sequence: Long,
    val from: ByteArray,
    val update: CodecMessage
) : CodecMessage {
    override fun messageId(): Int = messageId

    companion object {
        const val messageId = 0x0300
        val codec = Codec.of(
            ::Update,
            Update::sequence,
            Update::from,
            Update::update,
            Codec.field(1, Codec.longCodec()),
            Codec.field(2, Codec.bytesCodec()),
            Codec.field(3, Codec.anyCodec())
        )
    }
}

class InstallUpdate(val updates: List<Update>) : CodecMessage {
    override fun messageId(): Int = messageId

    companion object {
        const val messageId = 0x0301
        val codec = Codec.of(
            ::InstallUpdate,
            InstallUpdate::updates,
            Codec.field(1, Codec.listCodec(Update.codec))
        )
    }
}

class UpdateInstalled(val last: Long) : CodecMessage {
    override fun messageId(): Int = messageId

    companion object {
        const val messageId = 0x0302
        val codec = Codec.of(
            ::UpdateInstalled,
            UpdateInstalled::last,
            Codec.field(1, Codec.longCodec())
        )
    }
}

class UpdateRejected(val last: Long) : CodecMessage {
    override fun messageId(): Int = messageId

    companion object {
        const val messageId = 0x0303
        val codec = Codec.of(
            ::UpdateRejected,
            UpdateRejected::last,
            Codec.field(1, Codec.longCodec())
        )
    }
}

class SendUpdate(
    val randomId: Long,
    val address: ByteArray,
    val message: CodecMessage
) : CodecMessage {
    override fun messageId(): Int = messageId

    companion object {
        const val messageId = 0x0304
        val codec = Codec.of(
            ::SendUpdate,
            SendUpdate::randomId,
            SendUpdate::address,
            SendUpdate::message,
            Codec.field(1, Codec.longCodec()),
            Codec.field(2, Codec.bytesCodec()),
            Codec.field(3, Codec.anyCodec())
        )
    }
}

class SendAck(val randomId: Long) : CodecMessage {
    override fun messageId(): Int = messageId

    companion object {
        const val messageId = 0x0305
        val codec = Codec.of(
            ::SendAck,
            SendAck::randomId,
            Codec.field(1, Codec.longCodec())
        )
    }
}