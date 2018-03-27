package com.github.mitallast.ghost.crypto.utils

import com.github.mitallast.ghost.crypto.utils.BigInteger as KBigInteger
import java.math.BigInteger as JBigInteger

import kotlin.test.Test
import kotlin.test.assertEquals

class BigIntegerTest {

    @Test
    fun fromBytes() {
        val bytes = ByteArray(256)
        CryptoRandom().nextBytes(bytes)
        assertEquals(JBigInteger(bytes).toString(), KBigInteger(bytes).toString())
    }

    @Test
    fun add() {
        val bytes1 = ByteArray(256)
        val bytes2 = ByteArray(256)
        val rnd = CryptoRandom()
        rnd.nextBytes(bytes1)
        rnd.nextBytes(bytes2)

        assertEquals(
            JBigInteger(bytes1).add(JBigInteger(bytes2)).toString(),
            KBigInteger(bytes1).add(KBigInteger(bytes2)).toString()
        )
    }

    @Test
    fun subtract() {
        val bytes1 = ByteArray(256)
        val bytes2 = ByteArray(256)
        val rnd = CryptoRandom()
        rnd.nextBytes(bytes1)
        rnd.nextBytes(bytes2)

        assertEquals(
            JBigInteger(bytes1).subtract(JBigInteger(bytes2)).toString(),
            KBigInteger(bytes1).subtract(KBigInteger(bytes2)).toString()
        )
    }

    @Test
    fun multiply() {
        val bytes1 = ByteArray(256)
        val bytes2 = ByteArray(256)
        val rnd = CryptoRandom()
        rnd.nextBytes(bytes1)
        rnd.nextBytes(bytes2)

        assertEquals(
            JBigInteger(bytes1).multiply(JBigInteger(bytes2)).toString(),
            KBigInteger(bytes1).multiply(KBigInteger(bytes2)).toString()
        )
    }

    @Test
    fun divide() {
        val bytes1 = ByteArray(256)
        val bytes2 = ByteArray(256)
        val rnd = CryptoRandom()
        rnd.nextBytes(bytes1)
        rnd.nextBytes(bytes2)

        assertEquals(
            JBigInteger(bytes1).divide(JBigInteger(bytes2)).toString(),
            KBigInteger(bytes1).divide(KBigInteger(bytes2)).toString()
        )
    }

    @Test
    fun remainder() {
        val bytes1 = ByteArray(256)
        val bytes2 = ByteArray(256)
        val rnd = CryptoRandom()
        rnd.nextBytes(bytes1)
        rnd.nextBytes(bytes2)

        assertEquals(
            JBigInteger(bytes1).remainder(JBigInteger(bytes2)).toString(),
            KBigInteger(bytes1).remainder(KBigInteger(bytes2)).toString()
        )
    }

    @Test
    fun pow() {
        val bytes1 = ByteArray(256)
        val bytes2 = ByteArray(256)
        val rnd = CryptoRandom()
        rnd.nextBytes(bytes1)
        rnd.nextBytes(bytes2)

        assertEquals(
            JBigInteger(bytes1).pow(142).toString(),
            KBigInteger(bytes1).pow(142).toString()
        )
    }

    @Test
    fun gcd() {
        val bytes1 = ByteArray(256)
        val bytes2 = ByteArray(256)
        val rnd = CryptoRandom()
        rnd.nextBytes(bytes1)
        rnd.nextBytes(bytes2)

        assertEquals(
            JBigInteger(bytes1).gcd(JBigInteger(bytes2)).toString(),
            KBigInteger(bytes1).gcd(KBigInteger(bytes2)).toString()
        )
    }

    @Test
    fun abs() {
        val bytes1 = ByteArray(256)
        val bytes2 = ByteArray(256)
        val rnd = CryptoRandom()
        rnd.nextBytes(bytes1)
        rnd.nextBytes(bytes2)

        assertEquals(
            JBigInteger(bytes1).abs().toString(),
            KBigInteger(bytes1).abs().toString()
        )
    }

    @Test
    fun negate() {
        val bytes1 = ByteArray(256)
        val bytes2 = ByteArray(256)
        val rnd = CryptoRandom()
        rnd.nextBytes(bytes1)
        rnd.nextBytes(bytes2)

        assertEquals(
            JBigInteger(bytes1).negate().toString(),
            KBigInteger(bytes1).negate().toString()
        )
    }

