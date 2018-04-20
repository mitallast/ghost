package com.github.mitallast.ghost.client.e2e

import com.github.mitallast.ghost.client.common.launch
import com.github.mitallast.ghost.client.common.toArrayBuffer
import com.github.mitallast.ghost.client.common.toByteArray
import com.github.mitallast.ghost.client.crypto.HEX
import com.github.mitallast.ghost.client.groups.GroupsController
import com.github.mitallast.ghost.client.html.div
import com.github.mitallast.ghost.client.profile.ProfileController
import com.github.mitallast.ghost.client.profile.ProfilesController
import com.github.mitallast.ghost.client.updates.UpdatesController
import com.github.mitallast.ghost.client.view.*
import com.github.mitallast.ghost.common.codec.Codec
import com.github.mitallast.ghost.common.codec.CodecMessage
import com.github.mitallast.ghost.e2e.E2EAuthCanceled
import com.github.mitallast.ghost.e2e.E2EAuthRequest
import com.github.mitallast.ghost.e2e.E2EAuthResponse
import com.github.mitallast.ghost.e2e.E2EEncrypted
import com.github.mitallast.ghost.groups.GroupJoin
import com.github.mitallast.ghost.groups.GroupLeaved
import com.github.mitallast.ghost.message.Message
import com.github.mitallast.ghost.profile.UserProfile

object E2EController {
    suspend fun send(to: ByteArray, message: CodecMessage) {
        console.log("send e2e", HEX.toHex(to), message)
        val encoded = Codec.anyCodec<CodecMessage>().write(message)
        console.log("encrypt e2e")
        val encrypted = E2EDHFlow.encrypt(to, toArrayBuffer(encoded))
        console.log("send e2e encrypted", encrypted)
        UpdatesController.send(to, encrypted)
    }

    suspend fun answer(to: ByteArray, password: String) {
        val profile = ProfileController.profile()
        val encoded = UserProfile.codec.write(profile)
        val response = E2EDHFlow.answerRequest(to, password, toArrayBuffer(encoded))
        UpdatesController.send(to, response)
    }

    fun cancel(to: ByteArray) {
        launch {
            console.log("e2e cancel auth transaction")
            E2EDHFlow.cancelRequest(to)
            UpdatesController.send(to, E2EAuthCanceled)
            viewCanceled(to)
        }
    }

    private fun viewCanceled(address: ByteArray) {
        val view = E2EAuthCanceledView(address)
        ContentHeaderController.title("Auth canceled: " + HEX.toHex(address))
        ContentMainController.view(view)
        ContentFooterController.hide()
    }

    suspend fun handle(from: ByteArray, update: CodecMessage) {
        when (update) {
            is E2EAuthRequest -> IncomingRequestController.handle(from, update)
            is E2EAuthResponse -> ResponseController.handle(from, update)
            is E2EAuthCanceled -> {
                console.log("e2e auth transaction canceled")
                E2EDHFlow.cancelRequest(from)
                viewCanceled(from)
            }
            is E2EEncrypted -> {
                console.log("e2e encrypted received")
                val decrypted = E2EDHFlow.decrypt(from, update)
                val decoded = Codec.anyCodec<CodecMessage>().read(toByteArray(decrypted))
                console.log("e2e received", decoded)
                when (decoded) {
                    is UserProfile -> ProfilesController.update(decoded)
                    is Message -> ProfilesController.dialog(from)?.incoming(decoded)
                    is GroupJoin -> GroupsController.handle(from, decoded)
                    else -> console.warn("unexpected e2e message", decoded)
                }
            }
            is GroupJoin -> GroupsController.handle(from, update)
        }
    }
}

class E2EAuthCanceledView(address: ByteArray) : View {
    override val root = div {
        clazz("form-container")
        div {
            clazz("form-success")
            text("E2E Authorization canceled")
        }
        div {
            clazz("form-hint")
            text(HEX.toHex(address))
        }
    }
}