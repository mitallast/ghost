package com.github.mitallast.ghost.client

import com.github.mitallast.ghost.Registry
import com.github.mitallast.ghost.client.view.ApplicationController

fun main(args: Array<String>) {

//    launch {
//        val pair = ECDH.generateKey(CurveP384).await()
//        val spki = ECDH.exportPublicKey(pair.publicKey).await()
//        console.log("ECDH public", HEX.toHex(spki))
//
//        val pkcs8 = ECDH.exportPrivateKey(pair.privateKey).await()
//        console.log("ECDH private", HEX.toHex(pkcs8))
//    }
//
//    launch {
//        val pair = ECDSA.generateKey(CurveP384).await()
//        val spki = ECDSA.exportPublicKey(pair.publicKey).await()
//        console.log("ECDSA public", HEX.toHex(spki))
//        val pkcs8 = ECDSA.exportPrivateKey(pair.privateKey).await()
//        console.log("ECDSA private", HEX.toHex(pkcs8))
//    }

    Registry.register()
    ApplicationController.start()
}