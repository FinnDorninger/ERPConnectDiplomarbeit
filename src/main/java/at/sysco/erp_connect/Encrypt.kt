package at.sysco.erp_connect

import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import android.security.keystore.KeyProperties
import android.security.keystore.KeyGenParameterSpec
import android.util.Log
import java.security.*
import javax.crypto.spec.IvParameterSpec


class Encrypt {
    private val cipherAlgorithm = "AES/CBC/PKCS7Padding"
    private val keyAlias = "aes_encryption"

    fun generateSymmetricKey(): Key {
        val keyStore = KeyStore.getInstance("AndroidKeyStore")
        keyStore.load(null)
        if (!keyStore.containsAlias(keyAlias)) {
            val keyGenerator =
                KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
            val keyGenParameterSpec =
                KeyGenParameterSpec.Builder(
                    keyAlias,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                    .build()
            keyGenerator.init(keyGenParameterSpec)
            return keyGenerator.generateKey()
        }
        return keyStore.getKey(keyAlias, null)
    }

    fun encrypt(plainText: ByteArray, key: Key): Pair<ByteArray, ByteArray>? {
        val cipher = Cipher.getInstance(cipherAlgorithm)
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val cipherText = cipher.doFinal(plainText)
        return Pair(cipherText, cipher.iv)
    }

    fun decrypt(cipherText: ByteArray, key: Key, iv: IvParameterSpec): ByteArray? {
        val cipher = Cipher.getInstance(cipherAlgorithm)
        try {
            cipher.init(Cipher.DECRYPT_MODE, key, iv)
        } catch (e: InvalidKeyException) {
            return null
        } catch (e: InvalidAlgorithmParameterException) {
            return null
        }
        return cipher.doFinal(cipherText)
    }
}