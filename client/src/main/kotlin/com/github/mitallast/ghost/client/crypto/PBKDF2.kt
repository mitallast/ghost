package com.github.mitallast.ghost.client.crypto

import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Uint8Array
import kotlin.js.Promise
import kotlin.js.json

external class PBKDF2KeyAlgorithm {
    val name: String
}

external class PBKDF2Key : CryptoKey {
    override val type: String
    override val extractable: Boolean
    override val algorithm: PBKDF2KeyAlgorithm
    override val usages: Array<String>
}

object PBKDF2 {
    const val name = "PBKDF2"
    private val encoder = TextEncoder()

    fun importKey(password: String): Promise<PBKDF2Key> {
        return importKey(encoder.encode(password))
    }

    fun importKey(password: Uint8Array): Promise<PBKDF2Key> {
        return crypto.subtle.importKey(
            format = "raw",
            keyData = password,
            algorithm = json(Pair("name", name)),
            extractable = false,
            keyUsages = arrayOf("deriveKey", "deriveBits")
        )
    }

    fun deriveKeyAES(salt: Uint8Array, iterations: Int, hash: Hash, key: PBKDF2Key, len: AESKeyLen): Promise<AESKey> {
        return crypto.subtle.deriveKey(
            algorithm = json(
                Pair("name", name),
                Pair("salt", salt),
                Pair("iterations", iterations),
                Pair("hash", json(Pair("name", hash.name)))
            ),
            baseKey = key,
            derivedKeyType = json(Pair("name", AES.name), Pair("length", len.len)),
            extractable = true,
            keyUsages = arrayOf("encrypt", "decrypt")
        )
    }

    fun deriveBits(salt: Uint8Array, iterations: Int, hash: Hash, key: PBKDF2Key, len: AESKeyLen): Promise<ArrayBuffer> {
        return crypto.subtle.deriveBits(
            algorithm = json(
                Pair("name", name),
                Pair("salt", salt),
                Pair("iterations", iterations),
                Pair("hash", json(Pair("name", hash.name)))
            ),
            baseKey = key,
            length = len.len
        )
    }
}