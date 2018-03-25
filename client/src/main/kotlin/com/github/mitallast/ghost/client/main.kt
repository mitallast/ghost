package com.github.mitallast.ghost.client

import com.github.mitallast.ghost.HelloMessage
import kotlinx.io.ByteArrayInputStream
import kotlinx.io.ByteArrayOutputStream

fun main(args: Array<String>) {
    val write = HelloMessage(12123, "Hello world")
    val out = ByteArrayOutputStream()
    HelloMessage.codec.write(out, write)
    val array = out.toByteArray()
    val input = ByteArrayInputStream(array)
    val read = HelloMessage.codec.read(input)

    println("write")
    println(write)
    println("read")
    println(read)
}