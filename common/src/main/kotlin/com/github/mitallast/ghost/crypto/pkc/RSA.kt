package com.github.mitallast.ghost.crypto.pkc

import com.github.mitallast.ghost.crypto.chf.MD5
import com.github.mitallast.ghost.crypto.chf.SHA1
import com.github.mitallast.ghost.crypto.chf.SHA256
import com.github.mitallast.ghost.crypto.chf.SHA512
import com.github.mitallast.ghost.crypto.utils.Arrays
import com.github.mitallast.ghost.crypto.utils.BigInteger
import com.github.mitallast.ghost.crypto.utils.CryptoRandom
import com.github.mitallast.ghost.crypto.utils.Keys

object RSA {

    enum class RSAES {
        OAEP,
        PKCS1
    }

    enum class SIGH(SIGH_PREFIX: ByteArray) {
        MD5(MD5_PREFIX),
        SHA1(SHA160_PREFIX),
        SHA256(SHA256_PREFIX),
        SHA512(SHA512_PREFIX);

        val prefix = SIGH_PREFIX
    }

    fun keyPair(keySize: Int): RSAKeyPair {

        val primes = choosePrimes(keySize)
        val p = primes[0]
        val q = primes[1]
        val n = p.multiply(q)
        val phi = lcm(p.subtract(BigInteger.ONE), q.subtract(BigInteger.ONE))
        val e = chooseExponent(keySize, phi)
        val d = e.modInverse(phi)
        val exp1 = d.mod(p.subtract(BigInteger.ONE))
        val exp2 = d.mod(q.subtract(BigInteger.ONE))
        val coe = q.modInverse(p)

        return RSAKeyPair(RSAPublicKey(n, e), RSAPrivateKey(n, e, d, p, q, exp1, exp2, coe, phi))
    }

    fun encrypt(plainText: ByteArray, publicKey: RSAPublicKey, es: RSAES = RSAES.PKCS1): ByteArray {

        if (RSAES.PKCS1 == es) {
            return encryptPKCS1(plainText, publicKey)
        }
        throw IllegalArgumentException("Encryption scheme is not supported")
    }

    fun decrypt(encrypted: ByteArray, privateKey: RSAPrivateKey, es: RSAES = RSAES.PKCS1): ByteArray {

        if (RSAES.PKCS1 == es) {
            return decryptPKCS1(encrypted, privateKey)
        }
        throw IllegalArgumentException("Decryption scheme is not supported")
    }

    fun sign(message: ByteArray, privateKey: RSAPrivateKey, sigh: SIGH = SIGH.SHA256): ByteArray {

        val em = emsaPkcs1Encode(message, privateKey.k, sigh)
        val hm = os2ip(em)
        val sm = rsasp1(privateKey, hm)
        return i2osp(sm, privateKey.k)
    }

    fun verify(message: ByteArray, signed: ByteArray, publicKey: RSAPublicKey, sigh: SIGH = SIGH.SHA256): Boolean {

        val s = os2ip(signed)
        val m = rsavp1(publicKey, s)
        val em = i2osp(m, publicKey.k)
        val empkcs = emsaPkcs1Encode(message, publicKey.k, sigh)
        return Arrays.equals(em, empkcs)
    }

    private fun encryptPKCS1(message: ByteArray, publicKey: RSAPublicKey): ByteArray {

        if (message.size > publicKey.k - 11) {
            throw IllegalArgumentException("message to long")
        }

        val padding = padding(publicKey.k - message.size - 3)
        val encoded = encode(padding, message)
        val bi = os2ip(encoded)
        val encrypted = encryptBlock(bi, publicKey)

        return i2osp(encrypted, publicKey.k)
    }

    private fun padding(size: Int): ByteArray {
        val padding = ByteArray(size)
        val rnd = CryptoRandom()
        rnd.nextBytes(padding)
        val zero = 0.toByte()
        for(i in padding.indices) {
            while (padding[i] == zero) {
                padding[i] = rnd.nextInt().toByte()
            }
        }
        return padding
    }

    private fun decryptPKCS1(encrypted: ByteArray, privateKey: RSAPrivateKey): ByteArray {

        if (encrypted.size != privateKey.k || privateKey.k < 11) {
            throw IllegalArgumentException("decryption error")
        }

        val c = os2ip(encrypted)
        val pm = decryptBlock(c, privateKey)
        val m = i2osp(pm, privateKey.k)
        return decode(m)
    }

    private fun choosePrimes(keySize: Int): Array<BigInteger> {

        val primes = Array(2, { BigInteger.ZERO })
        val rnd = CryptoRandom()
        val p = BigInteger.probablePrime(keySize, rnd)
        var q = BigInteger.probablePrime(keySize, rnd)
        while (p == q) {
            q = BigInteger.probablePrime(keySize, rnd)
        }

        if (p.subtract(q) < BigInteger.ZERO) {
            primes[0] = q
            primes[1] = p
        } else {
            primes[0] = p
            primes[1] = q
        }

        return primes
    }

    private fun chooseExponent(keySize: Int, phi: BigInteger): BigInteger {
        var exp = BigInteger.probablePrime(keySize / 10, CryptoRandom())
        while (phi.subtract(exp) < BigInteger.ZERO && exp.gcd(phi) != BigInteger.ZERO) {
            exp = exp.nextProbablePrime()
        }
        return exp
    }

    private fun lcm(a: BigInteger, b: BigInteger): BigInteger {
        return (a.multiply(b).divide(a.gcd(b)))
    }

    private fun encode(padding: ByteArray, message: ByteArray): ByteArray {
        val encoded = ByteArray(padding.size + message.size + 3)
        encoded[0] = 0x00
        encoded[1] = 0x02
        Arrays.arraycopy(padding, 0, encoded, 2, padding.size)
        encoded[padding.size + 2] = 0x00
        Arrays.arraycopy(message, 0, encoded, padding.size + 3, message.size)
        return encoded
    }

