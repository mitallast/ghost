package com.github.mitallast.ghost.client.crypto

import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Uint8Array
import kotlin.js.Json
import kotlin.js.Promise

external val crypto: Crypto

external interface Crypto {
    val subtle: SubtleCrypto

    fun getRandomValues(array: Uint8Array)
}

external interface SubtleCrypto {
    fun generateKey(algorithm: Json, extractable: Boolean, keyUsages: Array<String>): Promise<dynamic>
    fun encrypt(algorithm: Json, key: CryptoKey, data: ArrayBuffer): Promise<ArrayBuffer>
    fun decrypt(algorithm: Json, key: CryptoKey, data: ArrayBuffer): Promise<ArrayBuffer>
    fun sign(algorithm: Json, key: CryptoKey, data: ArrayBuffer): Promise<ArrayBuffer>
    fun verify(algorithm: Json, key: CryptoKey, sign: ArrayBuffer, data: ArrayBuffer): Promise<Boolean>
    fun digest(algorithm: Json, data: ArrayBuffer): Promise<ArrayBuffer>
    fun deriveKey(algorithm: Json, baseKey: CryptoKey, derivedKeyType: Json, extractable: Boolean, keyUsages: Array<String>): Promise<dynamic>
    fun deriveBits(algorithm: Json, baseKey: CryptoKey, length: Int): Promise<ArrayBuffer>
    fun importKey(format: String, keyData: dynamic, algorithm: Json, extractable: Boolean, keyUsages: Array<String>): Promise<dynamic>
    fun exportKey(format: String, key: CryptoKey): Promise<ArrayBuffer>
    fun wrapKey(format: String, key: CryptoKey, wrappingKey: CryptoKey, algorithm: Json): Promise<ArrayBuffer>
    fun unwrapKey(
        format: String,
        wrappedKey: ArrayBuffer,
        unwrappingKey: CryptoKey,
        unwrapAlgorithm: Json,
        unwrappedKeyAlgorithm: Json,
        extractable: Boolean,
        keyUsages: Array<String>
    ): Promise<dynamic>
}

external interface CryptoKey {
    val type: String
    val extractable: Boolean
    val algorithm: Any
    val usages: Array<String>
}

external interface CryptoKeyPair<out PrivateKey : CryptoKey, out PublicKey : CryptoKey> {
    val privateKey: PrivateKey
    val publicKey: PublicKey
}