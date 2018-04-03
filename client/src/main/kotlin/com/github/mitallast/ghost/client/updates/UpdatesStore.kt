package com.github.mitallast.ghost.client.updates

import com.github.mitallast.ghost.client.common.await
import com.github.mitallast.ghost.client.persistent.IDBDatabase
import com.github.mitallast.ghost.client.persistent.await
import com.github.mitallast.ghost.client.persistent.indexedDB
import com.github.mitallast.ghost.client.persistent.promise
import com.github.mitallast.ghost.common.codec.Codec
import kotlin.js.Promise

internal object UpdatesStore {
    private val db: Promise<IDBDatabase>

    init {
        val open = indexedDB.open("updates", 1)
        open.onupgradeneeded = { event ->
            val db = open.result
            if (event.oldVersion < 1) {
                db.createObjectStore("last")
            }
        }
        db = open.promise()
    }

    suspend fun updateLastInstalled(sequence: Long) {
        console.log("update last installed", sequence)
        val buffer = Codec.longCodec().write(sequence)
        val tx = db.await().transaction("last", "readwrite")
        tx.objectStore("last").put(buffer, "self").await()
        tx.await()
    }

    suspend fun loadLastInstalled(): Long {
        val tx = db.await().transaction("last")
        val buffer = tx.objectStore("last").get<ByteArray>("self").await()
        tx.await()
        return if (buffer != null) {
            Codec.longCodec().read(buffer)
        } else {
            0L
        }
    }
}