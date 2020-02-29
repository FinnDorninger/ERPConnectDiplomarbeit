package at.sysco.erp_connect

import android.os.Bundle
import android.text.InputType
import android.util.Base64
import android.util.Patterns
import android.widget.Toast
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import at.sysco.erp_connect.network.HTTPClient
import java.lang.NumberFormatException

//Settingsfragment stellt die Einstellung dar. Setzt Listener auf die Einstellungsdaten. Prüft Benutzereingaben.
class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        //Ladet Einstellungshierarchie
        setPreferencesFromResource(R.xml.settings_pref, rootKey)
        //Ladet die Einstellungen aus dem Preferences. Dient dafür die Eigenschaften zu verändern
        val editURLPreference: EditTextPreference? = findPreference("base_url")
        val editConTimeoutPreference: EditTextPreference? = findPreference("timeoutCon")
        val editReadTimeoutPreference: EditTextPreference? = findPreference("timeoutRead")
        val editPwPreference: EditTextPreference? = findPreference("user_password")
        val editUserPreference: EditTextPreference? = findPreference("user_name")

        //Listener welcher bei Eingaben in den URL-EditText aufgerufen wird.
        val listenerUrl: Preference.OnPreferenceChangeListener =
            object : Preference.OnPreferenceChangeListener {
                //Prüft Benutzereingaben ob diese mit / endet und ob https://-Schema verwendet wurde
                override fun onPreferenceChange(preference: Preference?, newValue: Any?): Boolean {
                    val oldURL = newValue as String
                    var newURL: String
                    var shouldSafe: Boolean

                    if (oldURL.isEmpty()) {
                        removeFiles()
                    }
                    newURL = when {
                        oldURL.startsWith("https://") -> {
                            oldURL
                        }
                        oldURL.startsWith("http://") -> oldURL.replace("http://", "https://")
                        else -> "https://".plus(oldURL)
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
                            shouldSafe = false
                        } else {
                            removeFiles()
                            shouldSafe = true
                        }
                    } else {
                        Toast.makeText(context, "Ungültige Eingabe", Toast.LENGTH_LONG).show()
                        shouldSafe = false
                    }
                    return shouldSafe
                }
            }
        //Listener auf Benutzereingaben in die Benutzername-Einstellung, löscht dann Daten da durch andere Benutzereingaben neue Daten geladen werden sollen.
        val listenerUsername: Preference.OnPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { preference, newValue ->
                removeFiles()
                true
            }
        //Listener auf Benutzereingaben zur Auswahl des Connection Timeouts
        val listenerConnection: Preference.OnPreferenceChangeListener =
            object : Preference.OnPreferenceChangeListener {
                override fun onPreferenceChange(preference: Preference?, newValue: Any?): Boolean {
                    val shouldSafe: Boolean
                    val checked = checkInput(newValue)
                    if (checked.first) {
                        HTTPClient.conTimeout = checked.second
                        shouldSafe = true
                    } else {
                        shouldSafe = false
                    }
                    return shouldSafe
                }
            }
        //Listener auf Benutzereingaben zur Auswahl des Reading Timeouts.
        val listenerReading: Preference.OnPreferenceChangeListener =
            object : Preference.OnPreferenceChangeListener {
                override fun onPreferenceChange(preference: Preference?, newValue: Any?): Boolean {
                    val shouldSafe: Boolean
                    val checked = checkInput(newValue)
                    if (checked.first) {
                        HTTPClient.readTimeout = checked.second
                        shouldSafe = true
                    } else {
                        shouldSafe = false
                    }
                    return shouldSafe
                }
            }
        //Listener bei Eingaben in das Passwort-Fenster, speichert verschlüsselte Daten.
        //Löscht dann Daten da durch andere Benutzereingaben neue Daten geladen werden sollen.
        val listenerPassword: Preference.OnPreferenceChangeListener =
            object : Preference.OnPreferenceChangeListener {
                override fun onPreferenceChange(preference: Preference?, newValue: Any?): Boolean {
                    removeFiles()
                    val pwPlain = newValue as String
                    val key = Encrypt().generateSymmetricKey()
                    val pair = Encrypt().encrypt(
                        pwPlain.toByteArray(Charsets.UTF_8),
                        key
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
        //Versteckt Passwort bei Eingabe mit "Sternen"
        editPwPreference?.setOnBindEditTextListener { editText ->
            editText.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD
        }

        //Setzt die Listener
        editConTimeoutPreference?.onPreferenceChangeListener = listenerConnection
        editReadTimeoutPreference?.onPreferenceChangeListener = listenerReading
        editPwPreference?.onPreferenceChangeListener = listenerPassword
        editUserPreference?.onPreferenceChangeListener = listenerUsername
        editURLPreference?.onPreferenceChangeListener = listenerUrl
    }

    //Prüft Input ob dieser über 0 und unter 60 ist. (Für Timeout)
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

    //Methode welche die Daten löscht
    private fun removeFiles() {
        requireContext().deleteFile("KontoFile.xml")
        requireContext().deleteFile("KontakteFile.xml")
    }
}