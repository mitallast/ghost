package com.github.mitallast.ghost.client.e2e

import com.github.mitallast.ghost.client.common.await
import com.github.mitallast.ghost.client.common.toByteArray
import com.github.mitallast.ghost.client.crypto.*
import com.github.mitallast.ghost.client.persistent.*
import org.khronos.webgl.ArrayBuffer
import kotlin.js.Promise

class E2EAuth(
        val address: ByteArray,
        val secretKey: AESKey,
        val publicKey: ECDSAPublicKey,
        val privateKey: ECDSAPrivateKey
)

object E2EAuthStore {
    private val db: Promise<IDBDatabase>

    init {
        val open = indexedDB.open("e2e", 1)
        open.onupgradeneeded = { event ->
            val db = open.result
            if (event.oldVersion < 1) {
                db.createObjectStore("secretKey")
                db.createObjectStore("publicKey")
                db.createObjectStore("privateKey")

                db.createObjectStore("ECDH.public")
                db.createObjectStore("ECDH.private")
                db.createObjectStore("ECDSA.public")
                db.createObjectStore("ECDSA.private")
            }
        }
        db = open.promise()
    }

    suspend fun cleanup() {
        console.log("cleanup e2e")
        val d = db.await()
        d.close()
        indexedDB.deleteDatabase(d.name).await()
    }

    suspend fun store(auth: E2EAuth) {
        val secretKey = AES.exportKey(auth.secretKey).await()
        val publicKey = ECDSA.exportPublicKey(auth.publicKey).await()
        val privateKey = ECDSA.exportPrivateKey(auth.privateKey).await()

        val stores = arrayOf("secretKey", "publicKey", "privateKey")
        val tx = db.await().transaction(stores, "readwrite")
        tx.objectStore("secretKey").put(secretKey, auth.address).await()
        tx.objectStore("publicKey").put(publicKey, auth.address).await()
        tx.objectStore("privateKey").put(privateKey, auth.address).await()
        tx.await()
    }

    suspend fun load(auth: ByteArray): E2EAuth? {
        val stores = arrayOf("secretKey", "publicKey", "privateKey")
        val tx = db.await().transaction(stores)
        val secretKeyB = tx.objectStore("secretKey").get<ArrayBuffer>(auth).await()
        val publicKeyB = tx.objectStore("publicKey").get<ArrayBuffer>(auth).await()
        val privateKeyB = tx.objectStore("privateKey").get<ArrayBuffer>(auth).await()
        tx.await()
        return when {
            secretKeyB == null -> null
            publicKeyB == null -> null
            privateKeyB == null -> null
            else -> {
                val secretKey = AES.importKey(secretKeyB).await()
                val publicKey = ECDSA.importPublicKey(CurveP384, publicKeyB).await()
                val privateKey = ECDSA.importPrivateKey(CurveP384, privateKeyB).await()
                E2EAuth(auth, secretKey, publicKey, privateKey)
            }
        }
    }
}

class E2EOutgoingRequest(
        val address: ByteArray,
        val ecdhPublicKey: ECDHPublicKey,
        val ecdhPrivateKey: ECDHPrivateKey,
        val ecdsaPublicKey: ECDSAPublicKey,
        val ecdsaPrivateKey: ECDSAPrivateKey
)

object E2EOutgoingRequestStore {
    private val db: Promise<IDBDatabase>

    init {
        val open = indexedDB.open("e2e.outgoing", 1)
        open.onupgradeneeded = { event ->
            val db = open.result
            if (event.oldVersion < 1) {
                db.createObjectStore("ECDH.public")
                db.createObjectStore("ECDH.private")
                db.createObjectStore("ECDSA.public")
                db.createObjectStore("ECDSA.private")
            }
        }
        db = open.promise()
    }

    suspend fun cleanup() {
        console.log("cleanup e2e.outgoing")
        val d = db.await()
        d.close()
        indexedDB.deleteDatabase(d.name).await()
    }

    private suspend fun tx(mode: String = "readonly"): IDBTransaction {
        val db = db.await()
        return db.transaction(db.objectStoreNames, mode)
    }

