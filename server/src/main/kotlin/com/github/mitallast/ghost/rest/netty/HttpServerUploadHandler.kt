package com.github.mitallast.ghost.rest.netty

import com.github.mitallast.ghost.common.crypto.ECDH
import com.github.mitallast.ghost.common.file.FileService
import com.github.mitallast.ghost.ecdh.ECDHService
import com.google.inject.Inject
import io.netty.buffer.Unpooled
import io.netty.channel.ChannelFutureListener
import io.netty.channel.ChannelHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.handler.codec.http.*
import io.netty.handler.codec.http.HttpRequest
import io.netty.util.AsciiString
import io.netty.util.AttributeKey
import io.netty.util.CharsetUtil
import org.apache.logging.log4j.LogManager
import org.bouncycastle.util.encoders.Hex
import java.io.File
import java.io.FileOutputStream
import java.security.SecureRandom

@ChannelHandler.Sharable
class HttpServerUploadHandler @Inject constructor(
    private val fileService: FileService,
    private val ecdhService: ECDHService
) : SimpleChannelInboundHandler<HttpObject>() {
    private val logger = LogManager.getLogger()
    private val uploadKey = AttributeKey.valueOf<FileUploadState?>("upload")

    private val xAddress = AsciiString.cached("x-address")
    private val xSign = AsciiString.cached("x-sign")
    private val xIV = AsciiString.cached("x-iv")

    override fun channelInactive(ctx: ChannelHandlerContext) {
        val upload = ctx.channel().attr(uploadKey).get()
        if (upload != null) {
            logger.warn("unfinished upload, cleanup")
            if (upload.file.exists()) {
                upload.file.delete()
            }
        }
        super.channelInactive(ctx)
    }

    override fun channelRead0(ctx: ChannelHandlerContext, msg: HttpObject) {
        if (msg is HttpRequest && msg.method() == HttpMethod.POST && msg.uri() == "/file/upload") {
            val prev = ctx.channel().attr(uploadKey).get()
            if (prev != null) {
                logger.error("previous upload does not finished")
                ctx.channel().close()
                return
            }

            val request: HttpRequest = msg

            val addressHex = request.headers().get(xAddress)
            val signHex = request.headers().get(xSign)
            val ivHex = request.headers().get(xIV)
            if (addressHex == null || signHex == null || ivHex == null) {
                logger.error("x-address, sign, iv required")
                ctx.channel().close()
                return
            }

            logger.info("upload file auth={}", addressHex)

            val bytes = ByteArray(16)
            val random = SecureRandom.getInstance("SHA1PRNG")
            random.nextBytes(bytes)
            val address = Hex.toHexString(bytes)
            val file = fileService.resource("upload", address)

            val upload = FileUploadState(
                address,
                file,
                Hex.decode(addressHex),
                Hex.decode(signHex),
                Hex.decode(ivHex)
            )
            ctx.channel().attr(uploadKey).set(upload)
            return
        }

        val upload = ctx.channel().attr(uploadKey).get()
        if (upload != null) {
            if (msg is HttpContent) {
                val chunk: HttpContent = msg
                logger.info("chunk: {}", chunk.content().readableBytes())

                FileOutputStream(upload.file, true).use { stream ->
                    chunk.content().readBytes(stream, chunk.content().readableBytes())
                }

                if (chunk is LastHttpContent) {
                    logger.info("chunk is last")

                    if (!ecdhService.validateFile(upload.auth, upload.sign, upload.iv, upload.file)) {
                        val response = DefaultFullHttpResponse(
                            HttpVersion.HTTP_1_1,
                            HttpResponseStatus.OK,
                            Unpooled.copiedBuffer(upload.address, CharsetUtil.UTF_8)
                        )
                        val write = ctx.channel().writeAndFlush(response)
                        write.addListener(ChannelFutureListener.CLOSE)
                    } else {
                        ctx.channel().attr(uploadKey).set(null)
                        val response = DefaultFullHttpResponse(
                            HttpVersion.HTTP_1_1,
                            HttpResponseStatus.BAD_REQUEST,
                            Unpooled.copiedBuffer("Sign not verified", CharsetUtil.UTF_8)
                        )
                        val write = ctx.channel().writeAndFlush(response)
                        write.addListener(ChannelFutureListener.CLOSE)
                    }
                }
            }
        } else {
            ctx.fireChannelRead(msg)
        }
    }
}

private class FileUploadState(
    val address: String,
    val file: File,
    val auth: ByteArray,
    val iv: ByteArray,
    val sign: ByteArray
)