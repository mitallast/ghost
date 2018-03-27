package com.github.mitallast.ghost.crypto.pkc

import com.github.mitallast.ghost.crypto.utils.BigInteger
import kotlin.math.ceil

data class RSAPublicKey constructor(val n: BigInteger, val e: BigInteger) {

    val k = ceil(n.bitLength() / 8.0).toInt()
}