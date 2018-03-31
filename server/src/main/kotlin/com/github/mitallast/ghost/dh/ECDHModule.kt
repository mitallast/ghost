package com.github.mitallast.ghost.dh

import com.google.inject.AbstractModule

class ECDHModule : AbstractModule() {

    override fun configure() {
        bind(ECDHService::class.java).asEagerSingleton()
    }
}
