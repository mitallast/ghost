package com.github.mitallast.ghost.client.updates

import com.github.mitallast.ghost.client.common.await
import com.github.mitallast.ghost.client.common.toByteArray
import com.github.mitallast.ghost.client.crypto.crypto
import com.github.mitallast.ghost.client.persistent.IDBDatabase
import com.github.mitallast.ghost.client.persistent.await
import com.github.mitallast.ghost.client.persistent.indexedDB
import com.github.mitallast.ghost.client.persistent.promise
import com.github.mitallast.ghost.common.codec.Codec
import com.github.mitallast.ghost.common.codec.CodecMessage
import com.github.mitallast.ghost.updates.SendUpdate
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Uint8Array
import kotlin.js.Promise
import kotlin.js.json

internal object UpdatesStore {
    private val db: Promise<IDBDatabase>

    init {
        val open = indexedDB.open("updates", 2)
        open.onupgradeneeded = { event ->
            val db = open.result
            if (event.oldVersion < 1) {
                db.createObjectStore("last")
            }
            if (event.oldVersion < 2) {
                db.createObjectStore("send")
            }
        }
        db = open.promise()
    }

    suspend fun cleanup() {
        console.log("cleanup updates")
        val d = db.await()
        d.close()
        indexedDB.deleteDatabase(d.name).await()
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

    suspend fun queue(address: ByteArray, message: CodecMessage): SendUpdate {
        val tx = db.await().transaction("send", "readwrite")
        val store = tx.objectStore("send")
        val key = Uint8Array(8)
        do {
            crypto.getRandomValues(key)
            val result = store.get<dynamic>(key.buffer).await()
        } while (result != null)
        store.put(json(
                Pair("address", address),
                Pair("message", Codec.anyCodec<CodecMessage>().write(message))
        ), key).await()
        tx.await()
        val rid = Codec.longCodec().read(toByteArray(key))
        return SendUpdate(rid, address, message)
    }

    suspend fun peek(): SendUpdate? {
        val tx = db.await().transaction("send")
        val store = tx.objectStore("send")
        val cursor = store.openCursor().await()
        tx.await()
        return if(cursor != null) {
            val key = cursor.key as ArrayBuffer
            val rid = Codec.longCodec().read(toByteArray(key))
            val address = cursor.value.address as ByteArray
            val message = cursor.value.message as ByteArray
            val decoded = Codec.anyCodec<CodecMessage>().read(message)
            SendUpdate(rid, address, decoded)
        }else{
            null
        }
    }

    suspend fun complete(randomId: Long) {
        val key = Codec.longCodec().write(randomId)
        val tx = db.await().transaction("send", "readwrite")
        tx.objectStore("send").delete(key).await()
        tx.await()
    }
}