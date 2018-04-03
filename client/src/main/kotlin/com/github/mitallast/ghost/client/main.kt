package com.github.mitallast.ghost.client

import com.github.mitallast.ghost.Registry
import com.github.mitallast.ghost.client.common.launch
import com.github.mitallast.ghost.client.connection.ConnectionService
import com.github.mitallast.ghost.client.dialogs.DialogsFlow

fun main(args: Array<String>) {
    Registry.register()

    ConnectionService

    launch {
        DialogsFlow.load()
    }
}