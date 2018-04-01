package com.github.mitallast.ghost.dh

import com.google.inject.Inject
import com.typesafe.config.Config
import org.bouncycastle.asn1.ASN1Integer
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.conscrypt.OpenSSLProvider
import java.lang.IllegalArgumentException
import java.lang.reflect.Field
import java.lang.reflect.Modifier
import java.math.BigInteger
import java.security.*
import java.security.spec.ECGenParameterSpec
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher
import javax.crypto.KeyAgreement
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec
import javax.xml.bind.DatatypeConverter.parseHexBinary

private object ECDHParams {
    val PROVIDER = BouncyCastleProvider.PROVIDER_NAME
    val ECC_KEY_TYPE = "EC"
    val ECC_CURVE = "secp521r1"
    val ECC_SIGNATURE = "SHA512withECDSA"
    val ECDH_AGREEMENT = "ECDH"
    val AES_GCM = "AES/GCM/NoPadding"
    val AES_GCM_TAG_LENGTH = 128
    val AES_GCM_IV_LENGTH = 12

    init {
        try {
            val newMaxKeyLength = Cipher.getMaxAllowedKeyLength("AES")
            if (newMaxKeyLength < 256) {
                var c = Class.forName("javax.crypto.CryptoAllPermissionCollection")
                var con = c.getDeclaredConstructor()
                con.isAccessible = true
                val allPermissionCollection = con.newInstance()
                var f = c.getDeclaredField("all_allowed")
                f.isAccessible = true
                f.setBoolean(allPermissionCollection, true)

                c = Class.forName("javax.crypto.CryptoPermissions")

                con = c.getDeclaredConstructor()
                con.isAccessible = true
                val allPermissions = con.newInstance()
                f = c.getDeclaredField("perms")
                f.isAccessible = true

                @Suppress("UNCHECKED_CAST")
                (f.get(allPermissions) as MutableMap<String, Any>).put("*", allPermissionCollection)

                c = Class.forName("javax.crypto.JceSecurityManager")
                f = c.getDeclaredField("defaultPolicy")
                f.isAccessible = true
                val mf = Field::class.java.getDeclaredField("modifiers")
                mf.isAccessible = true
                mf.setInt(f, f.modifiers and Modifier.FINAL.inv())
                f.set(null, allPermissions)
            }
        } catch (e: Exception) {
            throw RuntimeException("Failed manually overriding key-length permissions.", e)
        }

        if (Cipher.getMaxAllowedKeyLength("AES") < 256)
            throw RuntimeException("Failed manually overriding key-length permissions.") // hack failed
    }

    init {
        Security.addProvider(OpenSSLProvider(ECDHParams.PROVIDER))
        Security.addProvider(BouncyCastleProvider())
    }
}

class ECDHService @Inject constructor(
    config: Config,
    private val authStore: AuthStore
) {
    private val privateKeyBytes = parseHexBinary(config.getString("security.ecdsa.private"))
    private val privateKey = ECDSA.importPrivateKey(privateKeyBytes)

    fun ecdh(request: ECDHRequest): Pair<Auth, ECDHResponse> {
        val serverECHD = ECDH.generate()
        val clientECDHPublicKey = ECDH.importPublicKey(request.ecdhPublicKey)
        val clientECDSAPublicKey = ECDSA.importPublicKey(request.ecdsaPublicKey)
        val requestSignDER = ECDSA.raw2der(request.sign)
        if (!ECDSA.verify(clientECDSAPublicKey, requestSignDER, request.ecdhPublicKey, request.ecdsaPublicKey)) {
            throw IllegalArgumentException("not verified")
        }
        val secretKey = ECDH.deriveKey(serverECHD.private, clientECDHPublicKey)
        val encoded = serverECHD.public.encoded

        val authId = authStore.generateAuthId()
        authStore.storeAES(authId, secretKey)
        authStore.storePublicECDSA(authId, clientECDSAPublicKey)
        val auth = Auth(authId, clientECDSAPublicKey, secretKey)

        val sign = ECDSA.sign(privateKey, authId, encoded)
        val signRaw = ECDSA.der2raw(sign)
        val response = ECDHResponse(authId, encoded, signRaw)

        return Pair(auth, response)
    }

    fun reconnect(message: ECDHReconnect): Auth {
        val aes = authStore.loadAES(message.auth)
        val ecdsa = authStore.loadPublicECDSA(message.auth)

        val signDER = ECDSA.raw2der(message.sign)
        if (!ECDSA.verify(ecdsa, signDER, message.auth, ecdsa.encoded)) {
            throw IllegalArgumentException("not verified")
        }
        return Auth(message.auth, ecdsa, aes)
    }

    fun encrypt(auth: Auth, data: ByteArray): ECDHEncrypted {
        val aes = auth.secretKey

        val iv = AES.iv()
        val encrypted = AES.encrypt(aes, iv, data)
        val sign = ECDSA.sign(privateKey, auth.auth, iv, data)
        val signRaw = ECDSA.der2raw(sign)

        return ECDHEncrypted(auth.auth, signRaw, iv, encrypted)
    }

    fun decrypt(auth: Auth, encrypted: ECDHEncrypted): ByteArray {
        val aes = auth.secretKey
        val ecdsa = auth.publicKey

        val decrypted = AES.decrypt(aes, encrypted.iv, encrypted.encrypted)
        val signDER = ECDSA.raw2der(encrypted.sign)
        if (!ECDSA.verify(ecdsa, signDER, auth.auth, encrypted.iv, decrypted)) {
            throw IllegalArgumentException("not verified")
        }
        return decrypted
    }
}

