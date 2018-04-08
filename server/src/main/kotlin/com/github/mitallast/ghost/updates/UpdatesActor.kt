package com.github.mitallast.ghost.updates

import com.github.mitallast.ghost.common.actor.Actor
import com.github.mitallast.ghost.common.actor.ActorRef
import com.github.mitallast.ghost.common.actor.ActorSystem
import com.github.mitallast.ghost.common.codec.CodecMessage
import com.github.mitallast.ghost.session.SessionSend
import com.google.inject.Inject
import com.google.inject.name.Named
import org.apache.logging.log4j.LogManager
import org.bouncycastle.util.encoders.Hex

class ForceUpdate(val auth: ByteArray)
class AuthSendUpdate(val from: ByteArray, val to: ByteArray, val randomId: Long, val update: CodecMessage)
class AuthUpdateInstalled(val auth: ByteArray, val last: Long)
class AuthUpdateRejected(val auth: ByteArray, val last: Long)

class UpdatesActor @Inject constructor(
    system: ActorSystem,
    private val updatesStore: UpdatesStore,
    @Named("session") private val session: ActorRef
) : Actor(system) {
    private val logger = LogManager.getLogger()

    override fun handle(message: Any, sender: ActorRef) {
        when (message) {
            is AuthSendUpdate -> {
                logger.info("received send update auth={} rid={}", Hex.toHexString(message.to), message.randomId)
                val update = updatesStore.append(message.from, message.to, message.update)
                session.send(SessionSend(message.from, SendAck(message.randomId)))
                val last = updatesStore.lastInstalled(message.to)
                logger.info("installed=$last sequence=${update.sequence} auth={}", Hex.toHexString(message.to))
                if (last + 1 == update.sequence) {
                    session.send(SessionSend(message.to, update))
                }
            }
            is ForceUpdate -> {
                logger.info("received force update auth={}", Hex.toHexString(message.auth))
                val currentSequence = updatesStore.currentSequence(message.auth)
                val lastInstalled = updatesStore.lastInstalled(message.auth)
                maybeSendUpdates(message.auth, lastInstalled, currentSequence)
            }
            is AuthUpdateInstalled -> {
                logger.info("received update installed auth={} i={}", Hex.toHexString(message.auth), message.last)
                updatesStore.mark(message.auth, message.last)
                val current = updatesStore.currentSequence(message.auth)
                maybeSendUpdates(message.auth, message.last, current)
            }
            is AuthUpdateRejected -> {
                logger.info("received update rejected auth={} i={}", Hex.toHexString(message.auth), message.last)
                updatesStore.mark(message.auth, message.last)
                val current = updatesStore.currentSequence(message.auth)
                maybeSendUpdates(message.auth, message.last, current)
            }
        }
    }

    private fun maybeSendUpdates(auth: ByteArray, lastInstalled: Long, currentSequence: Long) {
        logger.info("current=$currentSequence last=$lastInstalled")
        if (lastInstalled < currentSequence) {
            val updates = updatesStore.loadFrom(auth, lastInstalled, 100)
            logger.info("send install update {}", updates.size)
            session.send(SessionSend(auth, InstallUpdate(updates)))
        }
    }
}