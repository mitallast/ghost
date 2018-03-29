package com.github.mitallast.ghost.client.crypto

import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Uint8Array
import kotlin.js.Promise
import kotlin.js.json

sealed class ModulusLen(val len: Int)
object ModulusLen1024 : ModulusLen(1024)
object ModulusLen2048 : ModulusLen(2048)
object ModulusLen4096 : ModulusLen(4096)

external class RSAKeyPair {
    val privateKey: RSAPrivateKey
    val publicKey: RSAPublicKey
}

external class RSAKeyAlgorithm {
    val name: String
}

external class RSAPrivateKey : CryptoKey {
    override val type: String
    override val extractable: Boolean
    override val algorithm: RSAKeyAlgorithm
    override val usages: Array<String>
}

external class RSAPublicKey : CryptoKey {
    override val type: String
    override val extractable: Boolean
    override val algorithm: RSAKeyAlgorithm
    override val usages: Array<String>
}

object RSA {
    const val name = "RSA-OAEP"
    private val publicExp = Uint8Array(arrayOf<Byte>(0x01, 0x00, 0x01))

    fun generateKey(modulus: ModulusLen, hash: Hash): Promise<RSAKeyPair> {
        return crypto.subtle.generateKey(
            algorithm = json(
                Pair("name", name),
                Pair("modulusLength", modulus.len),
                Pair("publicExponent", publicExp),
                Pair("hash", json(Pair("name", hash.name)))
            ),
            extractable = true,
            keyUsages = arrayOf("encrypt", "decrypt", "wrapKey", "unwrapKey")
        )
    }

    fun exportPublicKey(key: RSAPublicKey): Promise<ArrayBuffer> {
        return crypto.subtle.exportKey("spki", key)
    }

    fun exportPrivateKey(key: RSAPrivateKey): Promise<ArrayBuffer> {
        return crypto.subtle.exportKey("pkcs8", key)
    }

    fun importPublicKey(hash: Hash, key: ArrayBuffer): Promise<RSAPublicKey> {
        return crypto.subtle.importKey(
            format = "spki",
            keyData = key,
            algorithm = json(Pair("name", name), Pair("hash", json(Pair("name", hash.name)))),
            extractable = true,
            keyUsages = arrayOf("encrypt", "wrapKey")
        )
    }

    fun importPrivateKey(hash: Hash, key: ArrayBuffer): Promise<RSAPrivateKey> {
        return crypto.subtle.importKey(
            format = "pkcs8",
            keyData = key,
            algorithm = json(Pair("name", name), Pair("hash", json(Pair("name", hash.name)))),
            extractable = true,
            keyUsages = arrayOf("decrypt", "unwrapKey")
        )
    }

    fun encrypt(key: RSAPublicKey, data: ArrayBuffer): Promise<ArrayBuffer> {
        return crypto.subtle.encrypt(
            algorithm = json(Pair("name", name)),
            key = key,
            data = data
        )
    }

    fun decrypt(key: RSAPrivateKey, data: ArrayBuffer): Promise<ArrayBuffer> {
        return crypto.subtle.decrypt(
            algorithm = json(Pair("name", name)),
            key = key,
            data = data
        )
    }

    fun wrapAESKey(wrappingKey: RSAPublicKey, hash: Hash, key: AESKey): Promise<ArrayBuffer> {
        return crypto.subtle.wrapKey(
            format = "raw",
            key = key,
            wrappingKey = wrappingKey,
            algorithm = json(Pair("name", name), Pair("hash", json(Pair("name", hash.name))))
        )
    }

    fun unwrapAESKey(unwrappingKey: RSAPrivateKey, modulus: ModulusLen, hash: Hash, wrappedKey: ArrayBuffer, len: AESKeyLen): Promise<AESKey> {
        return crypto.subtle.unwrapKey(
            format = "raw",
            wrappedKey = wrappedKey,
            unwrappingKey = unwrappingKey,
            unwrapAlgorithm = json(
                Pair("name", name),
                Pair("modulusLength", modulus.len),
                Pair("publicExponent", publicExp),
                Pair("hash", json(Pair("name", hash.name)))
            ),
            unwrappedKeyAlgorithm = json(Pair("name", AES.name), Pair("length", len.len)),
            extractable = true,
            keyUsages = arrayOf("encrypt", "decrypt")
        )
    }
}