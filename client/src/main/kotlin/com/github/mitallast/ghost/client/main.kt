package com.github.mitallast.ghost.client

import com.github.mitallast.ghost.Registry
import com.github.mitallast.ghost.client.common.await
import com.github.mitallast.ghost.client.common.launch
import com.github.mitallast.ghost.client.common.toByteArray
import com.github.mitallast.ghost.client.crypto.*
import com.github.mitallast.ghost.client.e2e.E2EDHFlow
import com.github.mitallast.ghost.client.ecdh.ConnectionService
import com.github.mitallast.ghost.message.TextMessage

fun main(args: Array<String>) {
    Registry.register()

    launch {
        val e2e = E2EDHFlow.connect(toByteArray(HEX.parseHex("9e175225b7ce4cde94730267456520b1").buffer)).await()
        ConnectionService.send(e2e.auth, TextMessage("Hello!"))
    }
}