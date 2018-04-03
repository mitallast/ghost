package com.github.mitallast.ghost.updates

import com.github.mitallast.ghost.common.actor.Actor
import com.github.mitallast.ghost.common.actor.ActorRef
import com.github.mitallast.ghost.common.actor.ActorSystem
import com.github.mitallast.ghost.common.codec.Message
import com.github.mitallast.ghost.session.SessionSend
import com.google.inject.Inject
import com.google.inject.name.Named
import org.apache.logging.log4j.LogManager
import org.bouncycastle.util.encoders.Hex

class ForceUpdate(val auth: ByteArray)
class SendUpdate(val auth: ByteArray, val update: Message)
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
            is SendUpdate -> {
                logger.info("received send update auth={}", Hex.toHexString(message.auth))
                val sequence = updatesStore.append(message.auth, message.update)
                val update = Update(sequence, message.update)
                val last = updatesStore.lastInstalled(message.auth)
                logger.info("installed=$last sequence=$sequence auth={}", Hex.toHexString(message.auth))
                if (last + 1 == sequence) {
                    session.send(SessionSend(message.auth, update))
                }
            }
            is ForceUpdate -> {
                logger.info("received force update auth={}", Hex.toHexString(message.auth))
                val last = updatesStore.currentSequence(message.auth)
                logger.info("currentSequence=$last auth={}", Hex.toHexString(message.auth))
                maybeSendUpdates(message.auth, last)
            }
            is AuthUpdateInstalled -> {
                logger.info("received update installed auth={} i={}", Hex.toHexString(message.auth), message.last)
                updatesStore.mark(message.auth, message.last)
                maybeSendUpdates(message.auth, message.last)
            }
            is AuthUpdateRejected -> {
                logger.info("received update rejected auth={} i={}", Hex.toHexString(message.auth), message.last)
                updatesStore.mark(message.auth, message.last)
                maybeSendUpdates(message.auth, message.last)
            }
        }
    }

    private fun maybeSendUpdates(auth: ByteArray, currentSequence: Long) {
        val installed = updatesStore.lastInstalled(auth)
        logger.info("installed=$installed auth={}", Hex.toHexString(auth))
        if (installed < currentSequence) {
            val updates = updatesStore.loadFrom(auth, installed, 100)
            logger.info("send install update {}", updates.size)
            session.send(SessionSend(auth, InstallUpdate(updates)))
        }
    }
}