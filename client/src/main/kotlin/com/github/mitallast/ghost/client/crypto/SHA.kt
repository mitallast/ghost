package com.github.mitallast.ghost.client.crypto

import org.khronos.webgl.ArrayBuffer
import kotlin.js.Promise
import kotlin.js.json

sealed class Hash(val name: String)
object HashSHA1 : Hash("SHA-1")
object HashSHA256 : Hash("SHA-256")
object HashSHA512 : Hash("SHA-512")

object SHA256 {
    fun digest(data: ArrayBuffer): Promise<ArrayBuffer> {
        return crypto.subtle.digest(json(Pair("name", "SHA-256")), data)
    }
}

object SHA512 {
    fun digest(data: ArrayBuffer): Promise<ArrayBuffer> {
        return crypto.subtle.digest(json(Pair("name", "SHA-512")), data)
    }
}