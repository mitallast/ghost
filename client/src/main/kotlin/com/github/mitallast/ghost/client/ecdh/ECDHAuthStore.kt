package com.github.mitallast.ghost.client.ecdh

import com.github.mitallast.ghost.client.crypto.*
import com.github.mitallast.ghost.client.common.*
import com.github.mitallast.ghost.client.persistent.*
import org.khronos.webgl.ArrayBuffer
import kotlin.js.Promise

class ECDHAuth(
    val auth: ByteArray,
    val secretKey: AESKey,
    val publicKey: ECDSAPublicKey,
    val privateKey: ECDSAPrivateKey
)

internal object ECDHAuthStore {
    private val db: Promise<IDBDatabase>

    init {
        val open = indexedDB.open("ecdh", 1)
        open.onupgradeneeded = { event ->
            val db = open.result
            if (event.oldVersion < 1) {
                db.createObjectStore("auth")
                db.createObjectStore("secretKey")
                db.createObjectStore("publicKey")
                db.createObjectStore("privateKey")
            }
        }
        db = open.promise()
    }

    suspend fun cleanup() {
        val d = db.await()
        val tx = d.transaction(d.objectStoreNames, "readwrite")
        for(store in d.objectStoreNames) {
            tx.objectStore(store).delete(IDBKeyRange.bound(null, null)).await()
        }
        tx.await()
    }

    suspend fun storeAuth(auth: ECDHAuth) {
        val secretKey = AES.exportKey(auth.secretKey).await()
        val publicKey = ECDSA.exportPublicKey(auth.publicKey).await()
        val privateKey = ECDSA.exportPrivateKey(auth.privateKey).await()

        val stores = arrayOf("auth", "secretKey", "publicKey", "privateKey")
        val tx = db.await().transaction(stores, "readwrite")
        tx.objectStore("auth").put(auth.auth, "self").await()
        tx.objectStore("secretKey").put(secretKey, "self").await()
        tx.objectStore("publicKey").put(publicKey, "self").await()
        tx.objectStore("privateKey").put(privateKey, "self").await()
        tx.await()
    }

    suspend fun loadAuth(): ECDHAuth? {
        val stores = arrayOf("auth", "secretKey", "publicKey", "privateKey")
        val tx = db.await().transaction(stores)
        val auth = tx.objectStore("auth").get<ByteArray>("self").await()
        val secretKeyB = tx.objectStore("secretKey").get<ArrayBuffer>("self").await()
        val publicKeyB = tx.objectStore("publicKey").get<ArrayBuffer>("self").await()
        val privateKeyB = tx.objectStore("privateKey").get<ArrayBuffer>("self").await()
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
                ECDHAuth(auth, secretKey, publicKey, privateKey)
            }
        }
    }
}