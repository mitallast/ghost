package com.github.mitallast.ghost.common.actor

import com.google.inject.AbstractModule

class ActorModule : AbstractModule() {
    override fun configure() {
        binder().disableCircularProxies()
        binder().requireExplicitBindings()

        bind(ActorSystem::class.java).to(DefaultActorSystem::class.java).asEagerSingleton()
    }
}