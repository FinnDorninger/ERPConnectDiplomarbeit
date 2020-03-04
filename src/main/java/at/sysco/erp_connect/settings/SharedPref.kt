package at.sysco.erp_connect.settings

import android.content.Context
import androidx.preference.PreferenceManager

object SharedPref {
    //Liefert den Benutzernamen aus den SharedPreferences (gespeichertes!)
    fun getUserName(context: Context): String? {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
        return sharedPref.getString("user_name", "")
    }

    //Liefert die URL aus den SharedPreferences
    fun getBaseURL(context: Context): String? {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
        return sharedPref.getString("base_url", "")
    }

    fun getUserPW(context: Context): String? {
        val key = Encrypt().getOrCreateSymmetricKey()
        var userPw: String? = null
        if (key != null) {
            val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
            userPw = sharedPref.getString("user_password", "")
            val cipher = sharedPref.getString("user_password", "")
            val ivString = sharedPref.getString("usedIV", "")

            if (cipher != null && ivString != null) {
                val plainText = Encrypt().decrypt(
                    cipherText = cipher,
                    key = key,
                    iv = ivString
                )
                if (plainText != null) {
                    userPw = plainText
                }
            }
        }
        return userPw
    }

    fun storePw(pwPlain: String, context: Context): Boolean {
        var sucess = false
        val key = Encrypt().getOrCreateSymmetricKey()
        if (key != null) {
            val encrypted = Encrypt().encrypt(pwPlain, key)
            if (encrypted != null) {
                PreferenceManager.getDefaultSharedPreferences(context).edit()
                    .putString("user_password", encrypted.first).apply()
                PreferenceManager.getDefaultSharedPreferences(context).edit()
                    .putString("usedIV", encrypted.second).apply()
                sucess = true
            }
        }
        return sucess
    }
}