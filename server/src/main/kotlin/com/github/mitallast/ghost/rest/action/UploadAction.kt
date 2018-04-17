package com.github.mitallast.ghost.rest.action

import com.github.mitallast.ghost.common.file.FileService
import com.github.mitallast.ghost.rest.RestController
import com.google.inject.Inject
import io.netty.handler.codec.http.HttpMethod
import org.apache.logging.log4j.LogManager
import java.io.File

class UploadAction @Inject constructor(
    c: RestController,
    private val fileService: FileService
) {
    private val logger = LogManager.getLogger()

    init {
        c.handle(this::download, c.param().string("address"), c.response().file())
            .handle(HttpMethod.GET, "file/{address}")
    }

    private fun download(address: String): File {
        logger.info("download {}", address)
        return fileService.resource("upload", address)
    }
}