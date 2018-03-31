package com.github.mitallast.ghost.rest.netty

import com.github.mitallast.ghost.common.codec.Codec
import com.github.mitallast.ghost.common.codec.Message
import com.github.mitallast.ghost.dh.*
import com.github.mitallast.ghost.message.TextMessage
import io.netty.buffer.ByteBufInputStream
import io.netty.buffer.ByteBufOutputStream
import io.netty.channel.*
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame
import io.netty.handler.codec.http.websocketx.WebSocketFrame
import io.netty.util.AttributeKey
import io.vavr.collection.HashMap
import io.vavr.collection.Map
import org.apache.logging.log4j.LogManager
import java.io.ByteArrayInputStream
import javax.inject.Inject
import kotlin.math.log

@ChannelHandler.Sharable
class WebSocketFrameHandler @Inject constructor(private val ecdhService: ECDHService) :
    SimpleChannelInboundHandler<WebSocketFrame>() {

    private val logger = LogManager.getLogger()
    private val ecdhKey = AttributeKey.valueOf<ECDHFlow>("ecdh")

    @Volatile
    private var channels: Map<ChannelId, Channel> = HashMap.empty()

    @Throws(Exception::class)
    override fun channelRegistered(ctx: ChannelHandlerContext) {
        logger.info("channel registered: {}", ctx.channel())
        synchronized(this) {
            channels = channels.put(ctx.channel().id(), ctx.channel())
        }
        super.channelActive(ctx)
    }

    @Throws(Exception::class)
    override fun channelInactive(ctx: ChannelHandlerContext) {
        logger.info("channel inactive: {}", ctx.channel())
        synchronized(this) {
            channels = channels.remove(ctx.channel().id())
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
                val input = ByteBufInputStream(frame.content())
                val message = Codec.anyCodec<Message>().read(input)
                input.close()
                logger.info("received {} {}", ctx.channel(), message)
                when (message) {
                    is ECDHRequest -> {
                        val ecdh = ecdhService.ecdh()
                        ctx.channel().attr(ecdhKey).set(ecdh)
                        val response = ecdh.keyAgreement(message)
                        val out = ctx.alloc().buffer()
                        val output = ByteBufOutputStream(out)
                        Codec.anyCodec<ECDHResponse>().write(output, response)
                        output.close()
                        ctx.writeAndFlush(BinaryWebSocketFrame(out), ctx.voidPromise())
                    }
                    is ECDHEncrypted -> {
                        val ecdh = ctx.channel().attr(ecdhKey).get()
                        val decrypted = ecdh.decrypt(message)
                        val decryptedInput = ByteArrayInputStream(decrypted)
                        val decryptedMessage = Codec.anyCodec<Message>().read(decryptedInput)
                        logger.info("decrypted: {}", decryptedMessage)
                        when(decryptedMessage) {
                            is TextMessage -> {
                                logger.info("text message: {}", decryptedMessage.text)
                            }
                        }
                    }
                }
            }
            else ->
                throw UnsupportedOperationException("unsupported frame type: " + frame.javaClass.simpleName)
        }
    }
}