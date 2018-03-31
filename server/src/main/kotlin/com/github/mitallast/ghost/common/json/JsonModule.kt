package com.github.mitallast.ghost.common.json

import com.google.inject.AbstractModule

class JsonModule : AbstractModule() {

    override fun configure() {
        bind(JsonService::class.java).asEagerSingleton()
    }
}
