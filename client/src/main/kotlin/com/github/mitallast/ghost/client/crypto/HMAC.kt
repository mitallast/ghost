package com.github.mitallast.ghost.client.crypto

import org.khronos.webgl.ArrayBuffer
import kotlin.js.Promise
import kotlin.js.json

external class HMACHashAlgorithm {
    val name: String
}

external class HMACAlgorithm {
    val name: String
    val hash: HMACHashAlgorithm
    val length: Int
}

external class HMACKey : CryptoKey {
    override val type: String
    override val extractable: Boolean
    override val algorithm: HMACAlgorithm
    override val usages: Array<String>
}

object HMAC {
    const val name = "HMAC"

    fun generateKey(hash: Hash): Promise<HMACKey> {
        return crypto.subtle.generateKey(
            algorithm = json(Pair("name", name), Pair("hash", json(Pair("name", hash.name)))),
            extractable = true,
            keyUsages = arrayOf("sign", "verify")
        )
    }

    fun exportKey(key: HMACKey): Promise<ArrayBuffer> {
        return crypto.subtle.exportKey("raw", key)
    }

    fun importKey(hash: Hash, key: ArrayBuffer): Promise<HMACKey> {
        return crypto.subtle.importKey(
            format = "raw",
            keyData = key,
            algorithm = json(Pair("name", name), Pair("hash", json(Pair("name", hash.name)))),
            extractable = true,
            keyUsages = arrayOf("sign", "verify")
        )
    }

    fun sign(key: HMACKey, data: ArrayBuffer): Promise<ArrayBuffer> {
        return crypto.subtle.sign(
            algorithm = json(Pair("name", name)),
            key = key,
            data = data
        )
    }

    fun verify(key: HMACKey, sign: ArrayBuffer, data: ArrayBuffer): Promise<Boolean> {
        return crypto.subtle.verify(
            algorithm = json(Pair("name", name)),
            key = key,
            sign = sign,
            data = data
        )
    }
}