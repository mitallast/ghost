package com.github.mitallast.ghost.crypto.ecc

import com.github.mitallast.ghost.crypto.dec.Base64
import com.github.mitallast.ghost.crypto.dec.Hex
import kotlin.test.Test
import kotlin.test.assertEquals

class Curve25519Test {

    @Test
    fun keyPair() {

        val aliceKeyPair = Curve25519.keyPair()
        val bobKeyPair = Curve25519.keyPair()

        val aliceSharedKey = Curve25519.sharedKey(aliceKeyPair.privateKey, bobKeyPair.publicKey)
        val bobSharedKey = Curve25519.sharedKey(bobKeyPair.privateKey, aliceKeyPair.publicKey)

        assertEquals(Base64.encode(aliceSharedKey), Base64.encode(bobSharedKey))
    }

    @Test
    fun sharedKey() {

        // Test vector from: https://tools.ietf.org/html/rfc7748#section-4.1
        val alicePrivKey = Hex.toByteArray("77076d0a7318a57d3c16c17251b26645df4c2f87ebc0992ab177fba51db92c2a")
        val alicePubKey = Hex.toByteArray("8520f0098930a754748b7ddcb43ef75a0dbf3a0d26381af4eba4a98eaa9b4e6a")
        val aliceKeyPair = KeyPair(PublicKey(alicePubKey), PrivateKey(alicePrivKey))

        val bobPrivKey = Hex.toByteArray("5dab087e624a8a4b79e17f8b83800ee66f3bb1292618b6fd1c2f8b27ff88e0eb")
        val bobPubKey = Hex.toByteArray("de9edb7d7b7dc1b4d35b61c2ece435373f8343c85b78674dadfc7e146f882b4f")
        val bobKeyPair = KeyPair(PublicKey(bobPubKey), PrivateKey(bobPrivKey))

        val aliceSharedKey = Curve25519.sharedKey(aliceKeyPair.privateKey, bobKeyPair.publicKey)
        val bobSharedKey = Curve25519.sharedKey(bobKeyPair.privateKey, aliceKeyPair.publicKey)

        assertEquals(Base64.encode(aliceSharedKey), Base64.encode(bobSharedKey))
        assertEquals("4a5d9d5ba4ce2de1728e3bf480350f25e07e21c947d19e3376f09b3c1e161742", Hex.encode(aliceSharedKey))
        assertEquals("4a5d9d5ba4ce2de1728e3bf480350f25e07e21c947d19e3376f09b3c1e161742", Hex.encode(bobSharedKey))
    }
}