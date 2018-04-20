package com.github.mitallast.ghost.client.groups

import com.github.mitallast.ghost.client.common.await
import com.github.mitallast.ghost.client.common.launch
import com.github.mitallast.ghost.client.crypto.AES
import com.github.mitallast.ghost.client.crypto.AESKey
import com.github.mitallast.ghost.client.crypto.HEX
import com.github.mitallast.ghost.client.persistent.*
import com.github.mitallast.ghost.common.codec.Codec
import com.github.mitallast.ghost.profile.UserProfile
import org.khronos.webgl.ArrayBuffer
import kotlin.js.Promise
import kotlin.js.json

class Group(
    val address: ByteArray,
    val secretKey: AESKey,
    val title: String
)

object GroupStore {
    private val db: Promise<IDBDatabase>

    private val min = ByteArray(16, { 0x00.toByte() })
    private val max = ByteArray(16, { 0xFF.toByte() })

    init {
        console.log("open groups db")
        val open = indexedDB.open("groups", 1)
        open.onupgradeneeded = { event ->
            val db = open.result
            if (event.oldVersion < 1) {
                // address: ByteArray
                // secretKey: ArrayBuffer
                // title: string
                db.createObjectStore("group")

                // group: ByteArray
                // profileId: ByteArray
                // profile: ByteArray
                db.createObjectStore("group.members", json(
                    Pair("keyPath", arrayOf("group", "profileId"))
                ))
            }
        }
        db = open.promise()
    }

    suspend fun cleanup() {
        console.log("cleanup groups")
        val d = db.await()
        d.close()
        indexedDB.deleteDatabase(d.name).await()
    }

    suspend fun create(group: Group) {
        console.log("create group")
        val row = js("{}")
        val secret = AES.exportKey(group.secretKey).await()
        row.address = group.address
        row.secretKey = secret
        row.title = group.title
        val tx = db.await().transaction("group", "readwrite")
        tx.objectStore("group").put(row, group.address).await()
        tx.await()
    }

    suspend fun delete(group: ByteArray) {
        console.log("delete group")
        val db = db.await()
        val tx = db.transaction(db.objectStoreNames, "readwrite")
        tx.objectStore("group").delete(group).await()
        val bound = IDBKeyRange.bound(
            arrayOf(group, min),
            arrayOf(group, max),
            false,
            false
        )
        tx.objectStore("group.members").delete(bound).await()
        tx.await()
    }

    suspend fun group(address: ByteArray): Group {
        console.log("load group", HEX.toHex(address))
        val tx = db.await().transaction("group", "readonly")
        val row = tx.objectStore("group").get<dynamic>(address).await()
        val secret = row.secretKey as ArrayBuffer
        val title = row.title as String
        val secretKey = AES.importKey(secret).await()
        val group = Group(address, secretKey, title)
        tx.await()
        return group
    }

    suspend fun groups(): List<Group> {
        console.log("load groups")
        val tx = db.await().transaction("group", "readonly")
        val store = tx.objectStore("group")
        val promise = Promise<List<Group>>({ resolve, reject ->
            val groups = ArrayList<Group>()
            val request = store.openCursor()
            request.onsuccess = {
                val cursor = request.result
                console.log(cursor)
                if (cursor == null) {
                    resolve.invoke(groups)
                } else {
                    launch {
                        val address = cursor.value.address as ByteArray
                        val secret = cursor.value.secretKey as ArrayBuffer
                        val title = cursor.value.title as String
                        val secretKey = AES.importKey(secret).await()
                        val group = Group(address, secretKey, title)
                        groups.add(group)
                    }
                    cursor.`continue`()
                }
            }
            request.onerror = {
                reject.invoke(it.target.error as Throwable)
            }
        })
        return promise.await()
    }

    suspend fun putMember(group: ByteArray, profile: UserProfile) {
        val tx = db.await().transaction("group.members", "readwrite")
        val row = js("{}")
        row.group = group
        row.profileId = profile.id
        row.profile = UserProfile.codec.write(profile)
        tx.objectStore("group.members").put(row).await()
        tx.await()
    }

    suspend fun removeMember(group: ByteArray, profileId: ByteArray) {
        val tx = db.await().transaction("group.members", "readwrite")
        tx.objectStore("group.members").delete(IDBKeyRange.only(arrayOf(group, profileId))).await()
        tx.await()
    }

    suspend fun members(group: ByteArray): List<UserProfile> {
        console.log("load members", HEX.toHex(group))
        val tx = db.await().transaction("group.members", "readonly")
        val store = tx.objectStore("group.members")
        console.log(HEX.toHex(min))
        console.log(HEX.toHex(max))
        val bound = IDBKeyRange.bound(
            arrayOf(group, min),
            arrayOf(group, max),
            false,
            false
        )
        val promise = Promise<List<UserProfile>>({ resolve, reject ->
            val members = ArrayList<UserProfile>()
            val request = store.openCursor(bound, "next")
            request.onsuccess = {
                val cursor = request.result
                console.log(cursor)
                if (cursor == null) {
                    resolve.invoke(members)
                } else {
                    val data = cursor.value.profile as ByteArray
                    val member = UserProfile.codec.read(data)
                    members.add(member)
                    cursor.`continue`()
                }
            }
            request.onerror = {
                reject.invoke(it.target.error as Throwable)
            }
        })

        return promise.await()
    }
}