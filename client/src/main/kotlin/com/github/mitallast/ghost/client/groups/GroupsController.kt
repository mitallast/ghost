package com.github.mitallast.ghost.client.groups

import com.github.mitallast.ghost.client.common.await
import com.github.mitallast.ghost.client.common.toArrayBuffer
import com.github.mitallast.ghost.client.crypto.AES
import com.github.mitallast.ghost.client.crypto.HEX
import com.github.mitallast.ghost.client.profile.ProfileController
import com.github.mitallast.ghost.common.codec.CodecMessage
import com.github.mitallast.ghost.groups.GroupEncrypted
import com.github.mitallast.ghost.groups.GroupJoin

object GroupsController {
    private val dialogs = HashMap<String, GroupDialogController>()

    suspend fun start() {
        for (group in GroupStore.groups()) {
            start(group)
        }
    }

    fun start(group: Group): GroupDialogController {
        console.log("group started")
        val key = HEX.toHex(group.address)

        val self = ProfileController.profile()
        val controller = GroupDialogController(self, group)
        dialogs[key] = controller
        return controller
    }

    private fun dialog(id: ByteArray): GroupDialogController? {
        val key = HEX.toHex(id)
        return dialogs[key]
    }

    private suspend fun join(update: GroupJoin) {
        val key = HEX.toHex(update.group)
        if (!dialogs.containsKey(key)) {
            val secretKey = AES.importKey(toArrayBuffer(update.secret)).await()
            val group = Group(
                update.group,
                secretKey,
                update.title
            )
            GroupStore.create(group)
            for (member in update.members) {
                GroupStore.putMember(update.group, member)
            }
            start(group)
        }
    }

    suspend fun handle(from: ByteArray, update: CodecMessage) {
        when (update) {
            is GroupJoin -> join(update)
            is GroupEncrypted -> dialog(update.group)?.incoming(from, update)
            else -> console.warn("unexpected group message", update)
        }
    }
}