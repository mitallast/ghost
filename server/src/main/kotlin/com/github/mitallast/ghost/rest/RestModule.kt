package com.github.mitallast.ghost.rest

import com.google.inject.AbstractModule
import com.github.mitallast.ghost.rest.action.ResourceHandler
import com.github.mitallast.ghost.rest.action.UploadAction
import com.github.mitallast.ghost.rest.netty.HttpServer
import com.github.mitallast.ghost.rest.netty.HttpServerHandler
import com.github.mitallast.ghost.rest.netty.HttpServerUploadHandler
import com.github.mitallast.ghost.rest.netty.WebSocketFrameHandler

class RestModule : AbstractModule() {
    override fun configure() {
        binder().disableCircularProxies()
        binder().requireExplicitBindings()

        bind(HttpServer::class.java).asEagerSingleton()
        bind(HttpServerHandler::class.java).asEagerSingleton()
        bind(WebSocketFrameHandler::class.java).asEagerSingleton()
        bind(HttpServerUploadHandler::class.java).asEagerSingleton()
        bind(RestController::class.java).asEagerSingleton()

        bind(ResourceHandler::class.java).asEagerSingleton()
        bind(UploadAction::class.java).asEagerSingleton()
    }
}