    @Test
    fun signum() {
        val bytes1 = ByteArray(256)
        val bytes2 = ByteArray(256)
        val rnd = CryptoRandom()
        rnd.nextBytes(bytes1)
        rnd.nextBytes(bytes2)

        assertEquals(
            JBigInteger(bytes1).signum(),
            KBigInteger(bytes1).signum()
        )
    }

    @Test
    fun mod() {
        val bytes1 = ByteArray(4096)
        val bytes2 = ByteArray(4096)
        val rnd = CryptoRandom()
        rnd.nextBytes(bytes1)
        rnd.nextBytes(bytes2)

        assertEquals(
            JBigInteger(bytes1).mod(JBigInteger(bytes2).abs()).toString(),
            KBigInteger(bytes1).mod(KBigInteger(bytes2).abs()).toString()
        )
    }

    @Test
    fun modPow() {
        val bytes1 = ByteArray(4096)
        val bytes2 = ByteArray(256)
        val bytes3 = ByteArray(256)
        val rnd = CryptoRandom()
        rnd.nextBytes(bytes1)
        rnd.nextBytes(bytes2)
        rnd.nextBytes(bytes3)

        assertEquals(
            JBigInteger(bytes1).modPow(JBigInteger(bytes2).abs(), JBigInteger(bytes3).abs()).toString(),
            KBigInteger(bytes1).modPow(KBigInteger(bytes2).abs(), KBigInteger(bytes3).abs()).toString()
        )
    }

    @Test
    fun modInverse() {
        val bytes1 = ByteArray(256)
        val bytes2 = ByteArray(256)
        val rnd = CryptoRandom()
        rnd.nextBytes(bytes1)
        rnd.nextBytes(bytes2)

        assertEquals(
            JBigInteger(bytes1).modInverse(JBigInteger(bytes2).abs()).toString(),
            KBigInteger(bytes1).modInverse(KBigInteger(bytes2).abs()).toString()
        )
    }

    @Test
    fun shiftLeft() {
        val bytes1 = ByteArray(256)
        val rnd = CryptoRandom()
        rnd.nextBytes(bytes1)
        assertEquals(
            JBigInteger(bytes1).shiftLeft(389).toString(),
            KBigInteger(bytes1).shiftLeft(389).toString()
        )
    }

    @Test
    fun shiftRight() {
        val bytes1 = ByteArray(256)
        val rnd = CryptoRandom()
        rnd.nextBytes(bytes1)
        assertEquals(
            JBigInteger(bytes1).shiftRight(12).toString(),
            KBigInteger(bytes1).shiftRight(12).toString()
        )
    }

    @Test
    fun and() {
        val bytes1 = ByteArray(256)
        val bytes2 = ByteArray(256)
        val rnd = CryptoRandom()
        rnd.nextBytes(bytes1)
        rnd.nextBytes(bytes2)

        assertEquals(
            JBigInteger(bytes1).and(JBigInteger(bytes2)).toString(),
            KBigInteger(bytes1).and(KBigInteger(bytes2)).toString()
        )
    }

    @Test
    fun or() {
        val bytes1 = ByteArray(256)
        val bytes2 = ByteArray(256)
        val rnd = CryptoRandom()
        rnd.nextBytes(bytes1)
        rnd.nextBytes(bytes2)

        assertEquals(
            JBigInteger(bytes1).or(JBigInteger(bytes2)).toString(),
            KBigInteger(bytes1).or(KBigInteger(bytes2)).toString()
        )
    }

    @Test
    fun xor() {
        val bytes1 = ByteArray(256)
        val bytes2 = ByteArray(256)
        val rnd = CryptoRandom()
        rnd.nextBytes(bytes1)
        rnd.nextBytes(bytes2)

        assertEquals(
            JBigInteger(bytes1).xor(JBigInteger(bytes2)).toString(),
            KBigInteger(bytes1).xor(KBigInteger(bytes2)).toString()
        )
    }

    @Test
    fun not() {
        val bytes1 = ByteArray(256)
        val rnd = CryptoRandom()
        rnd.nextBytes(bytes1)

        assertEquals(
            JBigInteger(bytes1).not().toString(),
            KBigInteger(bytes1).not().toString()
        )
    }


