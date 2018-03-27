package com.github.mitallast.ghost.client

import com.github.mitallast.ghost.HelloMessage
import com.github.mitallast.ghost.crypto.pkc.RSA
import kotlinx.io.ByteArrayInputStream
import kotlinx.io.ByteArrayOutputStream

fun main(args: Array<String>) {
    val write = HelloMessage(12123, "Hello world")
    println("write")
    println(write)

    val out = ByteArrayOutputStream()
    HelloMessage.codec.write(out, write)
    val serialized = out.toByteArray()

    val keyPair = RSA.keyPair(1024)
    val encrypted = RSA.encrypt(serialized, keyPair.publicKey)
    val decrypted = RSA.decrypt(encrypted, keyPair.privateKey)

    val input = ByteArrayInputStream(decrypted)
    val read = HelloMessage.codec.read(input)

    println("read")
    println(read)
}