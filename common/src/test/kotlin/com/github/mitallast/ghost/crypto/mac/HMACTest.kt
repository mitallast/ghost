package com.github.mitallast.ghost.crypto.mac

import com.github.mitallast.ghost.crypto.dec.Hex
import com.github.mitallast.ghost.crypto.utils.Strings.toByteArray
import kotlin.test.Test
import kotlin.test.assertEquals

class HMACTest {

    private val key = "4469726B4765726861726474"

    @Test
    fun hmacMD5() {
        assertEquals("d7f896266a03a7bed6819b0cef894ba8", HMAC.md5(key, "a"))
        assertEquals("9754b7f643a7a7d819e5c6b0cdf9047a", HMAC.md5(key, "abc"))
        assertEquals("ebd4f14b2fce90fae17370f297680f55", HMAC.md5(key, "abcdefghijklmnopqrstuvwxyz"))
        assertEquals("ccae86b04844da8da7be48461c0af5b4", HMAC.md5(key, "message digest"))
        assertEquals("6de3e006954039142350804643b0131e", HMAC.md5(key, "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"))
        assertEquals("04aa4bbc68ee21dfc742dd2d6909aa7d", HMAC.md5(key, "12345678901234567890123456789012345678901234567890123456789012345678901234567890"))
        assertEquals("34d3a67ceef9accb78f9183ee772d937", HMAC.md5(key, "A quick movement of the enemy will jeopardize six gunboats."))
        assertEquals("b02ff78e9a43ea6c6fb2c9b52ee8c6eb", HMAC.md5(key, "Schweißgequält zündet Typograf Jakob verflixt öde Pangramme an."))
        assertEquals("b02ff78e9a43ea6c6fb2c9b52ee8c6eb", HMAC.md5(key, "Schweißgequält zündet Typograf Jakob verflixt öde Pangramme an."))
    }

    @Test
    fun hmacSHA1() {
        assertEquals("6b548959039445f61690b07ed73c79f724a219e8", HMAC.sha1(key, "a"))
        assertEquals("0b2b618e704d0fb61c1bebb689fbd8f6ed14b12f", HMAC.sha1(key, "abc"))
        assertEquals("43a4933c37332a94576b5ee8dfbef3f6943a1be3", HMAC.sha1(key, "abcdefghijklmnopqrstuvwxyz"))
        assertEquals("c064e45ac780f3896255645af033d123eaadb3f2", HMAC.sha1(key, "message digest"))
        assertEquals("403d49d70c9a68bacda19548c278d2d62212ccf0", HMAC.sha1(key, "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"))
        assertEquals("7289b3e29d332f5844b909f758f29bb415170093", HMAC.sha1(key, "12345678901234567890123456789012345678901234567890123456789012345678901234567890"))
        assertEquals("e91c2ea3de90e1142a8abf0f5c109c94efa23543", HMAC.sha1(key, "A quick movement of the enemy will jeopardize six gunboats."))
        assertEquals("2da36520788838e244029b088e71e27209b46565", HMAC.sha1(key, "Schweißgequält zündet Typograf Jakob verflixt öde Pangramme an."))
        assertEquals("2da36520788838e244029b088e71e27209b46565", Hex.encode(HMAC.sha1(toByteArray(key), toByteArray("Schweißgequält zündet Typograf Jakob verflixt öde Pangramme an."))))
    }

    @Test
    fun hmacSHA256() {
        assertEquals("ee5abdb1a95dc78c59c7feb9688eaf3947650d4fc1dc0283afc75bcd09cda45d", HMAC.sha256(key, "a"))
        assertEquals("34ff7ba8b29202603bb5300d5c3c5b6f781ba8ac5e4f6d63404b2a75dbab77b9", HMAC.sha256(key, "abc"))
        assertEquals("b191f1ee30396296370ae2a75396278c24b8997c32e9dee8fa307c032a9035e2", HMAC.sha256(key, "abcdefghijklmnopqrstuvwxyz"))
        assertEquals("1c5a20aaac95f9113f06a5e65bce998034917dabeca4f70d4e51fe8ca14b7f61", HMAC.sha256(key, "message digest"))
        assertEquals("e436d05122707077d2ea9eda4eefd360fbd488dfa8464e8593f428453da74110", HMAC.sha256(key, "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"))
        assertEquals("8accf28017c679b90f35cf93b55d6ea72dad8b35ca54fc52b21225e60a0d4a7b", HMAC.sha256(key, "12345678901234567890123456789012345678901234567890123456789012345678901234567890"))
        assertEquals("c5f9fab7ab2b41c2a1a4fd4c5241f1de97beee260533c86e15bc63082a16bd6c", HMAC.sha256(key, "A quick movement of the enemy will jeopardize six gunboats."))
        assertEquals("c98ab641a2c962f74198e19f7cc299ae33d2e13c121746d23e35f0b23f1cae06", HMAC.sha256(key, "Schweißgequält zündet Typograf Jakob verflixt öde Pangramme an."))
        assertEquals("c98ab641a2c962f74198e19f7cc299ae33d2e13c121746d23e35f0b23f1cae06", Hex.encode(HMAC.sha256(toByteArray(key), toByteArray("Schweißgequält zündet Typograf Jakob verflixt öde Pangramme an."))))
    }

