package com.github.mitallast.ghost.ecdh

import com.google.inject.AbstractModule

class ECDHModule : AbstractModule() {
    override fun configure() {
        binder().disableCircularProxies()
        binder().requireExplicitBindings()

        bind(AuthStore::class.java).asEagerSingleton()
        bind(ECDHService::class.java).asEagerSingleton()
    }
}
