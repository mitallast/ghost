package com.github.mitallast.ghost.session

import com.github.mitallast.ghost.common.codec.Message
import com.github.mitallast.ghost.client.ecdh.Auth
import com.github.mitallast.ghost.client.ecdh.AuthStore
import com.github.mitallast.ghost.e2ee.E2EEncrypted
import com.github.mitallast.ghost.e2ee.E2ERequest
import com.github.mitallast.ghost.e2ee.E2EResponse
import com.github.mitallast.ghost.message.TextMessage
import org.apache.logging.log4j.LogManager
import org.bouncycastle.util.encoders.Hex
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject

interface SessionContext {
    fun auth(): Auth
    fun send(message: Message)
}

class SessionService @Inject constructor(
    private val authStore: AuthStore
) {
    private val logger = LogManager.getLogger()

    private val sessionMap = ConcurrentHashMap<String, SessionContext>()

    fun registered(session: SessionContext) {
        val key = Hex.toHexString(session.auth().auth)
        sessionMap[key] = session
    }

    fun inactive(auth: Auth) {
        val key = Hex.toHexString(auth.auth)
        sessionMap.remove(key)
    }

    fun handle(auth: Auth, message: Message) {
        when (message) {
            is E2ERequest -> {
                logger.info("e2e request: from={} to={}", Hex.toHexString(message.from), Hex.toHexString(message.to))
                send(message.to, message)
            }
            is E2EResponse -> {
                logger.info("e2e response: from={} to={}", Hex.toHexString(message.from), Hex.toHexString(message.to))
                send(message.to, message)
            }
            is E2EEncrypted -> {
                logger.info("e2e encrypted: from={} to={}", Hex.toHexString(message.from), Hex.toHexString(message.to))
                send(message.to, message)
            }
            is TextMessage -> {
                logger.info("text message: {}", message.text)
                send(auth.auth, TextMessage("hello from service"))
            }
            else ->
                logger.warn("unexpected message: {}", message)
        }
    }

    fun send(auth: ByteArray, message: Message) {
        val key = Hex.toHexString(auth)
        logger.info("send to={} {}", key, message)
        sessionMap[key]?.send(message)
    }
}