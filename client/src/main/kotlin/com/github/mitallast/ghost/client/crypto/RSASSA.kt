package com.github.mitallast.ghost.client.crypto

import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Uint8Array
import kotlin.js.Promise
import kotlin.js.json

external class RSASSAKeyPair {
    val privateKey: RSAPrivateKey
    val publicKey: RSAPublicKey
}

external class RSASSAKeyAlgorithm {
    val name: String
}

external class RSASSAPrivateKey : CryptoKey {
    override val type: String
    override val extractable: Boolean
    override val algorithm: RSAKeyAlgorithm
    override val usages: Array<String>
}

external class RSASSAPublicKey : CryptoKey {
    override val type: String
    override val extractable: Boolean
    override val algorithm: RSAKeyAlgorithm
    override val usages: Array<String>
}

object RSASSA {
    const val name = "RSASSA-PKCS1-v1_5"
    private val publicExp = Uint8Array(arrayOf<Byte>(0x01, 0x00, 0x01))

    fun generateKey(modulus: ModulusLen, hash: Hash): Promise<RSASSAKeyPair> {
        return crypto.subtle.generateKey(
            algorithm = json(
                Pair("name", name),
                Pair("modulusLength", modulus.len),
                Pair("publicExponent", publicExp),
                Pair("hash", json(Pair("name", hash.name)))
            ),
            extractable = true,
            keyUsages = arrayOf("sign", "verify")
        )
    }

    fun exportPublicKey(key: RSASSAPublicKey): Promise<ArrayBuffer> {
        return crypto.subtle.exportKey("spki", key)
    }

    fun exportPrivateKey(key: RSASSAPrivateKey): Promise<ArrayBuffer> {
        return crypto.subtle.exportKey("pkcs8", key)
    }

    fun importPublicKey(hash: Hash, key: ArrayBuffer): Promise<RSASSAPublicKey> {
        return crypto.subtle.importKey(
            format = "spki",
            keyData = key,
            algorithm = json(Pair("name", name), Pair("hash", json(Pair("name", hash.name)))),
            extractable = true,
            keyUsages = arrayOf("verify")
        )
    }

    fun importPrivateKey(hash: Hash, key: ArrayBuffer): Promise<RSASSAPrivateKey> {
        return crypto.subtle.importKey(
            format = "pkcs8",
            keyData = key,
            algorithm = json(Pair("name", name), Pair("hash", json(Pair("name", hash.name)))),
            extractable = true,
            keyUsages = arrayOf("sign")
        )
    }

    fun sign(key: RSASSAPrivateKey, data: ArrayBuffer): Promise<ArrayBuffer> {
        return crypto.subtle.sign(
            algorithm = json(Pair("name", name)),
            key = key,
            data = data
        )
    }

    fun verify(key: RSASSAPublicKey, sign: ArrayBuffer, data: ArrayBuffer): Promise<Boolean> {
        return crypto.subtle.verify(
            algorithm = json(Pair("name", name)),
            key = key,
            sign = sign,
            data = data
        )
    }
}