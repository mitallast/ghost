package com.github.mitallast.ghost

import com.github.mitallast.ghost.common.codec.Codec
import com.github.mitallast.ghost.client.ecdh.*
import com.github.mitallast.ghost.e2ee.*
import com.github.mitallast.ghost.message.TextMessage


object Registry {
    fun register() {
        Codec.register(ECDHRequest.messageId, ECDHRequest.codec)
        Codec.register(ECDHResponse.messageId, ECDHResponse.codec)
        Codec.register(ECDHEncrypted.messageId, ECDHEncrypted.codec)
        Codec.register(ECDHReconnect.messageId, ECDHReconnect.codec)

        Codec.register(E2ERequest.messageId, E2ERequest.codec)
        Codec.register(E2EResponse.messageId, E2EResponse.codec)
        Codec.register(E2EEncrypted.messageId, E2EEncrypted.codec)

        Codec.register(TextMessage.messageId, TextMessage.codec)
    }
}