    suspend fun store(request: E2EOutgoingRequest) {
        val ecdhPublicKeyB = ECDH.exportPublicKey(request.ecdhPublicKey).await()
        val ecdhPrivateKeyB = ECDH.exportPrivateKey(request.ecdhPrivateKey).await()
        val ecdsaPublicKeyB = ECDSA.exportPublicKey(request.ecdsaPublicKey).await()
        val ecdsaPrivateKeyB = ECDSA.exportPrivateKey(request.ecdsaPrivateKey).await()

        val tx = tx("readwrite")
        tx.objectStore("ECDH.public").put(ecdhPublicKeyB, request.address).await()
        tx.objectStore("ECDH.private").put(ecdhPrivateKeyB, request.address).await()
        tx.objectStore("ECDSA.public").put(ecdsaPublicKeyB, request.address).await()
        tx.objectStore("ECDSA.private").put(ecdsaPrivateKeyB, request.address).await()
        tx.await()
    }

    suspend fun load(address: ByteArray): E2EOutgoingRequest? {
        val tx = tx()
        val ecdhPublicKeyB = tx.objectStore("ECDH.public").get<ArrayBuffer>(address).await()
        val ecdhPrivateKeyB = tx.objectStore("ECDH.private").get<ArrayBuffer>(address).await()
        val ecdsaPublicKeyB = tx.objectStore("ECDSA.public").get<ArrayBuffer>(address).await()
        val ecdsaPrivateKeyB = tx.objectStore("ECDSA.private").get<ArrayBuffer>(address).await()
        tx.await()
        return when {
            ecdhPublicKeyB == null -> null
            ecdhPrivateKeyB == null -> null
            ecdsaPublicKeyB == null -> null
            ecdsaPrivateKeyB == null -> null
            else -> {
                val ecdhPublicKey = ECDH.importPublicKey(CurveP384, ecdhPublicKeyB).await()
                val ecdhPrivateKey = ECDH.importPrivateKey(CurveP384, ecdhPrivateKeyB).await()
                val ecdsaPublicKey = ECDSA.importPublicKey(CurveP384, ecdsaPublicKeyB).await()
                val ecdsaPrivateKey = ECDSA.importPrivateKey(CurveP384, ecdsaPrivateKeyB).await()
                E2EOutgoingRequest(address, ecdhPublicKey, ecdhPrivateKey, ecdsaPublicKey, ecdsaPrivateKey)
            }
        }
    }

    suspend fun remove(address: ByteArray) {
        val tx = tx("readwrite")
        tx.objectStore("ECDSA.public").delete(address).await()
        tx.objectStore("ECDSA.private").delete(address).await()
        tx.objectStore("ECDH.public").delete(address).await()
        tx.objectStore("ECDH.private").delete(address).await()
        tx.await()
    }

    suspend fun list(): List<ByteArray> {
        val tx = db.await().transaction("ECDH.public")
        val keys = tx.objectStore("ECDH.public").getAllKeys<ArrayBuffer>().await()
        tx.await()
        return keys.map { toByteArray(it) }.toList()
    }
}

class E2EIncomingRequest(
        val address: ByteArray,
        val ecdhPublicKey: ECDHPublicKey,
        val ecdsaPublicKey: ECDSAPublicKey
)

object E2EIncomingRequestStore {
    private val db: Promise<IDBDatabase>

    init {
        val open = indexedDB.open("e2e.incoming", 1)
        open.onupgradeneeded = { event ->
            val db = open.result
            if (event.oldVersion < 1) {
                db.createObjectStore("ECDH.public")
                db.createObjectStore("ECDSA.public")
            }
        }
        db = open.promise()
    }

    suspend fun cleanup() {
        console.log("cleanup e2e.incoming")
        val d = db.await()
        d.close()
        indexedDB.deleteDatabase(d.name).await()
    }

    private suspend fun tx(mode: String = "readonly"): IDBTransaction {
        val db = db.await()
        return db.transaction(db.objectStoreNames, mode)
    }

    suspend fun store(request: E2EIncomingRequest) {
        val ecdhPublicKeyB = ECDH.exportPublicKey(request.ecdhPublicKey).await()
        val ecdsaPublicKeyB = ECDSA.exportPublicKey(request.ecdsaPublicKey).await()

        val tx = tx("readwrite")
        tx.objectStore("ECDH.public").put(ecdhPublicKeyB, request.address).await()
        tx.objectStore("ECDSA.public").put(ecdsaPublicKeyB, request.address).await()
        tx.await()
    }

