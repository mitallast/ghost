package com.github.mitallast.ghost.dh

import com.github.mitallast.ghost.client.crypto.*
import com.github.mitallast.ghost.client.persistent.IDBDatabase
import com.github.mitallast.ghost.client.persistent.await
import com.github.mitallast.ghost.client.persistent.indexedDB
import com.github.mitallast.ghost.client.persistent.promise
import org.khronos.webgl.ArrayBuffer
import kotlin.js.Promise

class Auth(
    val auth: ByteArray,
    val secretKey: AESKey,
    val publicKey: ECDSAPublicKey,
    val privateKey: ECDSAPrivateKey
)

object AuthStore {
    private val db: Promise<IDBDatabase>

    init {
        val open = indexedDB.open("auth", 1)
        open.onupgradeneeded = { event ->
            val db = open.result
            if (event.oldVersion < 1) {
                db.createObjectStore("auth")
                db.createObjectStore("auth.AES")
                db.createObjectStore("auth.ECDSA.public")
                db.createObjectStore("auth.ECDSA.private")
            }
        }
        db = open.promise()
    }

    suspend fun storeAuth(auth: Auth) {
        val secretKey = AES.exportKey(auth.secretKey).await()
        val publicKey = ECDSA.exportPublicKey(auth.publicKey).await()
        val privateKey = ECDSA.exportPrivateKey(auth.privateKey).await()

        val stores = arrayOf("auth", "auth.AES", "auth.ECDSA.public", "auth.ECDSA.private")
        val tx = db.await().transaction(stores, "readwrite")
        tx.objectStore("auth").put(auth.auth, "self").await()
        tx.objectStore("auth.AES").put(secretKey, "self").await()
        tx.objectStore("auth.ECDSA.public").put(publicKey, "self").await()
        tx.objectStore("auth.ECDSA.private").put(privateKey, "self").await()
        tx.await()
    }

    suspend fun loadAuth(): Auth? {
        val stores = arrayOf("auth", "auth.AES", "auth.ECDSA.public", "auth.ECDSA.private")
        val tx = db.await().transaction(stores)
        val auth = tx.objectStore("auth").get<ByteArray>("self").await()
        val secretKeyB = tx.objectStore("auth.AES").get<ArrayBuffer>("self").await()
        val publicKeyB = tx.objectStore("auth.ECDSA.public").get<ArrayBuffer>("self").await()
        val privateKeyB = tx.objectStore("auth.ECDSA.private").get<ArrayBuffer>("self").await()
        tx.await()
        return when {
            auth == null -> null
            secretKeyB == null -> throw RuntimeException("secret key not found")
            publicKeyB == null -> throw RuntimeException("public key not found")
            privateKeyB == null -> throw RuntimeException("private key not found")
            else -> {
                val secretKey = AES.importKey(secretKeyB).await()
                val publicKey = ECDSA.importPublicKey(CurveP521, publicKeyB).await()
                val privateKey = ECDSA.importPrivateKey(CurveP521, privateKeyB).await()
                Auth(auth, secretKey, publicKey, privateKey)
            }
        }
    }

    suspend fun storeAES(auth: ByteArray, key: AESKey) {
        val exported = AES.exportKey(key).await()
        val tx = db.await().transaction("auth.AES", "readwrite")
        tx.objectStore("auth.AES").put(auth, exported).await()
        tx.await()
    }

    suspend fun loadAES(auth: ByteArray): AESKey? {
        val tx = db.await().transaction("auth.AES")
        val exported = tx.objectStore("auth.AES").get<ArrayBuffer>(auth).await()
        tx.await()
        if (exported == null) {
            throw RuntimeException("aes key not found")
        } else {
            return AES.importKey(exported).await()
        }
    }

    suspend fun storeECDSAPublic(auth: ByteArray, key: ECDSAPublicKey) {
        val exported = ECDSA.exportPublicKey(key).await()
        val tx = db.await().transaction("auth.ECDSA.public", "readwrite")
        tx.objectStore("auth.ECDSA.public").put(exported, auth).await()
        tx.await()
    }

    suspend fun loadECDSAPublic(auth: ByteArray): ECDSAPublicKey {
        val tx = db.await().transaction("auth.ECDSA.public", "readwrite")
        val exported = tx.objectStore("auth.ECDSA.public").get<ArrayBuffer>(auth).await()
        tx.await()
        if (exported == null) {
            throw RuntimeException("aes key not found")
        } else {
            return ECDSA.importPublicKey(CurveP521, exported).await()
        }
    }

    suspend fun storeECDSAPrivate(auth: ByteArray, key: ECDSAPrivateKey) {
        val exported = ECDSA.exportPrivateKey(key).await()
        val tx = db.await().transaction("auth.ECDSA.private", "readwrite")
        tx.objectStore("auth.ECDSA.public").put(exported, auth).await()
        tx.await()
    }

    suspend fun loadECDSAPrivate(auth: ByteArray): ECDSAPrivateKey {
        val tx = db.await().transaction("auth.ECDSA.private", "readwrite")
        val exported = tx.objectStore("auth.ECDSA.private").get<ArrayBuffer>(auth).await()
        tx.await()
        if (exported == null) {
            throw RuntimeException("aes key not found")
        } else {
            return ECDSA.importPrivateKey(CurveP521, exported).await()
        }
    }
}