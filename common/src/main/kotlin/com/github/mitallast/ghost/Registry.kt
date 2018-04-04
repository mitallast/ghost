package com.github.mitallast.ghost

import com.github.mitallast.ghost.common.codec.Codec
import com.github.mitallast.ghost.ecdh.*
import com.github.mitallast.ghost.e2e.*
import com.github.mitallast.ghost.files.Thumb
import com.github.mitallast.ghost.updates.*
import com.github.mitallast.ghost.message.TextMessage
import com.github.mitallast.ghost.profile.UserProfile


object Registry {
    object mask {
        val ecdh = 0x0100
        val e2e = 0x0200
        val update = 0x0300
        val profile = 0x0400
        val files = 0x0500
        val message = 0xFF00
    }

    fun register() {
        Codec.register(ECDHRequest.messageId, ECDHRequest.codec)
        Codec.register(ECDHResponse.messageId, ECDHResponse.codec)
        Codec.register(ECDHEncrypted.messageId, ECDHEncrypted.codec)
        Codec.register(ECDHReconnect.messageId, ECDHReconnect.codec)

        Codec.register(E2ERequest.messageId, E2ERequest.codec)
        Codec.register(E2EResponse.messageId, E2EResponse.codec)
        Codec.register(E2EEncrypted.messageId, E2EEncrypted.codec)

        Codec.register(Update.messageId, Update.codec)
        Codec.register(InstallUpdate.messageId, InstallUpdate.codec)
        Codec.register(UpdateInstalled.messageId, UpdateInstalled.codec)
        Codec.register(UpdateRejected.messageId, UpdateRejected.codec)

        Codec.register(UserProfile.messageId, UserProfile.codec)

        Codec.register(Thumb.messageId, Thumb.codec)

        Codec.register(TextMessage.messageId, TextMessage.codec)
    }
}