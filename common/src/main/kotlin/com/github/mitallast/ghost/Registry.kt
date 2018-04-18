package com.github.mitallast.ghost

import com.github.mitallast.ghost.common.codec.Codec
import com.github.mitallast.ghost.ecdh.*
import com.github.mitallast.ghost.e2e.*
import com.github.mitallast.ghost.files.*
import com.github.mitallast.ghost.updates.*
import com.github.mitallast.ghost.message.*
import com.github.mitallast.ghost.profile.UserProfile

object Registry {
    fun register() {
        Codec.register(ECDHRequest.messageId, ECDHRequest.codec)
        Codec.register(ECDHResponse.messageId, ECDHResponse.codec)
        Codec.register(ECDHEncrypted.messageId, ECDHEncrypted.codec)
        Codec.register(ECDHReconnect.messageId, ECDHReconnect.codec)

        Codec.register(E2EAuthRequest.messageId, E2EAuthRequest.codec)
        Codec.register(E2EAuthResponse.messageId, E2EAuthResponse.codec)
        Codec.register(E2EEncrypted.messageId, E2EEncrypted.codec)
        Codec.register(E2EAuthCanceled.messageId(), E2EAuthCanceled.codec)

        Codec.register(Update.messageId, Update.codec)
        Codec.register(InstallUpdate.messageId, InstallUpdate.codec)
        Codec.register(UpdateInstalled.messageId, UpdateInstalled.codec)
        Codec.register(UpdateRejected.messageId, UpdateRejected.codec)
        Codec.register(SendUpdate.messageId, SendUpdate.codec)
        Codec.register(SendAck.messageId, SendAck.codec)

        Codec.register(UserProfile.messageId, UserProfile.codec)

        Codec.register(Thumb.messageId, Thumb.codec)
        Codec.register(EncryptedFile.messageId, EncryptedFile.codec)

        Codec.register(Message.messageId, Message.codec)
        Codec.register(TextMessage.messageId, TextMessage.codec)
        Codec.register(ServiceMessage.messageId, ServiceMessage.codec)
        Codec.register(FileMessage.messageId, FileMessage.codec)
    }
}