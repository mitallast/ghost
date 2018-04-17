package com.github.mitallast.ghost.rest.netty

import com.github.mitallast.ghost.common.file.FileService
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
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.MessageDigest
import java.security.SecureRandom

@ChannelHandler.Sharable
class HttpServerUploadHandler @Inject constructor(
    private val fileService: FileService
) : SimpleChannelInboundHandler<HttpObject>() {
    private val logger = LogManager.getLogger()
    private val uploadKey = AttributeKey.valueOf<FileUploadState?>("upload")

    private val xSHA1 = AsciiString.cached("x-sha1")

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
            val sha1Hex = request.headers().get(xSHA1)
            if (sha1Hex == null) {
                logger.error("x-sha1 required")
                ctx.channel().close()
                return
            }

            logger.info("upload file")

            val bytes = ByteArray(16)
            val random = SecureRandom.getInstance("SHA1PRNG")
            random.nextBytes(bytes)
            val address = Hex.toHexString(bytes)
            val file = fileService.resource("upload", address)

            val upload = FileUploadState(
                address,
                file,
                Hex.decode(sha1Hex)
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
                    stream.flush()
                }

                if (chunk is LastHttpContent) {
                    logger.info("chunk is last")
                    if (validate(upload.file, upload.sha1)) {
                        logger.info("file uploaded {} {} bytes", upload.address, upload.file.length())
                        ctx.channel().attr(uploadKey).set(null)
                        val response = DefaultFullHttpResponse(
                            HttpVersion.HTTP_1_1,
                            HttpResponseStatus.OK,
                            Unpooled.copiedBuffer(upload.address, CharsetUtil.UTF_8)
                        )
                        val write = ctx.channel().writeAndFlush(response)
                        write.addListener(ChannelFutureListener.CLOSE)
                    } else {
                        val response = DefaultFullHttpResponse(
                            HttpVersion.HTTP_1_1,
                            HttpResponseStatus.BAD_REQUEST,
                            Unpooled.copiedBuffer("sha1 not verified", CharsetUtil.UTF_8)
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

    private fun validate(file: File, expected: ByteArray): Boolean {
        val digest = MessageDigest.getInstance("SHA-1")
        FileInputStream(file).use { stream ->
            do {
                val buffer = ByteArray(4096)
                val read = stream.read(buffer)
                if (read > 0) {
                    digest.update(buffer, 0, read)
                }
            } while (read > 0)
        }
        val actual = digest.digest()!!
        val valid = actual.contentEquals(expected)

        if (!valid) {
            logger.error("sha1 not verified")
            logger.error("file size: {}", file.length())
            logger.error("expected: {}", Hex.toHexString(expected))
            logger.error("actual  : {}", Hex.toHexString(actual))
        }

        return valid
    }
}

private class FileUploadState(
    val address: String,
    val file: File,
    val sha1: ByteArray
)