package at.sysco.erp_connect

import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import android.security.keystore.KeyProperties
import android.security.keystore.KeyGenParameterSpec
import java.security.*
import javax.crypto.spec.IvParameterSpec

//Klasse welche die Daten verschlüsselt
class Encrypt {
    private val cipherAlgorithm = "AES/CBC/PKCS7Padding"
    private val keyAlias = "aes_encryption"

    fun getKeyGenParameter(): KeyGenParameterSpec {
        val keyGenParameterSpec =
            KeyGenParameterSpec.Builder(
                keyAlias,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                .build()
        return keyGenParameterSpec
    }

    //Generiert einen AES-Schlüssel
    fun generateSymmetricKey(): Key {
        val keyStore = KeyStore.getInstance("AndroidKeyStore")
        val key: Key
        keyStore.load(null)
        key = if (!keyStore.containsAlias(keyAlias)) {
            val keyGenerator =
                KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
            keyGenerator.init(getKeyGenParameter())
            keyGenerator.generateKey()
        } else {
            keyStore.getKey(keyAlias, null)
        }
        return key
    }

    //Verschlüsselt ein Byte-Array und liefert zurück die verschlüsselten Daten + Initialisierungsvektor
    fun encrypt(plainText: ByteArray, key: Key): Pair<ByteArray, ByteArray>? {
        val cipher = Cipher.getInstance(cipherAlgorithm)
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val cipherText = cipher.doFinal(plainText)
        val iv = cipher.iv
        return Pair(cipherText, iv)
    }

    //Entschlüsselt einen Text. Eingabe Ciphertext, Schlüsssel und der Initialisierungsvektor
    fun decrypt(cipherText: ByteArray, key: Key, iv: IvParameterSpec): ByteArray? {
        var clearText: ByteArray?
        val cipher = Cipher.getInstance(cipherAlgorithm)
        try {
            cipher.init(Cipher.DECRYPT_MODE, key, iv)
            clearText = cipher.doFinal(cipherText)
        } catch (e: InvalidKeyException) {
            clearText = null
        } catch (e: InvalidAlgorithmParameterException) {
            clearText = null
        }
        return clearText
    }
}