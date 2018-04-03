package com.github.mitallast.ghost.session

import com.github.mitallast.ghost.common.actor.ActorRef
import com.google.inject.AbstractModule
import com.google.inject.name.Names

class SessionModule : AbstractModule() {
    override fun configure() {
        binder().disableCircularProxies()
        binder().requireExplicitBindings()

        bind(ActorRef::class.java)
            .annotatedWith(Names.named("session"))
            .toProvider(SessionActor::class.java)
            .asEagerSingleton()
    }
}