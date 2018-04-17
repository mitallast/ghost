package com.github.mitallast.ghost.rest.netty

import io.netty.channel.ChannelInitializer
import io.netty.channel.socket.SocketChannel
import io.netty.handler.codec.http.HttpObjectAggregator
import io.netty.handler.codec.http.HttpServerCodec
import io.netty.handler.codec.http.cors.CorsConfigBuilder
import io.netty.handler.codec.http.cors.CorsHandler
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler
import io.netty.handler.stream.ChunkedWriteHandler
import io.netty.util.concurrent.DefaultEventExecutorGroup

class HttpServerInitializer(
    private val httpHandler: HttpServerHandler,
    private val uploadHandler: HttpServerUploadHandler,
    private val webSocketFrameHandler: WebSocketFrameHandler
) : ChannelInitializer<SocketChannel>() {

    private val group = DefaultEventExecutorGroup(1);
    private val corsConfig = CorsConfigBuilder
        .forAnyOrigin()
        .allowedRequestHeaders("x-sha1")
        .build()

    override fun initChannel(ch: SocketChannel) {
        val pipeline = ch.pipeline()
        pipeline.addLast(HttpServerCodec(4096, 8192, 8192, false))
        pipeline.addLast(uploadHandler)
        pipeline.addLast(HttpObjectAggregator(65536))
        pipeline.addLast(ChunkedWriteHandler())
        pipeline.addLast(CorsHandler(corsConfig))
        pipeline.addLast(WebSocketServerCompressionHandler())
        pipeline.addLast(WebSocketServerProtocolHandler("/ws/", null, true, 65536))
        pipeline.addLast(webSocketFrameHandler)
        pipeline.addLast(group, httpHandler)
    }
}