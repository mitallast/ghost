package com.github.mitallast.ghost.common.actor

import com.google.inject.Provider
import io.netty.channel.DefaultEventLoop
import io.vavr.concurrent.Future
import io.vavr.concurrent.Promise
import org.apache.logging.log4j.LogManager

abstract class Actor(private val system: ActorSystem) : Provider<ActorRef> {
    protected val self: ActorRef = system.materialize(this)
    override fun get(): ActorRef = self
    abstract fun handle(message: Any, sender: ActorRef)
    open fun handle(message: Throwable, sender: ActorRef) {}
}

interface ActorRef {
    fun send(message: Any)
    fun send(message: Any, sender: ActorRef)
    fun send(error: Throwable)
    fun send(error: Throwable, sender: ActorRef)
    fun ack(message: Any): Future<Any>
    fun forward(message: Any, sender: ActorRef)
}

interface ActorSystem {
    fun materialize(actor: Actor): ActorRef
}

internal class DefaultActorRef(
    private val actorSystem: DefaultActorSystem,
    private val actor: Actor
) : ActorRef {
    override fun send(message: Any) {
        actorSystem.send(actor, message, NoopActorRef)
    }

    override fun send(message: Any, sender: ActorRef) {
        actorSystem.send(actor, message, sender)
    }

    override fun send(error: Throwable) {
        actorSystem.send(actor, error, NoopActorRef)
    }

    override fun send(error: Throwable, sender: ActorRef) {
        actorSystem.send(actor, error, sender)
    }

    override fun ack(message: Any): Future<Any> {
        return actorSystem.ack(actor, message)
    }

    override fun forward(message: Any, sender: ActorRef) {
        return actorSystem.send(actor, message, sender)
    }
}

internal class PromiseActorRef(private val promise: Promise<Any>) : ActorRef {
    override fun send(message: Any) {
        promise.success(message)
    }

    override fun send(message: Any, sender: ActorRef) {
        throw IllegalAccessException()
    }

    override fun send(error: Throwable) {
        promise.failure(error)
    }

    override fun send(error: Throwable, sender: ActorRef) {
        throw IllegalAccessException()
    }

    override fun ack(message: Any): Future<Any> {
        return Future.failed(IllegalAccessException())
    }

    override fun forward(message: Any, sender: ActorRef) {
        throw IllegalAccessException()
    }

}

internal object NoopActorRef : ActorRef {
    override fun send(message: Any) {
        throw IllegalAccessError()
    }

    override fun send(message: Any, sender: ActorRef) {
        throw IllegalAccessError()
    }

    override fun send(error: Throwable) {
        throw IllegalAccessError()
    }

    override fun send(error: Throwable, sender: ActorRef) {
        throw IllegalAccessError()
    }

    override fun ack(message: Any): Future<Any> {
        return Future.failed(IllegalAccessException())
    }

    override fun forward(message: Any, sender: ActorRef) {
        throw IllegalAccessError()
    }
}

internal class DefaultActorSystem : ActorSystem {
    private val logger = LogManager.getLogger()
    private val eventLoop = DefaultEventLoop()

    override fun materialize(actor: Actor): ActorRef {
        return DefaultActorRef(this, actor)
    }

    internal fun send(actor: Actor, message: Any, sender: ActorRef) {
        eventLoop.execute {
            try {
                actor.handle(message, sender)
            } catch (e: Throwable) {
                logger.error(e)
            }
        }
    }

    internal fun send(actor: Actor, error: Throwable, sender: ActorRef) {
        eventLoop.execute { actor.handle(error, sender) }
    }

    internal fun ack(actor: Actor, message: Any): Future<Any> {
        val promise = Promise.make<Any>()
        val ack = PromiseActorRef(promise)
        send(actor, message, ack)
        return promise.future()
    }
}