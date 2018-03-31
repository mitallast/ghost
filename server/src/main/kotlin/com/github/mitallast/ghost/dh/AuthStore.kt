package com.github.mitallast.ghost.dh

import com.github.mitallast.ghost.persistent.PersistentService
import java.lang.IllegalArgumentException
import java.nio.ByteBuffer
import java.security.PublicKey
import java.util.*
import javax.crypto.SecretKey
import javax.inject.Inject

class Auth(
    val auth: ByteArray,
    internal val publicKey: PublicKey,
    internal val secretKey: SecretKey
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Auth

        if (!Arrays.equals(auth, other.auth)) return false

        return true
    }

    override fun hashCode(): Int {
        return Arrays.hashCode(auth)
    }
}

class AuthStore @Inject constructor(private val db: PersistentService) {
    private val aesCF = db.columnFamily("auth.AES".toByteArray())
    private val ecdsaCF = db.columnFamily("auth.ECDSA.public".toByteArray())

    fun generateAuthId(): ByteArray {
        val uuid = UUID.randomUUID()
        val buffer = ByteArray(16)
        ByteBuffer.wrap(buffer).asLongBuffer()
            .put(uuid.mostSignificantBits)
            .put(uuid.leastSignificantBits)
        return buffer
    }

    fun storeAES(auth: ByteArray, secretKey: SecretKey) {
        db.put(aesCF, auth, AES.exportKey(secretKey))
    }

    fun loadAES(auth: ByteArray): SecretKey {
        val data = db.get(aesCF, auth)
        return if (data == null) {
            throw IllegalArgumentException("auth not found")
        } else {
            AES.importKey(data)
        }
    }

    fun storePublicECDSA(auth: ByteArray, publicKey: PublicKey) {
        db.put(ecdsaCF, auth, ECDSA.exportPublicKey(publicKey))
    }

    fun loadPublicECDSA(auth: ByteArray): PublicKey {
        val data = db.get(ecdsaCF, auth)
        return if (data == null) {
            throw IllegalArgumentException("auth not found")
        } else {
            ECDSA.importPublicKey(data)
        }
    }
}