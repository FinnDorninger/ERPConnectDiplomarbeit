package at.sysco.erp_connect

import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import at.sysco.erp_connect.network.UnsafeHTTPClient
import java.lang.NumberFormatException

class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings_pref, rootKey)
        val editTextPreference: EditTextPreference? = findPreference("user_password")
        val editConTimeoutPreference: EditTextPreference? = findPreference("timeoutCon")
        val editReadTimeoutPreference: EditTextPreference? = findPreference("timeoutRead")
        val editURLPreference: EditTextPreference? = findPreference("base_url")

        val preferenceListenerConnection: Preference.OnPreferenceChangeListener =
            object : Preference.OnPreferenceChangeListener {
                override fun onPreferenceChange(preference: Preference?, newValue: Any?): Boolean {
                    try {
                        val value = Integer.parseInt(newValue.toString()).toLong()
                        if ((value > 0) && (value < 60)) {
                            UnsafeHTTPClient.conTimeout = value
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
        val preferenceListenerReading: Preference.OnPreferenceChangeListener =
            object : Preference.OnPreferenceChangeListener {
                override fun onPreferenceChange(preference: Preference?, newValue: Any?): Boolean {
                    try {
                        val value = Integer.parseInt(newValue.toString()).toLong()
                        if ((value > 0) && (value < 60)) {
                            UnsafeHTTPClient.readTimeout = value
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
        val preferenceURLListener: Preference.OnPreferenceChangeListener =
            object : Preference.OnPreferenceChangeListener {
                override fun onPreferenceChange(preference: Preference?, newValue: Any?): Boolean {
                    var newURL = newValue as String
                    when {
                        newURL.isEmpty() -> {
                        }
                        newURL.startsWith("https://") -> {
                        }
                        newURL.startsWith("http://") -> newURL =
                            newURL.replace("http://", "https://")
                        else -> newURL = "https://".plus(newValue)
                    }
                    if (Patterns.WEB_URL.matcher(newURL).matches()) {
                        PreferenceManager.getDefaultSharedPreferences(requireContext().applicationContext)
                            .edit().putString("base_url", newURL).apply()
                        Log.w(
                            "Test",
                            PreferenceManager.getDefaultSharedPreferences(requireContext().applicationContext).getString(
                                "base_url",
                                ""
                            )
                        )
                        return true
                    }
                    Toast.makeText(context, "Ungültige Eingabe", Toast.LENGTH_LONG).show()
                    return false
                }
            }

        editTextPreference?.setOnBindEditTextListener { editText ->
            editText.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD
        }

        editConTimeoutPreference?.onPreferenceChangeListener = preferenceListenerConnection
        editReadTimeoutPreference?.onPreferenceChangeListener = preferenceListenerReading
        editURLPreference?.onPreferenceChangeListener = preferenceURLListener
    }
}