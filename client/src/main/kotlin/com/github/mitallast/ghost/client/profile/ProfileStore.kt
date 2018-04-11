package com.github.mitallast.ghost.client.profile

import com.github.mitallast.ghost.client.common.await
import com.github.mitallast.ghost.client.common.toByteArray
import com.github.mitallast.ghost.client.persistent.*
import com.github.mitallast.ghost.profile.UserProfile
import org.khronos.webgl.ArrayBuffer
import kotlin.js.Promise

object ProfileStore {
    private val db: Promise<IDBDatabase>

    init {
        console.log("open profiles db")
        val open = indexedDB.open("profiles", 1)
        open.onupgradeneeded = { event ->
            val db = open.result
            if (event.oldVersion < 1) {
                db.createObjectStore("profiles")
            }
        }
        db = open.promise()
    }

    suspend fun cleanup() {
        console.log("cleanup profiles")
        val d = db.await()
        d.close()
        indexedDB.deleteDatabase(d.name).await()
    }

    suspend fun updateProfile(profile: UserProfile) {
        val tx = db.await().transaction("profiles", "readwrite")
        val buffer = UserProfile.codec.write(profile)
        tx.objectStore("profiles").put(buffer, profile.id).await()
        tx.await()
    }

    suspend fun loadProfile(id: ByteArray): UserProfile? {
        val tx = db.await().transaction("profiles", "readwrite")
        val buffer = tx.objectStore("profiles").get<ByteArray>(id).await()
        tx.await()
        return if (buffer != null) {
            UserProfile.codec.read(buffer)
        } else {
            null
        }
    }

    suspend fun loadAll(): List<UserProfile> {
        val tx = db.await().transaction("profiles", "readwrite")
        val buffers = tx.objectStore("profiles").getAll<ArrayBuffer>().await()
        tx.await()

        return buffers.toList().map { UserProfile.codec.read(toByteArray(it)) }
    }
}