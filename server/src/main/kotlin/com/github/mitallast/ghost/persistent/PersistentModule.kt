package com.github.mitallast.ghost.persistent

import com.google.inject.AbstractModule

class PersistentModule : AbstractModule() {
    override fun configure() {
        bind(PersistentService::class.java).asEagerSingleton()
    }
}