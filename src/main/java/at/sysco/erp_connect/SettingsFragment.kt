package at.sysco.erp_connect

import android.os.Bundle
import android.text.InputType
import android.util.Base64
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import at.sysco.erp_connect.network.HTTPClient
import java.lang.NumberFormatException

class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings_pref, rootKey)
        val editURLPreference: EditTextPreference? = findPreference("base_url")
        val editConTimeoutPreference: EditTextPreference? = findPreference("timeoutCon")
        val editReadTimeoutPreference: EditTextPreference? = findPreference("timeoutRead")
        val editPwPreference: EditTextPreference? = findPreference("user_password")
        val editUserPreference: EditTextPreference? = findPreference("user_name")

        val listenerUrl: Preference.OnPreferenceChangeListener =
            object : Preference.OnPreferenceChangeListener {
                override fun onPreferenceChange(preference: Preference?, newValue: Any?): Boolean {
                    val oldURL = newValue as String
                    var newURL = ""

                    if (oldURL.isNullOrEmpty()) {
                        removeFiles()
                        return true
                    }
                    when {
                        oldURL.startsWith("https://") -> {
                            newURL = oldURL
                        }
                        oldURL.startsWith("http://") -> newURL =
                            oldURL.replace("http://", "https://")
                        else -> newURL = "https://".plus(oldURL)
                    }
                    if (!oldURL.endsWith("/")) {
                        newURL = newURL.plus("/")
                    }
                    if (Patterns.WEB_URL.matcher(newURL).matches()) {
                        if (oldURL != newURL) {
                            removeFiles()
                            editURLPreference?.text = newURL
                            preferenceManager.sharedPreferences.edit().putString("base_url", newURL)
                                .apply()
                            return false
                        }
                        removeFiles()
                        return true
                    } else {
                        Toast.makeText(context, "Ungültige Eingabe", Toast.LENGTH_LONG).show()
                        return false
                    }
                }
            }
        val listenerUsername: Preference.OnPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { preference, newValue ->
                removeFiles()
                true
            }
        val listenerConnection: Preference.OnPreferenceChangeListener =
            object : Preference.OnPreferenceChangeListener {
                override fun onPreferenceChange(preference: Preference?, newValue: Any?): Boolean {
                    val checked = checkInput(newValue)
                    if (checked.first) {
                        HTTPClient.conTimeout = checked.second
                        return true
                    } else {
                        return false
                    }
                }
            }
        val listenerReading: Preference.OnPreferenceChangeListener =
            object : Preference.OnPreferenceChangeListener {
                override fun onPreferenceChange(preference: Preference?, newValue: Any?): Boolean {
                    val checked = checkInput(newValue)
                    if (checked.first) {
                        HTTPClient.readTimeout = checked.second
                        return true
                    } else {
                        return false
                    }
                }
            }
        val listenerPassword: Preference.OnPreferenceChangeListener =
            object : Preference.OnPreferenceChangeListener {
                override fun onPreferenceChange(preference: Preference?, newValue: Any?): Boolean {
                    removeFiles()
                    val pwPlain = newValue as String
                    val key = Encrypt().generateSymmetricKey()
                    val pair = Encrypt().encrypt(
                        pwPlain.toByteArray(Charsets.UTF_8),
                        key = key
                    )
                    val cipherText = pair?.first!!
                    val cipherTextString = Base64.encodeToString(cipherText, Base64.DEFAULT)
                    val iv = pair.second
                    val ivString = Base64.encodeToString(iv, Base64.DEFAULT)

                    preferenceManager.sharedPreferences.edit()
                        .putString("user_password", cipherTextString).apply()
                    preferenceManager.sharedPreferences.edit().putString("usedIV", ivString).apply()
                    return false
                }
            }
        editPwPreference?.setOnBindEditTextListener { editText ->
            editText.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD
        }

        editConTimeoutPreference?.onPreferenceChangeListener = listenerConnection
        editReadTimeoutPreference?.onPreferenceChangeListener = listenerReading
        editPwPreference?.onPreferenceChangeListener = listenerPassword
        editUserPreference?.onPreferenceChangeListener = listenerUsername
        editURLPreference?.onPreferenceChangeListener = listenerUrl
    }

    private fun checkInput(newValue: Any?): Pair<Boolean, Long> {
        var returnPair: Pair<Boolean, Long> = Pair(false, 0)
        try {
            val value = Integer.parseInt(newValue.toString()).toLong()
            if ((value > 0) && (value < 60)) {
                returnPair = Pair(true, value)
            } else {
                Toast.makeText(context, "Ungültige Eingabe", Toast.LENGTH_LONG).show()
            }
        } catch (e: NumberFormatException) {
            Toast.makeText(context, "Ungültige Eingabe", Toast.LENGTH_LONG).show()
        }
        return returnPair
    }

    private fun removeFiles() {
        requireContext().deleteFile("KontoFile.xml")
        requireContext().deleteFile("KontakteFile.xml")
    }
}