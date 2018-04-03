package com.github.mitallast.ghost.common.netty

import com.google.inject.AbstractModule

class NettyModule : AbstractModule() {
    override fun configure() {
        binder().disableCircularProxies()
        binder().requireExplicitBindings()

        bind(NettyProvider::class.java).asEagerSingleton()
    }
}
