package at.sysco.erp_connect

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.rule.ActivityTestRule
import at.sysco.erp_connect.konto_list.KontoListActivity
import at.sysco.erp_connect.settings.Encrypt

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import java.security.SecureRandom
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4ClassRunner::class)
class TestEncryption {
    private val encrypter = Encrypt()

    //Prüft Erstellung des Schlüssels
    @Test
    fun generateSymmetricKey() {
        val key = encrypter.getOrCreateSymmetricKey()
        assertNotNull(key)
    }

    //Erstellt falscher Schlüssel, was zu falscher Verschlüsselung führen sollte.
    @Test
    fun encryptDataWithFalseKey() {
        val result = encrypter.encrypt("test", createFalseKey())
        assertNull(result)
    }

    //Prüft ob Verschlüsselung und Entschlüselung richtig funktioniert.
    @Test
    fun encryptAndDecryptData() {
        val input = "testString"
        val key = encrypter.getOrCreateSymmetricKey()
        val pair = Encrypt().encrypt(input, key!!)
        val cryptText = pair!!.first
        val iv = pair.second
        assertNotNull(cryptText)
        assertNotEquals(cryptText, input)
        val result = Encrypt().decrypt(cryptText, key, iv)
        assertEquals(input, result)
    }

    //Versucht mit einem falschen Schlüssel zu entschlüsseln.
    @Test
    fun decryptWithFalseKey() {
        val input = "testString"
        val key = encrypter.getOrCreateSymmetricKey()
        val pair = encrypter.encrypt(input, key!!)
        val cryptText = pair!!.first
        val iv = pair.second

        val keyGenParameterSpec =
            KeyGenParameterSpec.Builder(
                "aes_encryption",
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                .build()
        val keyGenerator =
            KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        keyGenerator.init(keyGenParameterSpec)

        val wrongKey = keyGenerator.generateKey()
        val result = encrypter.decrypt(cryptText, wrongKey, iv)
        assertNull(result)
    }

    //Erstellt keinen AES-Schlüssel, sondern einen DES. Dieser darf nicht verwendet werden.
    fun createFalseKey(): SecretKey {
        val keyGen = KeyGenerator.getInstance("DES")
        val secRandom = SecureRandom()
        keyGen.init(secRandom)
        return keyGen.generateKey()
    }
}
