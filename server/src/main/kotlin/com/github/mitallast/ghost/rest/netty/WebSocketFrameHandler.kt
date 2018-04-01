package com.github.mitallast.ghost.rest.netty

import com.github.mitallast.ghost.common.codec.Codec
import com.github.mitallast.ghost.common.codec.Message
import com.github.mitallast.ghost.client.ecdh.*
import com.github.mitallast.ghost.session.SessionContext
import com.github.mitallast.ghost.session.SessionService
import io.netty.buffer.Unpooled
import io.netty.channel.Channel
import io.netty.channel.ChannelHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame
import io.netty.handler.codec.http.websocketx.WebSocketFrame
import io.netty.util.AttributeKey
import org.apache.logging.log4j.LogManager
import javax.inject.Inject

@ChannelHandler.Sharable
class WebSocketFrameHandler @Inject constructor(
    private val ecdhService: ECDHService,
    private val sessionService: SessionService
) : SimpleChannelInboundHandler<WebSocketFrame>() {

    private val logger = LogManager.getLogger()
    private val authKey = AttributeKey.valueOf<Auth>("auth.key")

    @Throws(Exception::class)
    override fun channelRegistered(ctx: ChannelHandlerContext) {
        logger.info("channel registered: {}", ctx.channel())
        super.channelActive(ctx)
    }

    @Throws(Exception::class)
    override fun channelInactive(ctx: ChannelHandlerContext) {
        logger.info("channel inactive: {}", ctx.channel())
        val auth = ctx.channel().attr(authKey).getAndSet(null)
        if (auth != null) {
            sessionService.inactive(auth)
        }
        super.channelInactive(ctx)
    }

    @Throws(Exception::class)
    override fun channelRead0(ctx: ChannelHandlerContext, frame: WebSocketFrame) {
        when (frame) {
            is TextWebSocketFrame -> {
                val json = frame.text()
                logger.info("received {} {}", ctx.channel(), json)
            }
            is BinaryWebSocketFrame -> {
                val size = frame.content().readableBytes()
                val input = ByteArray(size)
                frame.content().readBytes(input)
                val message = Codec.anyCodec<Message>().read(input)
                logger.info("received {} {}", ctx.channel(), message)
                when (message) {
                    is ECDHRequest -> {
                        val (auth, response) = ecdhService.ecdh(message)
                        val out = Codec.anyCodec<ECDHResponse>().write(response)
                        ctx.channel().attr(authKey).set(auth)
                        ctx.writeAndFlush(BinaryWebSocketFrame(Unpooled.wrappedBuffer(out)), ctx.voidPromise())
                        val session = WebSocketSessionContext(auth, ctx.channel())
                        sessionService.registered(session)
                    }
                    is ECDHReconnect -> {
                        val auth = ctx.channel().attr(authKey).get()
                        if (auth == null) {
                            val reconnected = ecdhService.reconnect(message)
                            ctx.channel().attr(authKey).set(reconnected)
                            val session = WebSocketSessionContext(reconnected, ctx.channel())
                            sessionService.registered(session)
                        } else if (auth.auth.contentEquals(message.auth)) {
                            logger.warn("ignore reconnect message, same auth")
                        } else {
                            throw IllegalStateException("channel auth is different")
                        }
                    }
                    is ECDHEncrypted -> {
                        logger.info("decrypt echd message")
                        val auth = ctx.channel().attr(authKey).get()
                        if (auth == null) {
                            throw IllegalStateException("channel is not authorized")
                        } else if (!auth.auth.contentEquals(message.auth)) {
                            throw IllegalStateException("channel auth is different")
                        } else {
                            val decrypted = ecdhService.decrypt(auth, message)
                            logger.info("decrypted: {}", decrypted)
                            val decoded = Codec.anyCodec<Message>().read(decrypted)
                            logger.info("decoded: {}", decoded)
                            sessionService.handle(auth, decoded)
                        }
                    }
                    else ->
                        logger.warn("unexpected message: {}", message)
                }
            }
            else ->
                throw UnsupportedOperationException("unsupported frame type: " + frame.javaClass.simpleName)
        }
    }

    private inner class WebSocketSessionContext(
        private val auth: Auth,
        private val channel: Channel
    ) : SessionContext {
        override fun auth(): Auth = auth

        override fun send(message: Message) {
            val encoded = Codec.anyCodec<Message>().write(message)
            val encrypted = ecdhService.encrypt(auth, encoded)
            val data = Codec.anyCodec<Message>().write(encrypted)
            val buf = Unpooled.wrappedBuffer(data)
            val frame = BinaryWebSocketFrame(buf)
            channel.writeAndFlush(frame, channel.voidPromise())
        }
    }
}
