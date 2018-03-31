package com.github.mitallast.ghost

import com.github.mitallast.ghost.common.codec.Codec
import com.github.mitallast.ghost.dh.ECDHEncrypted
import com.github.mitallast.ghost.dh.ECDHRequest
import com.github.mitallast.ghost.dh.ECDHResponse
import com.github.mitallast.ghost.message.TextMessage


object Registry {
    fun register() {
        Codec.register(ECDHRequest.messageId, ECDHRequest.codec)
        Codec.register(ECDHResponse.messageId, ECDHResponse.codec)
        Codec.register(ECDHEncrypted.messageId, ECDHEncrypted.codec)

        Codec.register(TextMessage.messageId, TextMessage.codec)
    }
}