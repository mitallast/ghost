package com.github.mitallast.ghost.ecdh

import com.github.mitallast.ghost.common.crypto.AES
import com.github.mitallast.ghost.common.crypto.ECDH
import com.github.mitallast.ghost.common.crypto.ECDSA
import com.google.inject.Inject
import com.typesafe.config.Config
import java.lang.IllegalArgumentException
import javax.xml.bind.DatatypeConverter.parseHexBinary

class ECDHService @Inject constructor(
    config: Config,
    private val authStore: AuthStore
) {
    private val privateKeyBytes = parseHexBinary(config.getString("security.ecdsa.private"))
    private val privateKey = ECDSA.importPrivateKey(privateKeyBytes)

    fun ecdh(request: ECDHRequest): Pair<Auth, ECDHResponse> {
        val serverECHD = ECDH.generate()
        val clientECDHPublicKey = ECDH.importPublicKey(request.ecdhPublicKey)
        val clientECDSAPublicKey = ECDSA.importPublicKey(request.ecdsaPublicKey)
        val requestSignDER = ECDSA.raw2der(request.sign)
        if (!ECDSA.verify(clientECDSAPublicKey, requestSignDER, request.ecdhPublicKey, request.ecdsaPublicKey)) {
            throw IllegalArgumentException("not verified")
        }
        val secretKey = ECDH.deriveKey(serverECHD.private, clientECDHPublicKey)
        val encoded = serverECHD.public.encoded

        val authId = authStore.generateAuthId()
        authStore.storeAES(authId, secretKey)
        authStore.storePublicECDSA(authId, clientECDSAPublicKey)
        val auth = Auth(authId, clientECDSAPublicKey, secretKey)

        val sign = ECDSA.sign(privateKey, authId, encoded)
        val signRaw = ECDSA.der2raw(sign)
        val response = ECDHResponse(authId, encoded, signRaw)

        return Pair(auth, response)
    }

    fun reconnect(message: ECDHReconnect): Auth {
        val aes = authStore.loadAES(message.auth)
        val ecdsa = authStore.loadPublicECDSA(message.auth)

        val signDER = ECDSA.raw2der(message.sign)
        if (!ECDSA.verify(ecdsa, signDER, message.auth, ecdsa.encoded)) {
            throw IllegalArgumentException("not verified")
        }
        return Auth(message.auth, ecdsa, aes)
    }

    fun encrypt(auth: Auth, data: ByteArray): ECDHEncrypted {
        val aes = auth.secretKey

        val iv = AES.iv()
        val encrypted = AES.encrypt(aes, iv, data)
        val sign = ECDSA.sign(privateKey, auth.auth, iv, data)
        val signRaw = ECDSA.der2raw(sign)

        return ECDHEncrypted(auth.auth, signRaw, iv, encrypted)
    }

    fun decrypt(auth: Auth, encrypted: ECDHEncrypted): ByteArray {
        val aes = auth.secretKey
        val ecdsa = auth.publicKey

        val decrypted = AES.decrypt(aes, encrypted.iv, encrypted.encrypted)
        val signDER = ECDSA.raw2der(encrypted.sign)
        if (!ECDSA.verify(ecdsa, signDER, auth.auth, encrypted.iv, decrypted)) {
            throw IllegalArgumentException("not verified")
        }
        return decrypted
    }
}