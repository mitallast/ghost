package com.github.mitallast.ghost.crypto.dec

import com.github.mitallast.ghost.crypto.utils.Strings
import kotlin.test.Test
import kotlin.test.assertEquals

class Base64Test {

    @Test
    fun encodeRFC4648Examples() {
        assertEquals("", Base64.encode(""))
        assertEquals("Zg==", Base64.encode("f"))
        assertEquals("Zm8=", Base64.encode("fo"))
        assertEquals("Zm9v", Base64.encode("foo"))
        assertEquals("Zm9vYg==", Base64.encode("foob"))
        assertEquals("Zm9vYmE=", Base64.encode("fooba"))
        assertEquals("Zm9vYmFy", Base64.encode("foobar"))
    }

    @Test
    fun decodeRFC4648Examples() {
        assertEquals("", Base64.decode(""))
        assertEquals("f", Base64.decode("Zg=="))
        assertEquals("fo", Base64.decode("Zm8="))
        assertEquals("foo", Base64.decode("Zm9v"))
        assertEquals("foob", Base64.decode("Zm9vYg=="))
        assertEquals("fooba", Base64.decode("Zm9vYmE="))
        assertEquals("foobar", Base64.decode("Zm9vYmFy"))
    }

    @Test
    fun encodeFunctionCalls() {
        assertEquals("YmFzZTY0", Base64.encode("base64"))
        assertEquals("YmFzZTY0", Base64.encode(Strings.toByteArray("base64")))
        assertEquals("YmFzZTY0YmFzZTY0YmFzZTY0YmFzZTY0YmFzZTY0YmFzZTY0YmFzZTY0YmFzZTY0YmFzZTY0YmFz\r\n" + "ZTY0YmFzZTY0YmFzZTY0YmFzZTY0YmFzZTY0",
            Base64.encode("base64base64base64base64base64base64base64base64base64base64base64base64base64base64", true))
        assertEquals("YmFzZTY0YmFzZTY0YmFzZTY0YmFzZTY0YmFzZTY0YmFzZTY0YmFzZTY0YmFzZTY0YmFzZTY0YmFz\r\n" + "ZTY0YmFzZTY0YmFzZTY0YmFzZTY0YmFzZTY0",
            Base64.encode(Strings.toByteArray("base64base64base64base64base64base64base64base64base64base64base64base64base64base64"), true))
        assertEquals("YmFzZTY0", Base64.encode("base64"))
        assertEquals("w6TDvMO2YmFzZTY0", Base64.encode("äüöbase64"))
        assertEquals("w6TDvMO2YmFzZTY0", Base64.encode("äüöbase64"))
        assertEquals("YmFzZTY0YmFzZTY0YmFzZTY0YmFzZTY0YmFzZTY0YmFzZTY0YmFzZTY0YmFzZTY0YmFzZTY0YmFz\r\n" + "ZTY0YmFzZTY0YmFzZTY0YmFzZTY0YmFzZTY0",
            Base64.encode("base64base64base64base64base64base64base64base64base64base64base64base64base64base64", true))
        assertEquals("YmFzZTY0YmFzZTY0YmFzZTY0YmFzZTY0YmFzZTY0YmFzZTY0YmFzZTY0YmFzZTY0YmFzZTY0YmFz\r\n" + "ZTY0YmFzZTY0YmFzZTY0YmFzZTY0YmFzZTY0",
            Base64.encode("base64base64base64base64base64base64base64base64base64base64base64base64base64base64", true))

        assertEquals("base64", Base64.decode("YmFzZTY0"))
        assertEquals("base64", Base64.decode("YmFzZTY0"))
        assertEquals("äüöbase64", Base64.decode("w6TDvMO2YmFzZTY0"))
        assertEquals("äüöbase64", Base64.decode("w6TDvMO2YmFzZTY0"))
    }

    @Test
    fun decodeFunctionCalls() {
        assertEquals("base64", Base64.decode("YmFzZTY0"))
        assertEquals("base64", Base64.decode("YmFzZTY0"))
        assertEquals("äüöbase64", Base64.decode("w6TDvMO2YmFzZTY0"))
        assertEquals("äüöbase64", Base64.decode("w6TDvMO2YmFzZTY0"))
    }

    @Test
    fun encodePangrams() {
        assertEquals("QSBxdWljayBtb3ZlbWVudCBvZiB0aGUgZW5lbXkgd2lsbCBqZW9wYXJkaXplIHNpeCBndW5ib2F0cy4=",
            Base64.encode("A quick movement of the enemy will jeopardize six gunboats.", false))
        assertEquals("U2Nod2Vpw59nZXF1w6RsdCB6w7xuZGV0IFR5cG9ncmFmIEpha29iIHZlcmZsaXh0IMO2ZGUgUGFuZ3JhbW1lIGFuLg==",
            Base64.encode("Schweißgequält zündet Typograf Jakob verflixt öde Pangramme an."))
    }

    @Test
    fun decodePangrams() {
        assertEquals("A quick movement of the enemy will jeopardize six gunboats.",
            Base64.decode("QSBxdWljayBtb3ZlbWVudCBvZiB0aGUgZW5lbXkgd2lsbCBqZW9wYXJkaXplIHNpeCBndW5ib2F0cy4="))
        assertEquals("Schweißgequält zündet Typograf Jakob verflixt öde Pangramme an.",
            Base64.decode("U2Nod2Vpw59nZXF1w6RsdCB6w7xuZGV0IFR5cG9ncmFmIEpha29iIHZlcmZsaXh0IMO2ZGUgUGFuZ3JhbW1lIGFuLg=="))
    }
}