object HASH {
    fun sha256(vararg data: ByteArray): ByteArray {
        val hash = MessageDigest.getInstance("SHA-256", ECDHParams.PROVIDER)
        data.forEach { hash.update(it) }
        return hash.digest()
    }

    fun sha512(vararg data: ByteArray): ByteArray {
        val hash = MessageDigest.getInstance("SHA-512", ECDHParams.PROVIDER)
        data.forEach { hash.update(it) }
        return hash.digest()
    }
}

object ECDSA {
    fun generate(): KeyPair {
        val generator = KeyPairGenerator.getInstance(ECDHParams.ECC_KEY_TYPE, ECDHParams.PROVIDER)
        val spec = ECGenParameterSpec(ECDHParams.ECC_CURVE)
        generator.initialize(spec, SecureRandom())
        return generator.genKeyPair()
    }

    fun importPublicKey(publicKeyBytes: ByteArray): PublicKey {
        val publicKeySpec = X509EncodedKeySpec(publicKeyBytes)
        val kf = KeyFactory.getInstance(ECDHParams.ECC_KEY_TYPE, ECDHParams.PROVIDER)
        return kf.generatePublic(publicKeySpec)
    }

    fun importPrivateKey(privateKeyBytes: ByteArray): PrivateKey {
        val publicKeySpec = PKCS8EncodedKeySpec(privateKeyBytes)
        val kf = KeyFactory.getInstance(ECDHParams.ECC_KEY_TYPE, ECDHParams.PROVIDER)
        return kf.generatePrivate(publicKeySpec)
    }

    fun exportPublicKey(publicKey: PublicKey): ByteArray {
        return publicKey.encoded
    }

    fun exportPrivateKey(privateKey: PrivateKey): ByteArray {
        return privateKey.encoded
    }

    fun sign(privateKey: PrivateKey, vararg data: ByteArray): ByteArray {
        val signSignature = Signature.getInstance(ECDHParams.ECC_SIGNATURE, ECDHParams.PROVIDER)
        signSignature.initSign(privateKey, SecureRandom())
        data.forEach { signSignature.update(it) }
        return signSignature.sign()
    }

    fun verify(publicKey: PublicKey, sign: ByteArray, vararg data: ByteArray): Boolean {
        val signSignature = Signature.getInstance(ECDHParams.ECC_SIGNATURE, ECDHParams.PROVIDER)
        signSignature.initVerify(publicKey)
        data.forEach { signSignature.update(it) }
        return signSignature.verify(sign)
    }

    /**
     * 0000000 b6 07 b4 29 ed 5d e1 92 53 4d a2 0d f8 6f 3b f6
     * 0000010 a5 b8 bb d6 48 7d 86 2f 09 23 68 62 3a 67 6e 0e
     * 0000020
     *
     * @url https://stackoverflow.com/questions/45470941/create-asn-1-from-two-big-integers
     * @url https://tools.ietf.org/html/rfc3278#section-8.2
     */
    fun raw2der(sign: ByteArray): ByteArray {
        require(sign.size % 2 == 0)
        val m = sign.size
        val n = sign.size shr 1
        val r = BigInteger(1, sign.copyOfRange(0, n))
        val s = BigInteger(1, sign.copyOfRange(n, m))
        val ra = ASN1Integer(r)
        val sa = ASN1Integer(s)
        return DERSequence(arrayOf(ra, sa)).encoded
    }

