package com.github.mitallast.ghost.client

import com.github.mitallast.ghost.Registry
import com.github.mitallast.ghost.client.view.ApplicationController

fun main(args: Array<String>) {
    Registry.register()
    ApplicationController.start()
}