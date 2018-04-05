package com.github.mitallast.ghost.session

import com.github.mitallast.ghost.common.actor.Actor
import com.github.mitallast.ghost.common.actor.ActorRef
import com.github.mitallast.ghost.common.actor.ActorSystem
import com.github.mitallast.ghost.common.codec.CodecMessage
import com.github.mitallast.ghost.rest.netty.SendMessage
import com.google.inject.Inject
import org.apache.logging.log4j.LogManager
import org.bouncycastle.util.encoders.Hex

class SessionRegistered(val auth: ByteArray)
class SessionInactive(val auth: ByteArray)
class SessionSend(val auth: ByteArray, val message: CodecMessage)

class SessionActor @Inject constructor(system: ActorSystem) : Actor(system) {
    private val logger = LogManager.getLogger()
    private val sessionMap = HashMap<String, ActorRef>()

    override fun handle(message: Any, sender: ActorRef) {
        when (message) {
            is SessionRegistered -> {
                val key = Hex.toHexString(message.auth)
                logger.info("session registered {} sender={}", key, sender)
                // sessionMap.remove(key)?.send(CloseChannel)
                sessionMap[key] = sender
                logger.info("sessions {}", sessionMap)
            }
            is SessionInactive -> {
                val key = Hex.toHexString(message.auth)
                logger.info("session inactive {}", key)
                sessionMap.remove(key)
            }
            is SessionSend -> {
                val key = Hex.toHexString(message.auth)
                logger.info("send to={} {}", key, message.message)
                logger.info("send to={}", sessionMap[key])
                logger.info("sessions {}", sessionMap)
                sessionMap[key]?.send(SendMessage(message.message))
            }
        }
    }
}