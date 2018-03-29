package com.github.mitallast.ghost.client.crypto

import org.khronos.webgl.ArrayBuffer
import kotlin.js.Promise
import kotlin.js.json

external class ECDSAKeyPair {
    val privateKey: ECDSAPrivateKey
    val publicKey: ECDSAPublicKey
}

external class ECDSAAlgorithm {
    val name: String
    val namedCurve: String
}

external class ECDSAPrivateKey : CryptoKey {
    override val type: String
    override val extractable: Boolean
    override val algorithm: ECDSAAlgorithm
    override val usages: Array<String>
}

external class ECDSAPublicKey : CryptoKey {
    override val type: String
    override val extractable: Boolean
    override val algorithm: ECDSAAlgorithm
    override val usages: Array<String>
}

object ECDSA {
    const val name = "ECDSA"

    fun generateKey(curve: Curve): Promise<ECDSAKeyPair> {
        return crypto.subtle.generateKey(
            algorithm = json(Pair("name", name), Pair("namedCurve", curve.name)),
            extractable = true,
            keyUsages = arrayOf("sign", "verify")
        )
    }

    fun exportPublicKey(key: ECDSAPublicKey): Promise<ArrayBuffer> {
        return crypto.subtle.exportKey("spki", key)
    }

    fun exportPrivateKey(key: ECDSAPrivateKey): Promise<ArrayBuffer> {
        return crypto.subtle.exportKey("pkcs8", key)
    }

    fun importPublicKey(curve: Curve, key: ArrayBuffer): Promise<ECDSAPublicKey> {
        return crypto.subtle.importKey(
            format = "spki",
            keyData = key,
            algorithm = json(Pair("name", name), Pair("namedCurve", curve.name)),
            extractable = true,
            keyUsages = arrayOf("verify")
        )
    }

    fun importPrivateKey(curve: Curve, key: ArrayBuffer): Promise<ECDSAPrivateKey> {
        return crypto.subtle.importKey(
            format = "pkcs8",
            keyData = key,
            algorithm = json(Pair("name", name), Pair("namedCurve", curve.name)),
            extractable = true,
            keyUsages = arrayOf("sign")
        )
    }

    fun sign(hash: Hash, key: ECDSAPrivateKey, data: ArrayBuffer): Promise<ArrayBuffer> {
        return crypto.subtle.sign(
            algorithm = json(Pair("name", name), Pair("hash", json(Pair("name", hash.name)))),
            key = key,
            data = data
        )
    }

    fun verify(hash: Hash, key: ECDSAPublicKey, sign: ArrayBuffer, data: ArrayBuffer): Promise<Boolean> {
        return crypto.subtle.verify(
            algorithm = json(Pair("name", name), Pair("hash", json(Pair("name", hash.name)))),
            key = key,
            sign = sign,
            data = data
        )
    }
}