    /**
     * 0000000 30 45 02 21 00 e0 79 4e c4 74 f0 66 76 fe cb cd
     * 0000010 ad 02 b5 4e 56 4d 8f 39 64 14 0a 8d 75 78 96 6a
     * 0000020 1d a3 87 b0 f7 02 20 69 da dd bd 23 af 91 61 b3
     * 0000030 5a 68 e3 5d 6f 3d e4 c6 d8 f3 6e 01 92 1a 1e 6f
     * 0000040 1a 03 2f 41 7b 07 2b
     * 0000047
     */
    fun der2raw(sign: ByteArray): ByteArray {
        val sequence = DERSequence.fromByteArray(sign) as ASN1Sequence
        val r = sequence.getObjectAt(0) as ASN1Integer
        val s = sequence.getObjectAt(1) as ASN1Integer

        val rb = r.value.toByteArray()
        val sb = s.value.toByteArray()
        val m = 132 // max for secp521r1
        val n = m shr 1

        val rl = Math.min(n, rb.size)
        val sl = Math.min(n, sb.size)
        val ro = Math.max(0, rb.size - n)
        val so = Math.max(0, sb.size - n)

        val raw = ByteArray(m)
        System.arraycopy(rb, ro, raw, n - rl, rl)
        System.arraycopy(sb, so, raw, m - sl, sl)
        return raw
    }
}

object ECDH {
    fun generate(): KeyPair {
        val generator = KeyPairGenerator.getInstance(ECDHParams.ECC_KEY_TYPE, ECDHParams.PROVIDER)
        val spec = ECGenParameterSpec(ECDHParams.ECC_CURVE)
        generator.initialize(spec, SecureRandom())
        return generator.genKeyPair()
    }

    fun importPublicKey(publicKeyBytes: ByteArray): PublicKey {
        val publicKeySpec = X509EncodedKeySpec(publicKeyBytes)
        val kf = KeyFactory.getInstance(ECDHParams.ECC_KEY_TYPE, ECDHParams.PROVIDER)
        return kf.generatePublic(publicKeySpec)
    }

    fun exportPublicKey(publicKey: PublicKey): ByteArray {
        return publicKey.encoded
    }

    fun deriveKey(privateKey: PrivateKey, publicKey: PublicKey): SecretKey {
        val ka = KeyAgreement.getInstance(ECDHParams.ECDH_AGREEMENT, ECDHParams.PROVIDER)
        ka.init(privateKey)
        ka.doPhase(publicKey, true)
        // 528 = 66 bytes
        val secret = ka.generateSecret()
        // use SHA-256 as KDF function
        val hash = HASH.sha256(secret)
        return AES.importKey(hash)
    }
}

object AES {
    fun iv(): ByteArray {
        val iv = ByteArray(ECDHParams.AES_GCM_IV_LENGTH)
        SecureRandom.getInstanceStrong().nextBytes(iv)
        return iv
    }

    fun encrypt(secretKey: SecretKey, iv: ByteArray, data: ByteArray): ByteArray {
        val spec = GCMParameterSpec(ECDHParams.AES_GCM_TAG_LENGTH, iv)
        val cipher = Cipher.getInstance(ECDHParams.AES_GCM, ECDHParams.PROVIDER)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, spec)
        return cipher.doFinal(data)
    }

    fun decrypt(secretKey: SecretKey, iv: ByteArray, data: ByteArray): ByteArray {
        val spec = GCMParameterSpec(ECDHParams.AES_GCM_TAG_LENGTH, iv)
        val cipher = Cipher.getInstance(ECDHParams.AES_GCM, ECDHParams.PROVIDER)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)
        return cipher.doFinal(data)
    }

    fun importKey(data: ByteArray): SecretKey {
        return SecretKeySpec(data, "AES")
    }

    fun exportKey(secretKey: SecretKey): ByteArray {
        return secretKey.encoded
    }
}