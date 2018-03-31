package com.github.mitallast.ghost.session

import com.github.mitallast.ghost.common.codec.Message
import com.github.mitallast.ghost.dh.Auth
import com.github.mitallast.ghost.message.TextMessage
import org.apache.logging.log4j.LogManager
import java.util.concurrent.ConcurrentHashMap

interface SessionContext {
    fun auth(): Auth
    fun send(message: Message)
}

class SessionService {
    private val logger = LogManager.getLogger()

    private val sessionMap = ConcurrentHashMap<Auth, SessionContext>()

    fun registered(session: SessionContext) {
        sessionMap[session.auth()] = session
    }

    fun inactive(auth: Auth) {
        sessionMap.remove(auth)
    }

    fun handle(auth: Auth, message: Message) {
        when (message) {
            is TextMessage -> {
                logger.info("text message: {}", message.text)
                send(auth, TextMessage("hello from service"))
            }
        }
    }

    fun send(auth: Auth, message: Message) {
        sessionMap[auth]?.send(message)
    }
}