package com.github.mitallast.ghost.client.crypto

import com.github.mitallast.ghost.client.common.toArrayBuffer
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Uint8Array
import org.khronos.webgl.get
import kotlin.browser.window

object ECWrap {
    private val isFirefox = window.navigator.userAgent.toLowerCase().indexOf("firefox") > -1;
    private val invalidIdentifier = ASN1.ASN1ObjectIdentifier(Uint8Array(toArrayBuffer(HEX.parseHex("2b810470"))))
    private val validIdentifier = ASN1.ASN1ObjectIdentifier(Uint8Array(toArrayBuffer(HEX.parseHex("2A8648CE3D0201"))))

    fun privateKeyFormat(): String {
        return if (isFirefox) "jwk" else "pkcs8"
    }

    fun maybeUnwrap(encoded: ArrayBuffer): ArrayBuffer {
        return if (isFirefox) {
            unwrap(encoded)
        } else {
            encoded
        }
    }

    fun maybeWrap(encoded: ArrayBuffer): ArrayBuffer {
        return if (isFirefox) {
            wrap(encoded)
        } else {
            encoded
        }
    }

    private fun unwrap(encoded: ArrayBuffer): ArrayBuffer {
        console.log("unwrap key")
        val root = ASN1.InputStream(Uint8Array(encoded)).readObject()
        when (root) {
            is ASN1.ASN1Sequence -> {
                val first = root.vector[0]
                when (first) {
                    is ASN1.ASN1Sequence -> {
                        val firstId = first.vector[0]
                        when (firstId) {
                            is ASN1.ASN1ObjectIdentifier -> {
                                if (firstId.value == validIdentifier.value) {
                                    console.log("valid identifier detected")
                                    first.vector[0] = invalidIdentifier
                                    val out = ASN1.OutputStream()
                                    root.encode(out)
                                    return toArrayBuffer(out.toByteArray())
                                }
                            }
                        }
                    }
                }
            }
        }
        return encoded
    }

    private fun wrap(encoded: ArrayBuffer): ArrayBuffer {
        console.log("wrap key")
        val root = ASN1.InputStream(Uint8Array(encoded)).readObject()
        when (root) {
            is ASN1.ASN1Sequence -> {
                // console.log("root is sequence")
                val first = root.vector[0]
                when (first) {
                    is ASN1.ASN1Sequence -> {
                        // console.log("first is sequence")
                        val firstId = first.vector[0]
                        when (firstId) {
                            is ASN1.ASN1ObjectIdentifier -> {
                                // console.log("first is identifier", firstId.value)
                                // console.log("first is identifier", invalidIdentifier.value)
                                if (equal(firstId.value, invalidIdentifier.value)) {
                                    console.log("invalid identifier detected")
                                    first.vector[0] = validIdentifier

                                    val out = ASN1.OutputStream()
                                    root.encode(out)
                                    return toArrayBuffer(out.toByteArray())
                                }
                            }
                        }
                    }
                }
            }
        }
        return encoded
    }

    private fun equal(a: Uint8Array, b: Uint8Array): Boolean {
        if (a.length == b.length) {
            for (i in 0..a.length) {
                if (a[i] != b[i]) {
                    return false
                }
            }
            return true
        }
        return false
    }
}