    suspend fun load(address: ByteArray): E2EIncomingRequest? {
        val tx = tx()
        val ecdhPublicKeyB = tx.objectStore("ECDH.public").get<ArrayBuffer>(address).await()
        val ecdsaPublicKeyB = tx.objectStore("ECDSA.public").get<ArrayBuffer>(address).await()
        tx.await()
        return when {
            ecdhPublicKeyB == null -> null
            ecdsaPublicKeyB == null -> null
            else -> {
                val ecdhPublicKey = ECDH.importPublicKey(CurveP384, ecdhPublicKeyB).await()
                val ecdsaPublicKey = ECDSA.importPublicKey(CurveP384, ecdsaPublicKeyB).await()
                E2EIncomingRequest(address, ecdhPublicKey, ecdsaPublicKey)
            }
        }
    }

    suspend fun remove(address: ByteArray) {
        val tx = tx("readwrite")
        tx.objectStore("ECDSA.public").delete(address).await()
        tx.objectStore("ECDH.public").delete(address).await()
        tx.await()
    }

    suspend fun list(): List<ByteArray> {
        val tx = db.await().transaction("ECDH.public")
        val keys = tx.objectStore("ECDH.public").getAllKeys<ArrayBuffer>().await()
        tx.await()
        return keys.map { toByteArray(it) }.toList()
    }
}

class E2EResponse(
        val address: ByteArray,
        val ecdhPublicKey: ECDHPublicKey,
        val ecdsaPublicKey: ECDSAPublicKey
)

object E2EResponseStore {
    private val db: Promise<IDBDatabase>

    init {
        val open = indexedDB.open("e2e.response", 1)
        open.onupgradeneeded = { event ->
            val db = open.result
            if (event.oldVersion < 1) {
                db.createObjectStore("ECDH.public")
                db.createObjectStore("ECDSA.public")
            }
        }
        db = open.promise()
    }

    suspend fun cleanup() {
        console.log("cleanup e2e.response")
        val d = db.await()
        d.close()
        indexedDB.deleteDatabase(d.name).await()
    }

    private suspend fun tx(mode: String = "readonly"): IDBTransaction {
        val db = db.await()
        return db.transaction(db.objectStoreNames, mode)
    }

    suspend fun store(request: E2EResponse) {
        val ecdhPublicKeyB = ECDH.exportPublicKey(request.ecdhPublicKey).await()
        val ecdsaPublicKeyB = ECDSA.exportPublicKey(request.ecdsaPublicKey).await()

        val tx = tx("readwrite")
        tx.objectStore("ECDH.public").put(ecdhPublicKeyB, request.address).await()
        tx.objectStore("ECDSA.public").put(ecdsaPublicKeyB, request.address).await()
        tx.await()
    }

    suspend fun load(address: ByteArray): E2EResponse? {
        val tx = tx()
        val ecdhPublicKeyB = tx.objectStore("ECDH.public").get<ArrayBuffer>(address).await()
        val ecdsaPublicKeyB = tx.objectStore("ECDSA.public").get<ArrayBuffer>(address).await()
        tx.await()
        return when {
            ecdhPublicKeyB == null -> null
            ecdsaPublicKeyB == null -> null
            else -> {
                val ecdhPublicKey = ECDH.importPublicKey(CurveP384, ecdhPublicKeyB).await()
                val ecdsaPublicKey = ECDSA.importPublicKey(CurveP384, ecdsaPublicKeyB).await()
                E2EResponse(address, ecdhPublicKey, ecdsaPublicKey)
            }
        }
    }

    suspend fun remove(address: ByteArray) {
        val tx = tx("readwrite")
        tx.objectStore("ECDSA.public").delete(address).await()
        tx.objectStore("ECDH.public").delete(address).await()
        tx.await()
    }

    suspend fun list(): List<ByteArray> {
        val tx = db.await().transaction("ECDH.public")
        val keys = tx.objectStore("ECDH.public").getAllKeys<ArrayBuffer>().await()
        tx.await()
        return keys.map { toByteArray(it) }.toList()
    }
}