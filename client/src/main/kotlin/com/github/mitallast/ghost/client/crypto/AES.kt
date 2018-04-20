package com.github.mitallast.ghost.client.crypto

import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Uint8Array
import kotlin.js.Promise
import kotlin.js.json

sealed class AESKeyLen(val len: Int)
object AESKeyLen128 : AESKeyLen(128)
object AESKeyLen256 : AESKeyLen(256)

external class AESAlgorithm {
    val name: String
    val length: Int
}

external class AESKey : CryptoKey {
    override val type: String
    override val extractable: Boolean
    override val algorithm: AESAlgorithm
    override val usages: Array<String>
}

object AES {
    const val name = "AES-GCM"

    fun generateKey(len: AESKeyLen): Promise<AESKey> {
        return crypto.subtle.generateKey(
            algorithm = json(Pair("name", name), Pair("length", len.len)),
            extractable = true,
            keyUsages = arrayOf("encrypt", "decrypt", "wrapKey", "unwrapKey")
        )
    }

    fun exportKey(key: AESKey): Promise<ArrayBuffer> {
        return crypto.subtle.exportKey("raw", key)
    }

    fun importKey(key: ArrayBuffer): Promise<AESKey> {
        return crypto.subtle.importKey(
            format = "raw",
            keyData = key,
            algorithm = json(Pair("name", name)),
            extractable = true,
            keyUsages = arrayOf("encrypt", "decrypt", "wrapKey", "unwrapKey")
        )
    }

    fun encrypt(key: AESKey, data: ArrayBuffer): Promise<Pair<ArrayBuffer, Uint8Array>> {
        val iv = Uint8Array(12)
        crypto.getRandomValues(iv)
        return crypto.subtle.encrypt(
            algorithm = json(Pair("name", name), Pair("iv", iv)),
            key = key,
            data = data
        ).then { Pair(it, iv) }
    }

    fun decrypt(key: AESKey, data: ArrayBuffer, iv: Uint8Array): Promise<ArrayBuffer> {
        return crypto.subtle.decrypt(
            algorithm = json(Pair("name", name), Pair("iv", iv)),
            key = key,
            data = data
        )
    }

    fun wrapRSAPublicKey(wrappingKey: AESKey, key: RSAPublicKey): Promise<Pair<ArrayBuffer, Uint8Array>> {
        val iv = Uint8Array(12)
        crypto.getRandomValues(iv)
        return crypto.subtle.wrapKey(
            format = "spki",
            key = key,
            wrappingKey = wrappingKey,
            algorithm = json(Pair("name", name), Pair("iv", iv))
        ).then { Pair(it, iv) }
    }

    fun unwrapRSAPublicKey(unwrappingKey: AESKey, iv: Uint8Array, wrappedKey: ArrayBuffer, hash: Hash): Promise<RSAPublicKey> {
        return crypto.subtle.unwrapKey(
            format = "spki",
            wrappedKey = wrappedKey,
            unwrappingKey = unwrappingKey,
            unwrapAlgorithm = json(Pair("name", name), Pair("iv", iv)),
            unwrappedKeyAlgorithm = json(Pair("name", RSA.name), Pair("hash", json(Pair("name", hash.name)))),
            extractable = true,
            keyUsages = arrayOf("encrypt", "wrapKey")
        )
    }


    fun wrapRSAPrivateKey(wrappingKey: AESKey, key: RSAPrivateKey): Promise<Pair<ArrayBuffer, Uint8Array>> {
        val iv = Uint8Array(12)
        crypto.getRandomValues(iv)
        return crypto.subtle.wrapKey(
            format = "pkcs8",
            key = key,
            wrappingKey = wrappingKey,
            algorithm = json(Pair("name", name), Pair("iv", iv))
        ).then { Pair(it, iv) }
    }

    fun unwrapRSAPrivateKey(wrappingKey: AESKey, iv: Uint8Array, key: ArrayBuffer, hash: Hash): Promise<RSAPrivateKey> {
        return crypto.subtle.unwrapKey(
            format = "pkcs8",
            wrappedKey = key,
            unwrappingKey = wrappingKey,
            unwrapAlgorithm = json(Pair("name", name), Pair("iv", iv)),
            unwrappedKeyAlgorithm = json(Pair("name", RSA.name), Pair("hash", json(Pair("name", hash.name)))),
            extractable = true,
            keyUsages = arrayOf("decrypt", "unwrapKey")
        )
    }

    fun wrapAESKey(wrappingKey: AESKey, key: AESKey): Promise<Pair<ArrayBuffer, Uint8Array>> {
        val iv = Uint8Array(12)
        crypto.getRandomValues(iv)
        return crypto.subtle.wrapKey(
            format = "pkcs8",
            key = key,
            wrappingKey = wrappingKey,
            algorithm = json(Pair("name", name), Pair("iv", iv))
        ).then { Pair(it, iv) }
    }

    fun unwrapAESKey(wrappingKey: AESKey, iv: Uint8Array, key: ArrayBuffer): Promise<AESKey> {
        return crypto.subtle.unwrapKey(
            format = "pkcs8",
            wrappedKey = key,
            unwrappingKey = wrappingKey,
            unwrapAlgorithm = json(Pair("name", name), Pair("iv", iv)),
            unwrappedKeyAlgorithm = json(Pair("name", name)),
            extractable = true,
            keyUsages = arrayOf("encrypt", "decrypt")
        )
    }
}