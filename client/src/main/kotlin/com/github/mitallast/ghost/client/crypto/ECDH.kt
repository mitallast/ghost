package com.github.mitallast.ghost.client.crypto

import org.khronos.webgl.ArrayBuffer
import kotlin.js.Promise
import kotlin.js.json

external class ECDHKeyPair {
    val privateKey: ECDHPrivateKey
    val publicKey: ECDHPublicKey
}

external class ECDHKeyAlgorithm {
    val name: String
    val namedCurve: String
}

external class ECDHPrivateKey : CryptoKey {
    override val type: String
    override val extractable: Boolean
    override val algorithm: ECDHKeyAlgorithm
    override val usages: Array<String>
}

external class ECDHPublicKey : CryptoKey {
    override val type: String
    override val extractable: Boolean
    override val algorithm: ECDHKeyAlgorithm
    override val usages: Array<String>
}

object ECDH {
    const val name = "ECDH"

    fun generateKey(curve: Curve): Promise<ECDHKeyPair> {
        return crypto.subtle.generateKey(
            algorithm = json(Pair("name", name), Pair("namedCurve", curve.name)),
            extractable = true,
            keyUsages = arrayOf("deriveKey", "deriveBits")
        )
    }

    fun exportPublicKey(key: ECDHPublicKey): Promise<ArrayBuffer> {
        return crypto.subtle.exportKey("spki", key).then { ECWrap.maybeWrap(it) }
    }

    fun exportPrivateKey(key: ECDHPrivateKey): Promise<ArrayBuffer> {
        console.log("export ECDH private key")
        return crypto.subtle.exportKey(ECWrap.privateKeyFormat(), key)
    }

    fun importPublicKey(curve: Curve, key: ArrayBuffer): Promise<ECDHPublicKey> {
        return crypto.subtle.importKey(
            format = "spki",
            keyData = ECWrap.maybeUnwrap(key),
            algorithm = json(Pair("name", name), Pair("namedCurve", curve.name)),
            extractable = true,
            keyUsages = arrayOf()
        )
    }

    fun importPrivateKey(curve: Curve, key: ArrayBuffer): Promise<ECDHPrivateKey> {
        return crypto.subtle.importKey(
            format = ECWrap.privateKeyFormat(),
            keyData = key,
            algorithm = json(Pair("name", name), Pair("namedCurve", curve.name)),
            extractable = true,
            keyUsages = arrayOf("deriveKey", "deriveBits")
        )
    }

    fun deriveKey(curve: Curve, publicKey: ECDHPublicKey, privateKey: ECDHPrivateKey, len: AESKeyLen): Promise<AESKey> {
        return crypto.subtle.deriveKey(
            algorithm = json(Pair("name", name), Pair("namedCurve", curve.name), Pair("public", publicKey)),
            baseKey = privateKey,
            derivedKeyType = json(Pair("name", AES.name), Pair("length", len.len)),
            extractable = true,
            keyUsages = arrayOf("encrypt", "decrypt")
        )
    }

    fun deriveBits(curve: Curve, publicKey: ECDHPublicKey, privateKey: ECDHPrivateKey, len: Int): Promise<ArrayBuffer> {
        return crypto.subtle.deriveBits(
            algorithm = json(Pair("name", name), Pair("namedCurve", curve.name), Pair("public", publicKey)),
            baseKey = privateKey,
            length = len
        )
    }
}