package com.github.mitallast.ghost.rest

import com.google.inject.AbstractModule
import com.github.mitallast.ghost.rest.action.ResourceHandler
import com.github.mitallast.ghost.rest.netty.HttpServer
import com.github.mitallast.ghost.rest.netty.HttpServerHandler
import com.github.mitallast.ghost.rest.netty.WebSocketFrameHandler

class RestModule : AbstractModule() {
    override fun configure() {
        bind(HttpServer::class.java).asEagerSingleton()
        bind(HttpServerHandler::class.java).asEagerSingleton()
        bind(WebSocketFrameHandler::class.java).asEagerSingleton()
        bind(RestController::class.java).asEagerSingleton()

        bind(ResourceHandler::class.java).asEagerSingleton()
    }
}
