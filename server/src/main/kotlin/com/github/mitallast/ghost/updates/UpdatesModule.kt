package com.github.mitallast.ghost.updates

import com.github.mitallast.ghost.common.actor.ActorRef
import com.github.mitallast.ghost.common.codec.Message
import com.google.inject.AbstractModule
import com.google.inject.name.Names

class UpdatesModule : AbstractModule() {
    override fun configure() {
        binder().disableCircularProxies()
        binder().requireExplicitBindings()

        bind(UpdatesStore::class.java).asEagerSingleton()

        bind(ActorRef::class.java)
            .annotatedWith(Names.named("updates"))
            .toProvider(UpdatesActor::class.java)
            .asEagerSingleton()
    }
}