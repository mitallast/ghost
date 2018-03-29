package com.github.mitallast.ghost.client.crypto

sealed class Curve(val name: String)
object CurveP256 : Curve("P-256")
object CurveP521 : Curve("P-521")