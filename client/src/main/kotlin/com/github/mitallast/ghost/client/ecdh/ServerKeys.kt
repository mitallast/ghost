package com.github.mitallast.ghost.client.ecdh

import com.github.mitallast.ghost.client.common.await
import com.github.mitallast.ghost.client.crypto.CurveP521
import com.github.mitallast.ghost.client.crypto.ECDSA
import com.github.mitallast.ghost.client.crypto.ECDSAPublicKey
import com.github.mitallast.ghost.client.crypto.HEX

object ServerKeys {
    private val publicKeyHEX = "30819b301006072a8648ce3d020106052b81040023038186000400955901624e331001e2051bab3a26e2aa8f3ace8cf8d9a8a113bf2be71b8fba3340cd9fb1ffc5977885e8d0ba15674869c4fb344ecedd68e7aedd0d5bbcaec9359000d1bf0fcefa9dd7e236109a1053709ad1f07294bf8a2899980fee4eb8d0e83d60fdd4cf40867dd8052631108eaec245dcd2e860a0f2e1bb6abcb32fef36c0b95ef3"
    private val publicKeyHE_ = "308198300d06042b81047006052b810400230381860004019d9978cb62d52a6c60606f7ce0ee0fd4336944d516a385aba911ba39120648aa9db43f813ee446a8e82adf0749c30bd53d884b89a0741560a7db0968863606b99e01e33c5f67b20bd234c2f1c0cf98b7c642336c5070de008a5d28ecb4549b4859a03803092a3adda04783b27f4d55771281492162a4e11f0b05d0a8632cdc49adec3b"

    suspend fun publicKey(): ECDSAPublicKey {
        val exported = HEX.parseHex(publicKeyHEX).buffer
        return ECDSA.importPublicKey(CurveP521, exported).await()
    }
}