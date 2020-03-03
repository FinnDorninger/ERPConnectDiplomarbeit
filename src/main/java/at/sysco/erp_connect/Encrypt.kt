package at.sysco.erp_connect

import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import android.security.keystore.KeyProperties
import android.security.keystore.KeyGenParameterSpec
import android.util.Base64
import android.widget.Toast
import java.lang.Exception
import java.security.*
import javax.crypto.spec.IvParameterSpec

//Klasse welche die Daten verschlüsselt
class Encrypt {
    private val cipherAlgorithm = "AES/CBC/PKCS7Padding"
    private val keyAlias = "aes_encryption"

    private fun getKeyGenParameter(): KeyGenParameterSpec {
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
    fun getOrCreateSymmetricKey(): Key? {
        val key: Key?
        key = try {
            val keyStore = KeyStore.getInstance("AndroidKeyStore")
            keyStore.load(null)
            if (!keyStore.containsAlias(keyAlias)) {
                val keyGenerator =
                    KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
                keyGenerator.init(getKeyGenParameter())
                keyGenerator.generateKey()
            } else {
                keyStore.getKey(keyAlias, null)
            }
        } catch (e: Exception) {
            null
        }
        return key
    }

    //Verschlüsselt ein Byte-Array und liefert zurück die verschlüsselten Daten + Initialisierungsvektor
    fun encrypt(plainText: String, key: Key): Pair<String, String>? {
        var result: Pair<String, String>?
        val plain = plainText.toByteArray(Charsets.UTF_8)
        try {
            val cipher = Cipher.getInstance(cipherAlgorithm)
            cipher.init(Cipher.ENCRYPT_MODE, key)
            val cipherText = Base64.encodeToString(cipher.doFinal(plain), Base64.DEFAULT)
            val iv = Base64.encodeToString(cipher.iv, Base64.DEFAULT)
            result = Pair(cipherText, iv)
        } catch (e: Exception) {
            result = null
        }
        return result
    }

    //Entschlüsselt einen Text. Eingabe Ciphertext, Schlüsssel und der Initialisierungsvektor
    fun decrypt(cipherText: String, key: Key, iv: String): String? {
        var clearText: String?
        val cipherNew = Base64.decode(cipherText, Base64.DEFAULT)
        val ivNew = Base64.decode(iv, Base64.DEFAULT)

        try {
            val cipher = Cipher.getInstance(cipherAlgorithm)
            cipher.init(Cipher.DECRYPT_MODE, key, IvParameterSpec(ivNew))
            val byteClearText = cipher.doFinal(cipherNew)
            clearText = String(byteClearText, Charsets.UTF_8)
        } catch (e: Exception) {
            clearText = null
        }
        return clearText
    }
}