    @Test
    fun andNot() {
        val bytes1 = ByteArray(256)
        val bytes2 = ByteArray(256)
        val rnd = CryptoRandom()
        rnd.nextBytes(bytes1)
        rnd.nextBytes(bytes2)

        assertEquals(
            JBigInteger(bytes1).andNot(JBigInteger(bytes2)).toString(),
            KBigInteger(bytes1).andNot(KBigInteger(bytes2)).toString()
        )
    }


    @Test
    fun testBit() {
        val bytes1 = ByteArray(256)
        val rnd = CryptoRandom()
        rnd.nextBytes(bytes1)

        assertEquals(
            JBigInteger(bytes1).testBit(12),
            KBigInteger(bytes1).testBit(12)
        )
    }


    @Test
    fun setBit() {
        val bytes1 = ByteArray(256)
        val rnd = CryptoRandom()
        rnd.nextBytes(bytes1)

        assertEquals(
            JBigInteger(bytes1).setBit(12).toString(),
            KBigInteger(bytes1).setBit(12).toString()
        )
    }


    @Test
    fun clearBit() {
        val bytes1 = ByteArray(256)
        val rnd = CryptoRandom()
        rnd.nextBytes(bytes1)

        assertEquals(
            JBigInteger(bytes1).clearBit(12).toString(),
            KBigInteger(bytes1).clearBit(12).toString()
        )
    }


    @Test
    fun flipBit() {
        val bytes1 = ByteArray(256)
        val rnd = CryptoRandom()
        rnd.nextBytes(bytes1)

        assertEquals(
            JBigInteger(bytes1).flipBit(12).toString(),
            KBigInteger(bytes1).flipBit(12).toString()
        )
    }

    @Test
    fun getLowestSetBit() {
        val bytes1 = ByteArray(256)
        val rnd = CryptoRandom()
        rnd.nextBytes(bytes1)

        assertEquals(
            JBigInteger(bytes1).getLowestSetBit(),
            KBigInteger(bytes1).getLowestSetBit()
        )
    }

    @Test
    fun bitLength() {
        val bytes1 = ByteArray(256)
        val rnd = CryptoRandom()
        rnd.nextBytes(bytes1)

        assertEquals(
            JBigInteger(bytes1).bitLength(),
            KBigInteger(bytes1).bitLength()
        )
    }

    @Test
    fun bitCount() {
        val bytes1 = ByteArray(256)
        val rnd = CryptoRandom()
        rnd.nextBytes(bytes1)

        assertEquals(
            JBigInteger(bytes1).bitCount(),
            KBigInteger(bytes1).bitCount()
        )
    }

    @Test
    fun isProbablePrime() {
        val bytes1 = ByteArray(256)
        val rnd = CryptoRandom()
        rnd.nextBytes(bytes1)

        assertEquals(
            JBigInteger(bytes1).isProbablePrime(13),
            KBigInteger(bytes1).isProbablePrime(13)
        )
    }

    @Test
    fun compareTo() {
        val bytes1 = ByteArray(256)
        val bytes2 = ByteArray(256)
        val rnd = CryptoRandom()
        rnd.nextBytes(bytes1)
        rnd.nextBytes(bytes2)

        assertEquals(
            JBigInteger(bytes1).compareTo(JBigInteger(bytes2)),
            KBigInteger(bytes1).compareTo(KBigInteger(bytes2))
        )
    }

    @Test
    fun min() {
        val bytes1 = ByteArray(256)
        val bytes2 = ByteArray(256)
        val rnd = CryptoRandom()
        rnd.nextBytes(bytes1)
        rnd.nextBytes(bytes2)

        assertEquals(
            JBigInteger(bytes1).min(JBigInteger(bytes2)).toString(),
            KBigInteger(bytes1).min(KBigInteger(bytes2)).toString()
        )
    }

    @Test
    fun max() {
        val bytes1 = ByteArray(256)
        val bytes2 = ByteArray(256)
        val rnd = CryptoRandom()
        rnd.nextBytes(bytes1)
        rnd.nextBytes(bytes2)

        assertEquals(
            JBigInteger(bytes1).max(JBigInteger(bytes2)).toString(),
            KBigInteger(bytes1).max(KBigInteger(bytes2)).toString()
        )
    }
}