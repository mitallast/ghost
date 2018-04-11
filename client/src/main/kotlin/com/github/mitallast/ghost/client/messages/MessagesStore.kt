package com.github.mitallast.ghost.client.messages

import com.github.mitallast.ghost.client.common.await
import com.github.mitallast.ghost.client.crypto.HEX
import com.github.mitallast.ghost.client.persistent.*
import com.github.mitallast.ghost.common.codec.Codec
import com.github.mitallast.ghost.message.Message
import kotlin.js.Promise
import kotlin.js.json

object MessagesStore {
    private val db: Promise<IDBDatabase>
    private val zero = Codec.longCodec().write(0)
    private val min = Codec.longCodec().write(Long.MIN_VALUE)
    private val max = Codec.longCodec().write(Long.MAX_VALUE)

    init {
        console.log("open messages db")
        val open = indexedDB.open("messages", 1)
        open.onupgradeneeded = { event ->
            val db = open.result
            if (event.oldVersion < 1) {
                // "dialog": ByteArray
                // "date": ByteArray
                // "rid": ByteArray
                // "message": ByteArray
                db.createObjectStore("messages", json(
                    Pair("keyPath", arrayOf("dialog", "date", "rid"))
                ))
            }
        }
        db = open.promise()
    }

    suspend fun cleanup() {
        console.log("cleanup messages")
        val d = db.await()
        d.close()
        indexedDB.deleteDatabase(d.name).await()
    }

    suspend fun put(dialog: ByteArray, message: Message) {
        val tx = db.await().transaction("messages", "readwrite")
        val store = tx.objectStore("messages")
        store.put(json(
            Pair("dialog", dialog),
            Pair("date", Codec.longCodec().write(message.date)),
            Pair("rid", Codec.longCodec().write(message.randomId)),
            Pair("message", Message.codec.write(message))
        ))
        tx.await()
    }

    suspend fun lastMessage(dialog: ByteArray): Message? {
        console.log("load last message", HEX.toHex(dialog))
        val tx = db.await().transaction("messages", "readonly")
        val store = tx.objectStore("messages")
        val bound = IDBKeyRange.bound(
            arrayOf(dialog, zero, min),
            arrayOf(dialog, max, max),
            false,
            false
        )
        val request = store.openCursor(bound, "prev")
        val cursor = request.await()
        return if (cursor == null) {
            null
        } else {
            val data = cursor.value.message as ByteArray
            Message.codec.read(data)
        }
    }

    suspend fun historyTop(dialog: ByteArray, limit: Int): ArrayList<Message> {
        console.log("load historyTop", HEX.toHex(dialog), limit)
        val tx = db.await().transaction("messages", "readonly")
        val store = tx.objectStore("messages")
        val bound = IDBKeyRange.bound(
            arrayOf(dialog, 0, min),
            arrayOf(dialog, max, max),
            false,
            false
        )
        val promise = Promise<ArrayList<Message>>({ resolve, reject ->
            val messages = ArrayList<Message>()
            val request = store.openCursor(bound, "prev")
            request.onsuccess = {
                val cursor = request.result
                if (cursor == null) {
                    resolve.invoke(messages)
                } else {
                    val data = cursor.value.message as ByteArray
                    val message = Message.codec.read(data)
                    messages.add(message)
                    if (messages.size < limit) {
                        cursor.`continue`()
                    } else {
                        resolve.invoke(messages)
                    }
                }
            }
            request.onerror = {
                reject.invoke(it.target.error as Throwable)
            }
        })

        return promise.await()
    }
}