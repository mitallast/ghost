package com.github.mitallast.ghost.client.ecdh

import com.github.mitallast.ghost.client.common.await
import com.github.mitallast.ghost.client.crypto.CurveP384
import com.github.mitallast.ghost.client.crypto.ECDSA
import com.github.mitallast.ghost.client.crypto.ECDSAPublicKey
import com.github.mitallast.ghost.client.crypto.HEX

object ServerKeys {
    private val publicKeyHEX = "3076301006072a8648ce3d020106052b8104002203620004611094aba64c4a35079e1a0cdb6a8a0b4e666e94147384c3e44dc8248cb6885d93c98b0c1a47aee382f52182ea9878d74d02f537b7daefd933487b3c5c1fe72752e10b2c6e5932cc1212878ee1058f64087fbb1990f4227fd706fe99a7500c4e"

    suspend fun publicKey(): ECDSAPublicKey {
        val exported = HEX.parseHex(publicKeyHEX).buffer
        return ECDSA.importPublicKey(CurveP384, exported).await()
    }
}