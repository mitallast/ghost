package com.github.mitallast.ghost.persistent

import com.google.inject.AbstractModule

class PersistentModule : AbstractModule() {
    override fun configure() {
        binder().disableCircularProxies()
        binder().requireExplicitBindings()

        bind(PersistentService::class.java).asEagerSingleton()
    }
}