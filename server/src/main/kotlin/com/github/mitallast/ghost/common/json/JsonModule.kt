package com.github.mitallast.ghost.common.json

import com.google.inject.AbstractModule

class JsonModule : AbstractModule() {
    override fun configure() {
        binder().disableCircularProxies()
        binder().requireExplicitBindings()

        bind(JsonService::class.java).asEagerSingleton()
    }
}
