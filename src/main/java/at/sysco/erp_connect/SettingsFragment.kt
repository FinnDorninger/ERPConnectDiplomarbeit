package at.sysco.erp_connect

import android.os.Bundle
import android.text.InputType
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
        val preferenceURLListener: Preference.OnPreferenceChangeListener =
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
                        Log.w("Testfail", "Test$newURL")
                        Toast.makeText(context, "Ungültige Eingabe", Toast.LENGTH_LONG).show()
                        return false
                    }
                }
            }
        editURLPreference?.onPreferenceChangeListener = preferenceURLListener

        val editConTimeoutPreference: EditTextPreference? = findPreference("timeoutCon")
        val editReadTimeoutPreference: EditTextPreference? = findPreference("timeoutRead")
        val editPwPreference: EditTextPreference? = findPreference("user_password")
        val editUserPreference: EditTextPreference? = findPreference("user_name")

        val listenerPasswordOrNameChanger: Preference.OnPreferenceChangeListener =
            object : Preference.OnPreferenceChangeListener {
                override fun onPreferenceChange(preference: Preference?, newValue: Any?): Boolean {
                    removeFiles()
                    return true
                }
            }
        val preferenceListenerConnection: Preference.OnPreferenceChangeListener =
            object : Preference.OnPreferenceChangeListener {
                override fun onPreferenceChange(preference: Preference?, newValue: Any?): Boolean {
                    try {
                        val value = Integer.parseInt(newValue.toString()).toLong()
                        if ((value > 0) && (value < 60)) {
                            HTTPClient.conTimeout = value
                            return true
                        } else {
                            Toast.makeText(context, "Ungültige Eingabe", Toast.LENGTH_LONG).show()
                            return false
                        }
                    } catch (e: NumberFormatException) {
                        Toast.makeText(context, "Ungültige Eingabe", Toast.LENGTH_LONG).show()
                        return false
                    }
                }
            }
        val preferenceListenerReading: Preference.OnPreferenceChangeListener =
            object : Preference.OnPreferenceChangeListener {
                override fun onPreferenceChange(preference: Preference?, newValue: Any?): Boolean {
                    try {
                        val value = Integer.parseInt(newValue.toString()).toLong()
                        if ((value > 0) && (value < 60)) {
                            HTTPClient.readTimeout = value
                            return true
                        } else {
                            Toast.makeText(context, "Ungültige Eingabe T", Toast.LENGTH_LONG).show()
                            return false
                        }
                    } catch (e: NumberFormatException) {
                        Toast.makeText(context, "Ungültige Eingabe", Toast.LENGTH_LONG).show()
                        return false
                    }
                }
            }
        editPwPreference?.setOnBindEditTextListener { editText ->
            editText.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD
        }
        editConTimeoutPreference?.onPreferenceChangeListener = preferenceListenerConnection
        editReadTimeoutPreference?.onPreferenceChangeListener = preferenceListenerReading
        editPwPreference?.onPreferenceChangeListener = listenerPasswordOrNameChanger
        editUserPreference?.onPreferenceChangeListener = listenerPasswordOrNameChanger
    }

    private fun removeFiles() {
        requireContext().deleteFile("KontoFile.xml")
        requireContext().deleteFile("KontakteFile.xml")
    }
}