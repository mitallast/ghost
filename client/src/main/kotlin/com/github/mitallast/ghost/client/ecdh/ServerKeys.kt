package com.github.mitallast.ghost.client.ecdh

import com.github.mitallast.ghost.client.common.await
import com.github.mitallast.ghost.client.crypto.CurveP521
import com.github.mitallast.ghost.client.crypto.ECDSA
import com.github.mitallast.ghost.client.crypto.ECDSAPublicKey
import com.github.mitallast.ghost.client.crypto.HEX

object ServerKeys {
    private val publicKeyHEX = "30819b301006072a8648ce3d020106052b81040023038186000400c681747f80da03f26b65d43f08f62cb849631c654611a9fd600346d11075a9e28ea51d07d8e2e2896930a85067e934c3e615948787ed06951f32f28bb487f1cf110002f1968fa0ef924b8bbebd6982deec7439c6147ec4445811a8d63bdaf265f9c69459db49b79275f6fc70550e2651021a90522e8ac8058a2b592311c44d3461935d"

    suspend fun publicKey(): ECDSAPublicKey {
        val exported = HEX.parseHex(publicKeyHEX).buffer
        return ECDSA.importPublicKey(CurveP521, exported).await()
    }
}