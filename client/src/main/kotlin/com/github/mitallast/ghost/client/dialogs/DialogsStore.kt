package com.github.mitallast.ghost.client.dialogs

import com.github.mitallast.ghost.client.common.await
import com.github.mitallast.ghost.client.persistent.IDBDatabase
import com.github.mitallast.ghost.client.persistent.await
import com.github.mitallast.ghost.client.persistent.indexedDB
import com.github.mitallast.ghost.client.persistent.promise
import kotlin.js.Promise

object DialogsStore {
    private val db: Promise<IDBDatabase>

    init {
        val open = indexedDB.open("dialogs", 1)
        open.onupgradeneeded = { event ->
            val db = open.result
            if (event.oldVersion < 1) {
                db.createObjectStore("dialogs")
            }
        }
        db = open.promise()
    }

    suspend fun add(auth: ByteArray) {
        val tx = db.await().transaction("dialogs", "readwrite")
        tx.objectStore("dialogs").put(auth, auth).await()
        tx.await()
    }

    suspend fun load(): List<ByteArray> {
        val tx = db.await().transaction("dialogs")
        val dialogs = tx.objectStore("dialogs").getAll<ByteArray>().await()
        tx.await()
        return dialogs.toList()
    }
}