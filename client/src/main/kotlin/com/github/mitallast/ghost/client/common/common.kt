package com.github.mitallast.ghost.client.common

import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Uint8Array
import org.khronos.webgl.get
import org.khronos.webgl.set
import kotlin.coroutines.experimental.*
import kotlin.js.Promise

suspend fun <T> Promise<T>.await(): T = suspendCoroutine { cont ->
    then({ cont.resume(it) }, { cont.resumeWithException(it) })
}

fun launch(block: suspend () -> Unit) {
    block.startCoroutine(object : Continuation<Unit> {
        override val context: CoroutineContext get() = EmptyCoroutineContext
        override fun resume(value: Unit) {}
        override fun resumeWithException(exception: Throwable) {
            println("Coroutine failed: $exception")
        }
    })
}

fun toByteArray(array: ArrayBuffer): ByteArray {
    val view = Uint8Array(array)
    return ByteArray(view.length, { view[it] })
}

fun toArrayBuffer(bytes: ByteArray): ArrayBuffer {
    val view = Uint8Array(bytes.size)
    for (i in 0 until bytes.size) {
        view[i] = bytes[i]
    }
    return view.buffer
}

fun toArrayBuffer(vararg buffers: dynamic): ArrayBuffer {
    val size = buffers.fold(0, { a, b ->
        when (b) {
            is ByteArray -> a + b.size
            is Uint8Array -> a + b.length
            is ArrayBuffer -> a + b.byteLength
            else -> throw IllegalArgumentException()
        }
    })
    val view = Uint8Array(size)
    var offset = 0
    buffers.forEach { a ->
        when (a) {
            is ByteArray -> {
                for (b in a) {
                    view[offset] = b
                    offset++
                }
            }
            is Uint8Array -> {
                view.set(a as Uint8Array, offset)
                offset += a.length
            }
            is ArrayBuffer -> {
                view.set(Uint8Array(a as ArrayBuffer), offset)
                offset += a.byteLength
            }
            else -> throw IllegalArgumentException()
        }
    }
    return view.buffer
}