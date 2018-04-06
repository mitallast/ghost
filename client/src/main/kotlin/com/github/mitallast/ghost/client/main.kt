package com.github.mitallast.ghost.client

import com.github.mitallast.ghost.Registry
import com.github.mitallast.ghost.client.view.ApplicationController

fun main(args: Array<String>) {

//    launch {
//        val pair = ECDH.generateKey(CurveP521).await()
//        val jwk = ECDH.exportPublicKeyJWK(pair.publicKey).await()
//        console.log(jwk)
//        val spki = ECDH.exportPublicKey(pair.publicKey).await()
//        console.log(HEX.toHex(spki))
//
//        val privJWK = ECDH.exportPrivateKeyJWK(pair.privateKey).await()
//        console.log(privJWK)
//        val pkcs8 = ECDH.exportPrivateKey(pair.privateKey).await()
//        console.log(HEX.toHex(pkcs8))
//    }

//    launch {
//        val pair = ECDSA.generateKey(CurveP521).await()
//        val jwk = ECDSA.exportPublicKeyJWK(pair.publicKey).await()
//        console.log(jwk)
//        val spki = ECDSA.exportPublicKey(pair.publicKey).await()
//        console.log(HEX.toHex(spki))
//
//        val privJWK = ECDSA.exportPrivateKeyJWK(pair.privateKey).await()
//        console.log(privJWK)
//        val pkcs8 = ECDSA.exportPrivateKey(pair.privateKey).await()
//        console.log(HEX.toHex(pkcs8))
//    }
//
    Registry.register()
    ApplicationController.start()
}