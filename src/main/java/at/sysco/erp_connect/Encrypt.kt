package at.sysco.erp_connect

import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import android.security.keystore.KeyProperties
import android.security.keystore.KeyGenParameterSpec
import android.util.Log
import androidx.security.crypto.MasterKeys
import java.security.*
import javax.crypto.spec.IvParameterSpec


//Klasse zur Verschlüsselung von Daten
class Encrypt {
    private val cipherAlgorithm = "AES/CBC/PKCS7Padding"
    private val keyAlias = "aes_encryption"

    //Methode welche die Schlüsseleigenschaften liefert/setzt
    fun getKeyGenParameter(): KeyGenParameterSpec {
        val keyGenParameterSpec =
            KeyGenParameterSpec.Builder(
                keyAlias,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                .setUserAuthenticationRequired(true)
                .build()
        return keyGenParameterSpec
    }

    //Ladet falls vorhanden AES-Schlüssel aus KeyStore. Ansonsten wird ein neuer generiert, und in KeyStore gespeichert.
    fun generateSymmetricKey(): Key {
        val keyStore = KeyStore.getInstance("AndroidKeyStore")
        keyStore.load(null)
        if (!keyStore.containsAlias(keyAlias)) {
            val keyGenerator =
                KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
            keyGenerator.init(getKeyGenParameter())
            return keyGenerator.generateKey()
        }
        return keyStore.getKey(keyAlias, null)
    }

    //Methode welche einen Text (ByteArray) mit einem Schlüssel verschlüsselt
    //Rückgabe ist der verschlüsselte Text und der Initialisierungsvektor
    fun encrypt(plainText: ByteArray, key: Key): Pair<ByteArray, ByteArray>? {
        val cipher = Cipher.getInstance(cipherAlgorithm)
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val cipherText = cipher.doFinal(plainText)
        return Pair(cipherText, cipher.iv)
    }

    //Methode mit Parametern (Ciphertext, Schlüssel und Initialisierungsvektor) für die Entschlüsselung eines Textes
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