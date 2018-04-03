package com.github.mitallast.ghost.client.dialogs

object DialogsFlow {
    suspend fun newContact(auth: ByteArray) {
        DialogsStore.add(auth)
        DialogsView.addDialog(auth)
    }

    suspend fun load() {
        val dialogs = DialogsStore.load()
        DialogsView.clear()
        dialogs.forEach { DialogsView.addDialog(it) }
    }
}