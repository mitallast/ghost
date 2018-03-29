package com.github.mitallast.ghost.client

import com.github.mitallast.ghost.client.crypto.*
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Uint8Array

fun main(args: Array<String>) {
    val bytes = ArrayBuffer(256)
    crypto.getRandomValues(Uint8Array(bytes))

    console.log("bytes", bytes)

    SHA256.digest(bytes).then { console.log(it) }
    SHA512.digest(bytes).then { console.log(it) }

    ECDSA.generateKey(CurveP521).then { keyPair ->
        console.log("ECDSA key pair", keyPair)
        ECDSA.exportPrivateKey(keyPair.privateKey).then { buffer ->
            console.log("ECDSA export private key", buffer)
            ECDSA.importPrivateKey(CurveP521, buffer).then {
                console.log("ECDSA import private key", it)
            }
        }
        ECDSA.exportPublicKey(keyPair.publicKey).then { buffer ->
            console.log("ECDSA export public key", buffer)
            ECDSA.importPublicKey(CurveP521, buffer).then {
                console.log("ECDSA import public key", it)
            }
        }

        ECDSA.sign(HashSHA512, keyPair.privateKey, bytes).then { sign ->
            console.log("ECDSA sign", sign)
            ECDSA.verify(HashSHA512, keyPair.publicKey, sign, bytes).then { valid ->
                console.log("ECDSA valid", valid)
            }
        }
    }

    HMAC.generateKey(HashSHA512).then { key ->
        console.log("HMAC key", key)
        HMAC.exportKey(key).then { buffer ->
            console.log("HMAC export key", buffer)
            HMAC.importKey(HashSHA512, buffer).then {
                console.log("ECDSA import key", it)
            }
        }
        HMAC.sign(key, bytes).then { sign ->
            console.log("HMAC sign", sign)
            HMAC.verify(key, sign, bytes).then { valid ->
                console.log("HMAC valid", valid)
            }
        }
    }

    AES.generateKey(AESKeyLen256).then { key ->
        console.log("AES key", key)
        AES.encrypt(key, bytes).then { (encrypted, iv) ->
            console.log("AES encrypted", encrypted)
            console.log("AES iv", iv)
            AES.decrypt(key, encrypted, iv).then { decrypted ->
                console.log("AES decrypted", decrypted)
            }
        }

        RSA.generateKey(ModulusLen4096, HashSHA512).then { keyPair ->
            console.log("AES generated RSA key pair", keyPair)
            AES.wrapRSAPublicKey(key, keyPair.publicKey).then { (wrapped, iv) ->
                console.log("AES wrapped RSA public key", wrapped, iv)
                AES.unwrapRSAPublicKey(key, iv, wrapped, HashSHA512).then { unwrapped ->
                    console.log("AES unwrapped RSA public key", unwrapped)
                }
            }
            AES.wrapRSAPrivateKey(key, keyPair.privateKey).then { (wrapped, iv) ->
                console.log("AES wrapped RSA private key", wrapped, iv)
                AES.unwrapRSAPrivateKey(key, iv, wrapped, HashSHA512).then { unwrapped ->
                    console.log("AES unwrapped RSA private key", unwrapped)
                }
            }
        }
    }

    ECDH.generateKey(CurveP521).then { keyPair ->
        console.log("ECDH key pair", keyPair)
        ECDH.exportPublicKey(keyPair.publicKey).then { buffer ->
            console.log("ECDH export public key", buffer)
            ECDH.importPublicKey(CurveP521, buffer).then { key ->
                console.log("ECDH import public key", key)
            }
        }
        ECDH.exportPrivateKey(keyPair.privateKey).then { buffer ->
            console.log("ECDH export private key", buffer)
            ECDH.importPrivateKey(CurveP521, buffer).then { key ->
                console.log("ECDH import private key", key)
            }
        }
        ECDH.deriveKey(CurveP521, keyPair.publicKey, keyPair.privateKey, AESKeyLen256).then { key ->
            console.log("ECDH deriveKey", key)
        }
        ECDH.deriveBits(CurveP521, keyPair.publicKey, keyPair.privateKey, AESKeyLen256).then { buffer ->
            console.log("ECDH deriveBits", buffer)
        }
    }

    PBKDF2.importKey("admin").then { key ->
        console.log("PBKDF2 importKey", key)
        val salt = Uint8Array(16)
        crypto.getRandomValues(salt)
        PBKDF2.deriveKeyAES(salt, 1000, HashSHA512, key, AESKeyLen256).then { aesKey ->
            console.log("PBKDF2 deriveKeyAES", aesKey)
        }
        PBKDF2.deriveBits(salt, 1000, HashSHA512, key, AESKeyLen256).then { bits ->
            console.log("PBKDF2 deriveBits", bits)
        }
    }

    RSA.generateKey(ModulusLen4096, HashSHA512).then { keyPair ->
        console.log("RSA key pair", keyPair)
        RSA.exportPrivateKey(keyPair.privateKey).then { buffer ->
            console.log("RSA exported private key", buffer)
            RSA.importPrivateKey(HashSHA512, buffer).then { key ->
                console.log("RSA imported private key", key)
            }
        }
        RSA.exportPublicKey(keyPair.publicKey).then { buffer ->
            console.log("RSA exported public key", buffer)
            RSA.importPublicKey(HashSHA512, buffer).then { key ->
                console.log("RSA imported public key", key)
            }
        }
        RSA.encrypt(keyPair.publicKey, bytes).then { encrypted ->
            console.log("RSA encrypted", encrypted)
            RSA.decrypt(keyPair.privateKey, encrypted).then { decrypted ->
                console.log("RSA decrypted", decrypted)
            }
        }
        AES.generateKey(AESKeyLen256).then { aesKey ->
            console.log("RSA aes key to wrap", aesKey)
            RSA.wrapAESKey(keyPair.publicKey, HashSHA512, aesKey).then { wrapped ->
                console.log("RSA wrapped aes key", wrapped)
                RSA.unwrapAESKey(keyPair.privateKey, ModulusLen4096, HashSHA512, wrapped, AESKeyLen256).then { unwrapped ->
                    console.log("RSA unwrapped aes key", unwrapped)
                }
            }
        }
    }
}