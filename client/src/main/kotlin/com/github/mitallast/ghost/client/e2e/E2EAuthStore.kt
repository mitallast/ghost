package com.github.mitallast.ghost.client.e2e

import com.github.mitallast.ghost.client.crypto.*
import com.github.mitallast.ghost.client.common.*
import com.github.mitallast.ghost.client.html.a
import com.github.mitallast.ghost.client.persistent.*
import org.khronos.webgl.ArrayBuffer
import kotlin.js.Promise

class E2EAuth(
    val auth: ByteArray,
    val secretKey: AESKey,
    val publicKey: ECDSAPublicKey,
    val privateKey: ECDSAPrivateKey
)

class E2EAuthRequest(
    val auth: ByteArray,
    val ecdhPublicKey: ECDHPublicKey,
    val ecdhPrivateKey: ECDHPrivateKey,
    val ecdsaPublicKey: ECDSAPublicKey,
    val ecdsaPrivateKey: ECDSAPrivateKey
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

    suspend fun storeAuth(auth: E2EAuth) {
        val secretKey = AES.exportKey(auth.secretKey).await()
        val publicKey = ECDSA.exportPublicKey(auth.publicKey).await()
        val privateKey = ECDSA.exportPrivateKey(auth.privateKey).await()

        val stores = arrayOf("secretKey", "publicKey", "privateKey")
        val tx = db.await().transaction(stores, "readwrite")
        tx.objectStore("secretKey").put(secretKey, auth.auth).await()
        tx.objectStore("publicKey").put(publicKey, auth.auth).await()
        tx.objectStore("privateKey").put(privateKey, auth.auth).await()
        tx.await()
    }

    suspend fun loadAuth(auth: ByteArray): E2EAuth? {
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
                val publicKey = ECDSA.importPublicKey(CurveP521, publicKeyB).await()
                val privateKey = ECDSA.importPrivateKey(CurveP521, privateKeyB).await()
                E2EAuth(auth, secretKey, publicKey, privateKey)
            }
        }
    }

    suspend fun storeRequest(request: E2EAuthRequest) {
        val ecdhPublicKeyB = ECDH.exportPublicKey(request.ecdhPublicKey).await()
        val ecdhPrivateKeyB = ECDH.exportPrivateKey(request.ecdhPrivateKey).await()
        val ecdsaPublicKeyB = ECDSA.exportPublicKey(request.ecdsaPublicKey).await()
        val ecdsaPrivateKeyB = ECDSA.exportPrivateKey(request.ecdsaPrivateKey).await()

        val stores = arrayOf("ECDH.public", "ECDH.private", "ECDSA.public", "ECDSA.private")
        val tx = db.await().transaction(stores, "readwrite")
        tx.objectStore("ECDH.public").put(ecdhPublicKeyB, request.auth).await()
        tx.objectStore("ECDH.private").put(ecdhPrivateKeyB, request.auth).await()
        tx.objectStore("ECDSA.public").put(ecdsaPublicKeyB, request.auth).await()
        tx.objectStore("ECDSA.private").put(ecdsaPrivateKeyB, request.auth).await()
        tx.await()
    }

    suspend fun loadRequest(auth: ByteArray): E2EAuthRequest {
        val stores = arrayOf("ECDH.public", "ECDH.private", "ECDSA.public", "ECDSA.private")
        val tx = db.await().transaction(stores)
        val ecdhPublicKeyB = tx.objectStore("ECDH.public").get<ArrayBuffer>(auth).await()
        val ecdhPrivateKeyB = tx.objectStore("ECDH.private").get<ArrayBuffer>(auth).await()
        val ecdsaPublicKeyB = tx.objectStore("ECDSA.public").get<ArrayBuffer>(auth).await()
        val ecdsaPrivateKeyB = tx.objectStore("ECDSA.private").get<ArrayBuffer>(auth).await()
        tx.await()
        return when {
            ecdhPublicKeyB == null -> throw RuntimeException("public key not found")
            ecdhPrivateKeyB == null -> throw RuntimeException("private key not found")
            ecdsaPublicKeyB == null -> throw RuntimeException("private key not found")
            ecdsaPrivateKeyB == null -> throw RuntimeException("private key not found")
            else -> {
                val ecdhPublicKey = ECDH.importPublicKey(CurveP521, ecdhPublicKeyB).await()
                val ecdhPrivateKey = ECDH.importPrivateKey(CurveP521, ecdhPrivateKeyB).await()
                val ecdsaPublicKey = ECDSA.importPublicKey(CurveP521, ecdsaPublicKeyB).await()
                val ecdsaPrivateKey = ECDSA.importPrivateKey(CurveP521, ecdsaPrivateKeyB).await()
                E2EAuthRequest(auth, ecdhPublicKey, ecdhPrivateKey, ecdsaPublicKey, ecdsaPrivateKey)
            }
        }
    }

    suspend fun removeRequest(auth: ByteArray) {
        val stores = arrayOf("ECDSA.public", "ECDSA.private", "ECDH.public", "ECDH.private")
        val tx = db.await().transaction(stores, "readwrite")
        tx.objectStore("ECDSA.public").delete(auth).await()
        tx.objectStore("ECDSA.private").delete(auth).await()
        tx.objectStore("ECDH.public").delete(auth).await()
        tx.objectStore("ECDH.private").delete(auth).await()
        tx.await()
    }

    suspend fun loadRequests(): List<ByteArray> {
        val tx = db.await().transaction("ECDH.public")
        val keys = tx.objectStore("ECDH.public").getAllKeys<ArrayBuffer>().await()
        tx.await()
        return keys.map { toByteArray(it) }.toList()
    }
}