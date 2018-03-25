package com.github.mitallast.ghost.client

import com.github.mitallast.ghost.HelloMessage
import com.github.mitallast.ghost.codec.ByteDataInput
import com.github.mitallast.ghost.codec.ByteDataOutput

fun main(args: Array<String>) {
    val write = HelloMessage(12123, "Hello world")
    val out = ByteDataOutput()
    HelloMessage.codec.write(out, write)
    val array = out.toByteArray()
    val input = ByteDataInput(array)
    val read = HelloMessage.codec.read(input)

    println("write")
    println(write)
    println("read")
    println(read)
}