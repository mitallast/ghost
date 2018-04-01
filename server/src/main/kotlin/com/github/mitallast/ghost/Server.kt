package com.github.mitallast.ghost

import com.google.inject.AbstractModule
import com.google.inject.Injector
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import com.github.mitallast.ghost.common.component.AbstractLifecycleComponent
import com.github.mitallast.ghost.common.component.ComponentModule
import com.github.mitallast.ghost.common.component.LifecycleService
import com.github.mitallast.ghost.common.component.ModulesBuilder
import com.github.mitallast.ghost.common.file.FileModule
import com.github.mitallast.ghost.common.json.JsonModule
import com.github.mitallast.ghost.common.netty.NettyModule
import com.github.mitallast.ghost.client.ecdh.ECDHModule
import com.github.mitallast.ghost.rest.RestModule

class Server(conf: Config, vararg plugins: AbstractModule) : AbstractLifecycleComponent() {

    private val config = conf.withFallback(ConfigFactory.defaultReference())
    private val injector: Injector

    init {
        logger.info("initializing...")

        val modules = ModulesBuilder()
        modules.add(ComponentModule(config))
        modules.add(FileModule())
        modules.add(JsonModule())
        modules.add(ECDHModule())
        modules.add(NettyModule())
        modules.add(RestModule())

        modules.add(*plugins)
        injector = modules.createInjector()

        logger.info("initialized")
    }

    fun config(): Config = config

    fun injector(): Injector = injector

    override fun doStart() {
        injector.getInstance(LifecycleService::class.java).start()
    }

    override fun doStop() {
        injector.getInstance(LifecycleService::class.java).stop()
    }

    override fun doClose() {
        injector.getInstance(LifecycleService::class.java).close()
    }
}
