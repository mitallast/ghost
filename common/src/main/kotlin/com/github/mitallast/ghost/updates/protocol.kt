package com.github.mitallast.ghost.updates

import com.github.mitallast.ghost.common.codec.Codec
import com.github.mitallast.ghost.common.codec.Message

class Update(val sequence: Long, val update: Message) : Message {
    override fun messageId(): Int = messageId

    companion object {
        const val messageId = 0x0300
        val codec = Codec.of(
            ::Update,
            Update::sequence,
            Update::update,
            Codec.longCodec(),
            Codec.anyCodec()
        )
    }
}

class InstallUpdate(val updates: List<Update>) : Message {
    override fun messageId(): Int = messageId

    companion object {
        const val messageId = 0x0301
        val codec = Codec.of(
            ::InstallUpdate,
            InstallUpdate::updates,
            Codec.listCodec(Update.codec)
        )
    }
}

class UpdateInstalled(val last: Long) : Message {
    override fun messageId(): Int = messageId

    companion object {
        const val messageId = 0x0302
        val codec = Codec.of(
            ::UpdateInstalled,
            UpdateInstalled::last,
            Codec.longCodec()
        )
    }
}

class UpdateRejected(val last: Long) : Message {
    override fun messageId(): Int = messageId

    companion object {
        const val messageId = 0x0303
        val codec = Codec.of(
            ::UpdateRejected,
            UpdateRejected::last,
            Codec.longCodec()
        )
    }
}