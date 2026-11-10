package com.qlic.mobile.feature.auth.data

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

interface TokenCipher {
    fun encrypt(plainText: String): String

    fun decrypt(cipherText: String): String
}

class AndroidKeystoreTokenCipher : TokenCipher {
    override fun encrypt(plainText: String): String {
        val cipher = Cipher.getInstance(transformation)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())
        val encrypted = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
        return listOf(
            payloadVersion,
            cipher.iv.toBase64(),
            encrypted.toBase64(),
        ).joinToString(separator = ".")
    }

    override fun decrypt(cipherText: String): String {
        val parts = cipherText.split('.')
        require(parts.size == 3 && parts[0] == payloadVersion) { "Unsupported encrypted token payload" }
        val cipher = Cipher.getInstance(transformation)
        cipher.init(
            Cipher.DECRYPT_MODE,
            getOrCreateKey(),
            GCMParameterSpec(gcmTagLengthBits, parts[1].fromBase64()),
        )
        return cipher.doFinal(parts[2].fromBase64()).toString(Charsets.UTF_8)
    }

    private fun getOrCreateKey(): SecretKey {
        val keyStore = KeyStore.getInstance(androidKeyStore).apply { load(null) }
        (keyStore.getKey(keyAlias, null) as? SecretKey)?.let { return it }

        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, androidKeyStore).run {
            init(
                KeyGenParameterSpec.Builder(
                    keyAlias,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(256)
                    .build(),
            )
            generateKey()
        }
    }

    private fun ByteArray.toBase64(): String = Base64.encodeToString(this, Base64.NO_WRAP or Base64.URL_SAFE)
    private fun String.fromBase64(): ByteArray = Base64.decode(this, Base64.NO_WRAP or Base64.URL_SAFE)

    private companion object {
        const val androidKeyStore = "AndroidKeyStore"
        const val keyAlias = "qlic_session_token_key"
        const val transformation = "AES/GCM/NoPadding"
        const val payloadVersion = "v1"
        const val gcmTagLengthBits = 128
    }
}
