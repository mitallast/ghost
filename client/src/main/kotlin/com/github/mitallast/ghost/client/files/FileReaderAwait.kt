package com.github.mitallast.ghost.client.files

import org.w3c.files.FileReader
import kotlin.coroutines.experimental.suspendCoroutine

suspend fun <T> FileReader.await(): T = suspendCoroutine { cont ->
    onload = { cont.resume(result as T) }
    onerror = {
        console.error("error file read", it)
        cont.resumeWithException(Exception(it.toString()))
    }
}