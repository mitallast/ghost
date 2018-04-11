package com.github.mitallast.ghost.rest.action

import com.github.mitallast.ghost.common.file.FileService
import com.github.mitallast.ghost.rest.RestController
import com.github.mitallast.ghost.rest.RestRequest
import com.google.inject.Inject
import io.netty.handler.codec.http.HttpHeaderNames
import io.netty.handler.codec.http.HttpHeaderValues
import io.netty.handler.codec.http.HttpMethod
import org.bouncycastle.util.encoders.Hex.toHexString
import java.io.File
import java.io.FileOutputStream
import java.security.SecureRandom

class UploadAction @Inject constructor(
    c: RestController,
    private val fileService: FileService
) {
    init {
        c.handle(this::upload, c.param().request(), text())
            .handle(HttpMethod.POST, "file/upload")

        c.handle(this::download, c.param().string("address"), file())
            .handle(HttpMethod.GET, "file/{address}")
    }

    private fun upload(c: RestRequest): String {
        val bytes = ByteArray(16)
        SecureRandom.getInstance("SHA1PRNG").nextBytes(bytes)
        val address = toHexString(bytes)
        val file = fileService.resource("upload", address)
        FileOutputStream(file).use { output ->
            c.content.readBytes(output, c.content.readableBytes())
        }
        return address
    }

    private fun download(address: String): File {
        return fileService.resource("upload", address)
    }

    private fun file(): (RestRequest, File) -> Unit {
        return { request, file ->
            request.response()
                .header(HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN, "*")
                .file(file)
        }
    }

    private fun text(): (RestRequest, String) -> Unit {
        return { request, response ->
            request.response()
                .header(HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN, "*")
                .text(response)
        }
    }
}