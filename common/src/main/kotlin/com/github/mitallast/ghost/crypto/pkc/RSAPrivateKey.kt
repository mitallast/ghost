package com.github.mitallast.ghost.crypto.pkc

import com.github.mitallast.ghost.crypto.utils.BigInteger
import kotlin.math.ceil


data class RSAPrivateKey constructor(
    val n: BigInteger,
    val e: BigInteger,
    val d: BigInteger,
    val p: BigInteger,
    val q: BigInteger,
    val exp1: BigInteger,
    val exp2: BigInteger,
    val coe: BigInteger,
    val phi: BigInteger
) {

    val k = ceil(n.bitLength() / 8.0).toInt()
}