    private fun decode(message: ByteArray): ByteArray {
        var i = 1
        val zero = 0.toByte()
        while (message[i] != zero) i++
        return message.copyOfRange(i + 1, message.size)
    }

    private fun encryptBlock(m: BigInteger, publicKey: RSAPublicKey): BigInteger {
        return m.modPow(publicKey.e, publicKey.n)
    }

    private fun decryptBlock(m: BigInteger, privateKey: RSAPrivateKey): BigInteger {
        return m.modPow(privateKey.d, privateKey.n)
    }

    private fun rsasp1(privateKey: RSAPrivateKey, message: BigInteger): BigInteger {
        if (message < BigInteger.ZERO || message > privateKey.n.subtract(BigInteger.ONE)) {
            throw IllegalArgumentException("message representative out of range")
        }
        return message.modPow(privateKey.d, privateKey.n)
    }

    private fun rsavp1(publicKey: RSAPublicKey, signedMessage: BigInteger): BigInteger {
        if (signedMessage < BigInteger.ZERO || signedMessage > publicKey.n.subtract(BigInteger.ONE)) {
            throw IllegalArgumentException("signature representative out of range")
        }
        return signedMessage.modPow(publicKey.e, publicKey.n)
    }

    private fun emsaPkcs1Encode(message: ByteArray, emLen: Int, sigh: SIGH): ByteArray {

        val mHash = hash(message, sigh)

        val t = ByteArray(sigh.prefix.size + mHash.size)
        Arrays.arraycopy(sigh.prefix, 0, t, 0, sigh.prefix.size)
        Arrays.arraycopy(mHash, 0, t, sigh.prefix.size, mHash.size)

        val tLen = t.size
        if (emLen < tLen + 11) {
            throw IllegalArgumentException("emLen too short")
        }

        val ps = ByteArray(emLen - tLen - 3)
        for (i in ps.indices) {
            ps[i] = 0xFF.toByte()
        }

        val result = ByteArray(3 + ps.size + tLen)
        result[0] = 0x00
        result[1] = 0x01
        Arrays.arraycopy(ps, 0, result, 2, ps.size)
        result[ps.size + 2] = 0x00
        Arrays.arraycopy(t, 0, result, ps.size + 3, t.size)
        return result
    }

    private fun hash(message: ByteArray, sigh: SIGH): ByteArray {
        return when (sigh) {
            SIGH.MD5 -> MD5.hash(message)
            SIGH.SHA1 -> SHA1.hash(message)
            SIGH.SHA256 -> SHA256.hash(message)
            SIGH.SHA512 -> SHA512.hash(message)
        }
    }

    private fun os2ip(bi: ByteArray): BigInteger {
        var out = BigInteger.ZERO
        val max = BigInteger.valueOf(256)

        for (i in 1..bi.size) {
            out = out.add(BigInteger.valueOf((0xFF and bi[i - 1].toInt()).toLong()).multiply(max.pow(bi.size - i)))
        }
        return out
    }

    internal fun i2osp(bi: BigInteger, len: Int): ByteArray {
        val twofiftysix = BigInteger.valueOf(256)
        val out = ByteArray(len)
        var cur: Array<BigInteger>

        if (bi >= twofiftysix.pow(len)) {
            throw IllegalArgumentException("integer to large")
        }

        for (i in 1..len) {
            cur = bi.divideAndRemainder(twofiftysix.pow(len - i))
            out[i - 1] = cur[0].intValue().toByte()
        }
        return out
    }

    private val MD5_PREFIX = byteArrayOf(
        0x30.toByte(), 0x20.toByte(), 0x30.toByte(), 0x0c.toByte(), 0x06.toByte(), 0x08.toByte(), 0x2a.toByte(),
        0x86.toByte(), 0x48.toByte(), 0x86.toByte(), 0xf7.toByte(), 0x0d.toByte(), 0x02.toByte(), 0x05.toByte(),
        0x05.toByte(), 0x00.toByte(), 0x04.toByte(), 0x10.toByte()
    )

    private val SHA160_PREFIX = byteArrayOf(
        0x30.toByte(), 0x21.toByte(), 0x30.toByte(), 0x09.toByte(), 0x06.toByte(), 0x05.toByte(), 0x2b.toByte(),
        0x0e.toByte(), 0x03.toByte(), 0x02.toByte(), 0x1a.toByte(), 0x05.toByte(), 0x00.toByte(), 0x04.toByte(),
        0x14.toByte()
    )

    private val SHA256_PREFIX = byteArrayOf(
        0x30.toByte(), 0x31.toByte(), 0x30.toByte(), 0x0d.toByte(), 0x06.toByte(), 0x09.toByte(), 0x60.toByte(),
        0x86.toByte(), 0x48.toByte(), 0x01.toByte(), 0x65.toByte(), 0x03.toByte(), 0x04.toByte(), 0x02.toByte(),
        0x01.toByte(), 0x05.toByte(), 0x00.toByte(), 0x04.toByte(), 0x20.toByte()
    )

    private val SHA512_PREFIX = byteArrayOf(
        0x30.toByte(), 0x51.toByte(), 0x30.toByte(), 0x0d.toByte(), 0x06.toByte(), 0x09.toByte(), 0x60.toByte(),
        0x86.toByte(), 0x48.toByte(), 0x01.toByte(), 0x65.toByte(), 0x03.toByte(), 0x04.toByte(), 0x02.toByte(),
        0x03.toByte(), 0x05.toByte(), 0x00.toByte(), 0x04.toByte(), 0x40.toByte()
    )
}