package com.github.mitallast.ghost.client.e2e

import com.github.mitallast.ghost.client.common.toArrayBuffer
import com.github.mitallast.ghost.client.common.toByteArray
import com.github.mitallast.ghost.client.crypto.HEX
import com.github.mitallast.ghost.client.ecdh.ECDHController
import com.github.mitallast.ghost.client.messages.MessagesController
import com.github.mitallast.ghost.client.profile.ProfileController
import com.github.mitallast.ghost.client.updates.UpdatesController
import com.github.mitallast.ghost.common.codec.Codec
import com.github.mitallast.ghost.common.codec.CodecMessage
import com.github.mitallast.ghost.e2e.E2EEncrypted
import com.github.mitallast.ghost.e2e.E2EAuthRequest
import com.github.mitallast.ghost.e2e.E2EAuthResponse
import com.github.mitallast.ghost.e2e.E2EComplete
import com.github.mitallast.ghost.message.Message
import com.github.mitallast.ghost.profile.UserProfile

object E2EController {
    suspend fun send(to: ByteArray, message: CodecMessage) {
        console.log("send e2e", HEX.toHex(to), message)
        val encoded = Codec.anyCodec<CodecMessage>().write(message)
        console.log("get address for e2e encryption")
        val auth = ECDHController.auth()
        console.log("encrypt e2e")
        val encrypted = E2EDHFlow.encrypt(auth, to, toArrayBuffer(encoded))
        console.log("send e2e encrypted", encrypted)
        UpdatesController.send(to, encrypted)
    }

    suspend fun complete(to: ByteArray) {
        val profile = ProfileController.profile()
        val encoded = UserProfile.codec.write(profile)
        console.log("get address for e2e encryption")
        val auth = ECDHController.auth()
        console.log("encrypt e2e")
        val encrypted = E2EDHFlow.encrypt(auth, to, toArrayBuffer(encoded))
        console.log("send e2e complete", encrypted)
        UpdatesController.send(to, encrypted.toComplete())
    }

    suspend fun handle(update: CodecMessage) {
        when (update) {
            is E2EAuthRequest -> IncomingRequestController.handle(update)
            is E2EAuthResponse -> ResponseController.handle(update)
            is E2EComplete -> {
                console.log("e2e complete received")
                val self = ProfileController.profile()
                send(update.from, self)
                val decrypted = E2EDHFlow.decrypt(update.toEncrypted())
                val profile = UserProfile.codec.read(toByteArray(decrypted))
                console.log("e2e complete profile", profile)
                ProfileController.updateProfile(profile)
            }
            is E2EEncrypted -> {
                console.log("e2e encrypted received")
                val decrypted = E2EDHFlow.decrypt(update)
                val decoded = Codec.anyCodec<CodecMessage>().read(toByteArray(decrypted))
                console.log("e2e received", decoded)
                when (decoded) {
                    is UserProfile -> ProfileController.updateProfile(decoded)
                    is Message -> MessagesController.handle(update.from, decoded)
                }
            }
        }
    }
}