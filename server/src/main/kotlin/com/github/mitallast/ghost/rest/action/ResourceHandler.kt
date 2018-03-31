package com.github.mitallast.ghost.rest.action

import com.github.mitallast.ghost.rest.RestController
import com.google.common.reflect.ClassPath
import com.google.inject.Inject
import io.netty.handler.codec.http.HttpMethod
import java.net.URL

class ResourceHandler @Inject constructor(c: RestController) {

    init {
        val classPath = ClassPath.from(ResourceHandler::class.java.classLoader)
        val resources = classPath.resources

        resources
            .stream()
            .filter { resource -> resource.resourceName.startsWith("META-INF/resources/webjars/") }
            .forEach { resource ->
                val resourcePath = resource.resourceName.substring("META-INF".length)
                c.handle(this::webjars, c.param().path(), c.response().url())
                    .handle(HttpMethod.GET, resourcePath)
            }

        resources.stream()
            .filter { resource -> resource.resourceName.startsWith("com/github/mitallast/ghost/") }
            .forEach { resource ->
                val resourcePath = resource.resourceName.substring("com/github/mitallast/ghost".length)
                c.handle(this::resourceStatic, c.param().path(), c.response().url())
                    .handle(HttpMethod.GET, resourcePath)
            }

        c.handle(this::resourceFavicon, c.response().url())
            .handle(HttpMethod.GET, "favicon.ico")
    }

    private fun webjars(path: String): URL {
        return ResourceHandler::class.java.getResource("/META-INF$path")
    }

    private fun resourceStatic(path: String): URL {
        return ResourceHandler::class.java.getResource("/com/github/mitallast/ghost$path")
    }

    private fun resourceFavicon(): URL {
        return ResourceHandler::class.java.getResource("/favicon.ico")
    }
}
