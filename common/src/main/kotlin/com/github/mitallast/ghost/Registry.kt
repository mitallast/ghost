package com.github.mitallast.ghost

import com.github.mitallast.ghost.common.codec.Codec
import com.github.mitallast.ghost.ecdh.*
import com.github.mitallast.ghost.e2e.*
import com.github.mitallast.ghost.files.*
import com.github.mitallast.ghost.updates.*
import com.github.mitallast.ghost.message.*
import com.github.mitallast.ghost.profile.UserProfile


object Registry {
    object mask {
        const val ecdh = 0x0100
        const val e2e = 0x0200
        const val update = 0x0300
        const val profile = 0x0400
        const val files = 0x0500
        const val message = 0x0600
    }

    fun register() {
        Codec.register(ECDHRequest.messageId, ECDHRequest.codec)
        Codec.register(ECDHResponse.messageId, ECDHResponse.codec)
        Codec.register(ECDHEncrypted.messageId, ECDHEncrypted.codec)
        Codec.register(ECDHReconnect.messageId, ECDHReconnect.codec)

        Codec.register(E2EAuthRequest.messageId, E2EAuthRequest.codec)
        Codec.register(E2EAuthResponse.messageId, E2EAuthResponse.codec)
        Codec.register(E2EComplete.messageId, E2EComplete.codec)
        Codec.register(E2EEncrypted.messageId, E2EEncrypted.codec)

        Codec.register(Update.messageId, Update.codec)
        Codec.register(InstallUpdate.messageId, InstallUpdate.codec)
        Codec.register(UpdateInstalled.messageId, UpdateInstalled.codec)
        Codec.register(UpdateRejected.messageId, UpdateRejected.codec)
        Codec.register(SendUpdate.messageId, SendUpdate.codec)
        Codec.register(SendAck.messageId, SendAck.codec)

        Codec.register(UserProfile.messageId, UserProfile.codec)

        Codec.register(Thumb.messageId, Thumb.codec)

        Codec.register(Message.messageId, Message.codec)
        Codec.register(TextMessage.messageId, TextMessage.codec)
        Codec.register(ServiceMessage.messageId, ServiceMessage.codec)
        Codec.register(EncryptedFileMessage.messageId, EncryptedFileMessage.codec)
    }
}