package com.github.mitallast.ghost.rest

import io.netty.buffer.ByteBuf
import io.netty.handler.codec.http.HttpMethod
import io.vavr.collection.Map

interface RestRequest {
    val httpMethod: HttpMethod

    val queryPath: String

    val uri: String

    val paramMap: Map<String, String>

    val content: ByteBuf

    fun param(param: String): String

    fun hasParam(param: String): Boolean

    fun response(): ResponseBuilder
}
