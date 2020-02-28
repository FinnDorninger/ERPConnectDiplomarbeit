package at.sysco.erp_connect

import android.content.Context
import android.util.Base64
import androidx.preference.PreferenceManager
import javax.crypto.spec.IvParameterSpec

//Klasse mit Methoden zum Laden von Daten aus den SharedPreferences
object SharedPref {
    //Liefert Username
    fun getUserName(context: Context): String? {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
        val userName = sharedPref.getString("user_name", "")
        return userName
    }

    //Liefert URL aus SharedPref.
    fun getBaseURL(context: Context): String? {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
        val baseURL = sharedPref.getString("base_url", "")
        return baseURL
    }

    //Ladet (entschlüsselt) das Passwort des Benutzers aus SharedPref.
    fun getUserPW(context: Context): String? {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
        var userPW = sharedPref.getString("user_password", "")

        val key = Encrypt().generateSymmetricKey()
        val cipher = sharedPref.getString("user_password", "")
        val ivString = sharedPref.getString("usedIV", "")
        val cipherText = Base64.decode(cipher, Base64.DEFAULT)
        val iv = Base64.decode(ivString, Base64.DEFAULT)

        val plainText = Encrypt().decrypt(
            cipherText = cipherText,
            key = key,
            iv = IvParameterSpec(iv)
        )
        if (plainText != null) {
            userPW = String(plainText, Charsets.UTF_8)
        }
        return userPW
    }
}