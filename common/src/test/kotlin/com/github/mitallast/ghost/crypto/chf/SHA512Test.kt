package com.github.mitallast.ghost.crypto.chf

import kotlin.test.Test
import kotlin.test.assertEquals

class SHA512Test {

    @Test
    fun hash() {
        assertEquals("cf83e1357eefb8bdf1542850d66d8007d620e4050b5715dc83f4a921d36ce9ce47d0d13c5d85f2b0ff8318d2877eec2f63b" +
                "931bd47417a81a538327af927da3e", SHA512.hash(""))
        assertEquals("1f40fc92da241694750979ee6cf582f2d5d7d28e18335de05abc54d0560e0f5302860c652bf08d560252aa5e74210546f36" +
                "9fbbbce8c12cfc7957b2652fe9a75", SHA512.hash("a"))
        assertEquals("ddaf35a193617abacc417349ae20413112e6fa4e89a97ea20a9eeee64b55d39a2192992a274fc1a836ba3c23a3feebbd454" +
                "d4423643ce80e2a9ac94fa54ca49f", SHA512.hash("abc"))
        assertEquals("4dbff86cc2ca1bae1e16468a05cb9881c97f1753bce3619034898faa1aabe429955a1bf8ec483d7421fe3c1646613a59ed5" +
                "441fb0f321389f77f48a879c7b1f1", SHA512.hash("abcdefghijklmnopqrstuvwxyz"))
        assertEquals("107dbf389d9e9f71a3a95f6c055b9251bc5268c2be16d6c13492ea45b0199f3309e16455ab1e96118e8a905d5597b72038d" +
                "db372a89826046de66687bb420e7c", SHA512.hash("message digest"))
        assertEquals("1e07be23c26a86ea37ea810c8ec7809352515a970e9253c26f536cfc7a9996c45c8370583e0a78fa4a90041d71a4ceab742" +
                "3f19c71b9d5a3e01249f0bebd5894", SHA512.hash("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"))
        assertEquals("72ec1ef1124a45b047e8b7c75a932195135bb61de24ec0d1914042246e0aec3a2354e093d76f3048b456764346900cb130d" +
                "2a4fd5dd16abb5e30bcb850dee843", SHA512.hash("12345678901234567890123456789012345678901234567890123456789012345678901234567890"))
        assertEquals("df20f8b85d47ff4030b84ac52d10245da65a7dec47bc151cc4ffb2a35b36e44d0ea903bd6e9967fd5e193940241f8fe9ca5" +
                "269f1d585b739830db2e93b9cd125", SHA512.hash("A quick movement of the enemy will jeopardize six gunboats."))
        assertEquals("693f39c78ee05925db3f3ea91fde8f1f22435986a8bf6bce6fbafae0a453fe8c4582aa4cec5959768b3c975bb5c3127093b" +
                "2a3c4188bab8a845cce02ae5e1366", SHA512.hash("Schweißgequält zündet Typograf Jakob verflixt öde Pangramme an."))
        assertEquals("9b75ddb74674b45ab738f84f73ef25c833d7d33d7c72d2556f13274d753259187386bf91dadf8e6a735e6111d703d3ffbabf64d827aaec64d5c6c33259260ce9"
                ,  SHA512.hash("Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, " +
                "sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit " +
                "amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam " +
                "voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet."))
    }
}