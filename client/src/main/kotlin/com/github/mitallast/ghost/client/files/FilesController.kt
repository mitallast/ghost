package com.github.mitallast.ghost.client.files

import com.github.mitallast.ghost.client.common.await
import com.github.mitallast.ghost.client.common.toArrayBuffer
import com.github.mitallast.ghost.client.common.toByteArray
import com.github.mitallast.ghost.client.crypto.*
import com.github.mitallast.ghost.files.EncryptedFile
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Uint8Array
import org.w3c.files.Blob
import org.w3c.files.BlobPropertyBag
import org.w3c.files.File
import org.w3c.files.FileReader
import org.w3c.xhr.ARRAYBUFFER
import org.w3c.xhr.XMLHttpRequest
import org.w3c.xhr.XMLHttpRequestResponseType

internal class EncryptedBlob(
    val secretKey: ArrayBuffer,
    val verifyKey: ArrayBuffer,
    val sign: ArrayBuffer,
    val iv: ArrayBuffer,
    val encrypted: ArrayBuffer
)

object FilesController {
    suspend fun upload(file: File): EncryptedFile {
        console.log("read file")
        val reader = FileReader()
        reader.readAsArrayBuffer(file)
        val buffer = reader.await<ArrayBuffer>()
        console.log("encrypt file")
        val encrypted = encrypt(buffer)
        val sha1 = SHA1.digest(encrypted.encrypted).await()
        console.log("send file")
        val xhr = XMLHttpRequest()
        xhr.open("POST", "http://localhost:8800/file/upload", true)
        xhr.setRequestHeader("x-sha1", HEX.toHex(sha1))
        xhr.send(encrypted.encrypted)
        xhr.await()
        val address = xhr.responseText
        console.log("uploaded address", address)
        return EncryptedFile(
            file.name,
            file.size,
            file.type,
            address,
            toByteArray(encrypted.secretKey),
            toByteArray(encrypted.verifyKey),
            toByteArray(encrypted.sign),
            toByteArray(encrypted.iv)
        )
    }

    suspend fun download(file: EncryptedFile): Blob {
        console.log("download e2e file")
        val xhr = XMLHttpRequest()
        xhr.responseType = XMLHttpRequestResponseType.ARRAYBUFFER
        xhr.open("GET", "http://localhost:8800/file/${file.address}", true)
        xhr.send()
        xhr.await()
        console.info("success download")
        val encrypted = xhr.response as ArrayBuffer
        val encryptedFile = EncryptedBlob(
            toArrayBuffer(file.secretKey),
            toArrayBuffer(file.verifyKey),
            toArrayBuffer(file.sign),
            toArrayBuffer(file.iv),
            encrypted
        )
        console.info("decrypt file")
        val decrypted = decrypt(encryptedFile)
        return Blob(arrayOf(decrypted), BlobPropertyBag(type = file.mimetype))
    }

    private suspend fun encrypt(data: ArrayBuffer): EncryptedBlob {
        val aes = AES.generateKey(AESKeyLen256).await()
        val ecdsa = ECDSA.generateKey(CurveP384).await()
        val (encrypted, iv) = AES.encrypt(aes, data).await()
        val sha1 = SHA1.digest(data).await()
        val buffer = toArrayBuffer(iv.buffer, sha1)
        val sign = ECDSA.sign(HashSHA512, ecdsa.privateKey, buffer).await()
        val secretKey = AES.exportKey(aes).await()
        val verifyKey = ECDSA.exportPublicKey(ecdsa.publicKey).await()
        return EncryptedBlob(
            secretKey,
            verifyKey,
            sign,
            iv.buffer,
            encrypted
        )
    }

    private suspend fun decrypt(file: EncryptedBlob): ArrayBuffer {
        val ivB = Uint8Array(file.iv)
        val aes = AES.importKey(file.secretKey).await()
        val decrypted = AES.decrypt(aes, file.encrypted, ivB).await()
        val sha1 = SHA1.digest(decrypted).await()
        val buffer = toArrayBuffer(file.iv, sha1)
        val ecdsa = ECDSA.importPublicKey(CurveP384, file.verifyKey).await()
        val verified = ECDSA.verify(HashSHA512, ecdsa, file.sign, buffer).await()
        if (!verified) {
            throw IllegalArgumentException("e2e decrypt: sign not verified")
        }
        return decrypted
    }
}