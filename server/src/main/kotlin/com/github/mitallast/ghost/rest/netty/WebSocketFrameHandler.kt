package com.github.mitallast.ghost.rest.netty

import com.github.mitallast.ghost.ecdh.ECDHEncrypted
import com.github.mitallast.ghost.ecdh.ECDHReconnect
import com.github.mitallast.ghost.ecdh.ECDHRequest
import com.github.mitallast.ghost.common.actor.Actor
import com.github.mitallast.ghost.common.actor.ActorRef
import com.github.mitallast.ghost.common.actor.ActorSystem
import com.github.mitallast.ghost.common.codec.Codec
import com.github.mitallast.ghost.common.codec.Message
import com.github.mitallast.ghost.e2e.E2EEncrypted
import com.github.mitallast.ghost.e2e.E2ERequest
import com.github.mitallast.ghost.e2e.E2EResponse
import com.github.mitallast.ghost.ecdh.Auth
import com.github.mitallast.ghost.ecdh.ECDHService
import com.github.mitallast.ghost.session.SessionInactive
import com.github.mitallast.ghost.session.SessionRegistered
import com.github.mitallast.ghost.updates.*
import com.google.inject.name.Named
import io.netty.buffer.Unpooled
import io.netty.channel.Channel
import io.netty.channel.ChannelHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame
import io.netty.util.AttributeKey
import org.apache.logging.log4j.LogManager
import org.bouncycastle.util.encoders.Hex
import javax.inject.Inject

class SendMessage(val message: Message)
object ChannelInactive
object CloseChannel

@ChannelHandler.Sharable
class WebSocketFrameHandler @Inject constructor(
    private val system: ActorSystem,
    private val ecdhService: ECDHService,
    @Named("session") private val session: ActorRef,
    @Named("updates") private val updates: ActorRef
) : SimpleChannelInboundHandler<BinaryWebSocketFrame>() {

    private val logger = LogManager.getLogger()
    private val actorKey = AttributeKey.valueOf<ActorRef>("actor")

    @Throws(Exception::class)
    override fun channelRegistered(ctx: ChannelHandlerContext) {
        logger.info("channel registered: {}", ctx.channel())
        val ref = WebSocketActor(ctx.channel()).get()
        ctx.channel().attr(actorKey).set(ref)
        super.channelActive(ctx)
    }

    @Throws(Exception::class)
    override fun channelInactive(ctx: ChannelHandlerContext) {
        logger.info("channel inactive: {}", ctx.channel())
        val ref = ctx.channel().attr(actorKey).getAndSet(null)
        ref.send(ChannelInactive)
        super.channelInactive(ctx)
    }

    @Throws(Exception::class)
    override fun channelRead0(ctx: ChannelHandlerContext, frame: BinaryWebSocketFrame) {
        val size = frame.content().readableBytes()
        val input = ByteArray(size)
        frame.content().readBytes(input)
        val message = Codec.anyCodec<Message>().read(input)
        logger.info("received {} {}", ctx.channel(), message)
        ctx.channel().attr(actorKey).get().send(message)
    }

    private inner class WebSocketActor(private val channel: Channel) : Actor(system) {
        private var auth: Auth? = null

        override fun handle(message: Any, sender: ActorRef) {
            when (message) {
                is ChannelInactive -> inactive()
                is CloseChannel -> {
                    auth = null
                    channel.close()
                }
                is Auth -> {
                    logger.info("authorized {}", Hex.toHexString(message.auth))
                    this.auth = message
                    session.send(SessionRegistered(message.auth), self)
                    updates.send(ForceUpdate(message.auth))
                }
                is ECDHRequest -> {
                    inactive()
                    try {
                        logger.info("ecdh request")
                        val (auth, response) = ecdhService.ecdh(message)
                        self.send(auth)
                        send(response)
                    } catch (e: Exception) {
                        logger.error(e)
                    }
                }
                is ECDHReconnect -> {
                    inactive()
                    try {
                        logger.info("ecdh reconnect")
                        val auth = ecdhService.reconnect(message)
                        self.send(auth)
                    } catch (e: Exception) {
                        logger.error(e)
                    }
                }
                is ECDHEncrypted -> {
                    if (auth != null) {
                        try {
                            logger.info("ecdh encrypted")
                            val decrypted = ecdhService.decrypt(auth!!, message)
                            val decoded = Codec.anyCodec<Message>().read(decrypted)
                            when (decoded) {
                                is E2ERequest -> updates.send(SendUpdate(decoded.to, decoded))
                                is E2EResponse -> updates.send(SendUpdate(decoded.to, decoded))
                                is E2EEncrypted -> updates.send(SendUpdate(decoded.to, decoded))
                                is UpdateInstalled -> updates.send(AuthUpdateInstalled(auth!!.auth, decoded.last))
                                is UpdateRejected -> updates.send(AuthUpdateRejected(auth!!.auth, decoded.last))
                            }
                        } catch (e: Exception) {
                            logger.error(e)
                        }
                    } else {
                        logger.error("not authorized, but received encrypted message")
                    }
                }
                is SendMessage -> {
                    if (auth != null) {
                        try {
                            logger.info("send encrypted {}", message.message)
                            val encoded = Codec.anyCodec<Message>().write(message.message)
                            val encrypted = ecdhService.encrypt(auth!!, encoded)
                            send(encrypted)
                        } catch (e: Exception) {
                            logger.error(e)
                        }
                    } else {
                        logger.error("not authorized, received send message")
                    }
                }
            }
        }

        private fun inactive() {
            if (auth != null) {
                session.send(SessionInactive(auth!!.auth))
            }
            auth = null
        }

        private fun send(message: Message) {
            val out = Codec.anyCodec<Message>().write(message)
            val frame = BinaryWebSocketFrame(Unpooled.wrappedBuffer(out))
            channel.writeAndFlush(frame, channel.voidPromise())
        }
    }
}
