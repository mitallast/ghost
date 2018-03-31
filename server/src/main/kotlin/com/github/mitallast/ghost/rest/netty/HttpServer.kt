package com.github.mitallast.ghost.rest.netty

import com.google.inject.Inject
import com.typesafe.config.Config
import io.netty.channel.ChannelInitializer
import io.netty.channel.socket.SocketChannel
import com.github.mitallast.ghost.common.netty.NettyProvider
import com.github.mitallast.ghost.common.netty.NettyServer

class HttpServer @Inject constructor(
    config: Config,
    provider: NettyProvider,
    private val serverHandler: HttpServerHandler,
    private val webSocketFrameHandler: WebSocketFrameHandler
) :
    NettyServer(config, provider, config.getString("rest.host"), config.getInt("rest.port")) {

    override fun channelInitializer(): ChannelInitializer<SocketChannel> {
        return HttpServerInitializer(serverHandler, webSocketFrameHandler)
    }
}
