package com.github.mitallast.ghost.common.netty

import com.google.inject.AbstractModule

class NettyModule : AbstractModule() {
    override fun configure() {
        bind(NettyProvider::class.java).asEagerSingleton()
    }
}