    @Test
    fun hmacSHA512() {
        assertEquals("ba76ae26b103b9e7ed33fe31eafd33d439031dc3c9f02626aeacd4eac26e27cece3cfdb1640ee0fcc6b013c4af18f38ea79d0851783cd86874d06dc98de000be", HMAC.sha512(key, "a"))
        assertEquals("9a04ef3dc0e170403d57f849045864ae967c163bbfa6d4491298378ddb82bcf6548e35045ad267670b0d7ee6b7c7b2b917ebd6cb36409faab0649fe09c365471", HMAC.sha512(key, "abc"))
        assertEquals("254c22a5a30017f067ffe3b221150b91db9847647a1c845ca681dec4e395ad19df1e3de56ac637f15571cdc8c382f82d92070f4da1d55ad2fa996b548ae56e6a", HMAC.sha512(key, "abcdefghijklmnopqrstuvwxyz"))
        assertEquals("c5f9e25552215c013bb84273ae1de4abd07bbd9bce50a82f4c99a7ef15ba192dd84496ffd6831c4c3a460afcf231b5eaaaafbf5b8a11d064d61026ecffd321bd", HMAC.sha512(key, "message digest"))
        assertEquals("6307c492e552418815556ee863620632df38082495ce3c867a89b2db40d7a3df0b344a70a3f11e914b09d75a7d1ec3697b3d444ae39eb2db404afb78ebc39742", HMAC.sha512(key, "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"))
        assertEquals("e488dd58c2b5aed55b81ea09b12fc077ed9bb85e5d42e31b5bff5a16a5c23384423d1af8198e62c08eac2af1cf9bd7a077ece872ffbbf2bbd9332b4c2b1d7199", HMAC.sha512(key, "12345678901234567890123456789012345678901234567890123456789012345678901234567890"))
        assertEquals("7b7ba2c8385048a3bcdb0294269c56f705b9ce00f71349c3101e24b6d9c02effad79edd07f7c5d4910c6bf6222bb81912a7af9b6c6271b148f722ede27e96c93", HMAC.sha512(key, "A quick movement of the enemy will jeopardize six gunboats."))
        assertEquals("e1dd4a9abbed6944fc2b0e39b4f162b29b6f96431c3b6083063e167ed649cc9093f853be492e326338972b5e76a820870e69beb9bedce7e8dd31dd16f882422c", HMAC.sha512(key, "Schweißgequält zündet Typograf Jakob verflixt öde Pangramme an."))
        assertEquals("e1dd4a9abbed6944fc2b0e39b4f162b29b6f96431c3b6083063e167ed649cc9093f853be492e326338972b5e76a820870e69beb9bedce7e8dd31dd16f882422c", Hex.encode(HMAC.sha512(toByteArray(key), toByteArray("Schweißgequält zündet Typograf Jakob verflixt öde Pangramme an."))))
    }

    @Test
    fun hmacBigText() {
        assertEquals("fb94ef3435ff1bfb010fb88088a111ea6bb1086148290ec5e20c27ff3cf55791b758205fde468b95591f4c60d59b5e3495e06a30dc1443b30388f093964258d1"
                , HMAC.sha512(key, "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor " +
                "invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et " +
                "ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit " +
                "amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, " +
                "sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea " +
                "takimata sanctus est Lorem ipsum dolor sit amet."))
    }

    @Test
    fun hmacBigKey() {

        assertEquals("6f3dbfd27acb93dcb58533d26340c949ba68f037e520940d7d670afcc0d3fa6b"
                , HMAC.sha256("Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor " +
                "invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et " +
                "ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit " +
                "amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, " +
                "sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea " +
                "takimata sanctus est Lorem ipsum dolor sit amet.", "Schweißgequält zündet Typograf Jakob verflixt öde Pangramme an."))

        assertEquals("630ab05b3f6964014db021ec2c8b766b95adc20bc206fbb9f8b599493c66e5911e27c67506aa5ea0db36f63128fa64c97034e10620388cffb3194af3fe4b13fc"
                , HMAC.sha512("Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor " +
                "invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et " +
                "ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit " +
                "amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, " +
                "sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea " +
                "takimata sanctus est Lorem ipsum dolor sit amet.", "Schweißgequält zündet Typograf Jakob verflixt öde Pangramme an."))
    }
}