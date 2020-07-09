package at.sysco.erp_connect.settings

import android.os.Bundle
import android.text.InputType
import android.util.Patterns
import android.widget.Toast
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import at.sysco.erp_connect.R
import at.sysco.erp_connect.network.HTTPClient

//Settingsfragment stellt die Einstellung dar. Setzt Listener auf die Einstellungsdaten. Prüft Benutzereingaben.
class SettingsFragment : PreferenceFragmentCompat() {
    var toast: Toast? = null
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
                    val newURL: String
                    var shouldSafe = false

                    if (oldURL.isEmpty()) {
                        removeFiles()
                    }
                    newURL = SettingsUtility.improveURL(oldURL)
                    if (Patterns.WEB_URL.matcher(newURL).matches()) {
                        if (oldURL != newURL) {
                            removeFiles()
                            editURLPreference?.text = newURL
                            preferenceManager.sharedPreferences.edit().putString("base_url", newURL)
                                .apply()
                        } else {
                            removeFiles()
                            shouldSafe = true
                        }
                    } else {
                        toast = Toast.makeText(context, "Ungültige Eingabe", Toast.LENGTH_LONG)
                        toast?.show()
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
                    var shouldSafe = false
                    val checked = SettingsUtility.checkInput(newValue)
                    if (checked.first) {
                        HTTPClient.conTimeout = checked.second
                        shouldSafe = true
                    } else {
                        toast = Toast.makeText(
                            context,
                            "Eingabe muss zwischen 0-60 sein!",
                            Toast.LENGTH_LONG
                        )
                        toast?.show()
                    }
                    return shouldSafe
                }
            }
        //Listener auf Benutzereingaben zur Auswahl des Reading Timeouts.
        val listenerReading: Preference.OnPreferenceChangeListener =
            object : Preference.OnPreferenceChangeListener {
                override fun onPreferenceChange(preference: Preference?, newValue: Any?): Boolean {
                    var shouldSafe = false
                    val checked = SettingsUtility.checkInput(newValue)
                    if (checked.first) {
                        HTTPClient.readTimeout = checked.second
                        shouldSafe = true
                    } else {
                        toast = Toast.makeText(
                            context,
                            "Eingabe muss zwischen 0-60 sein!",
                            Toast.LENGTH_LONG
                        )
                        toast?.show()
                    }
                    return shouldSafe
                }
            }
        //Listener bei Eingaben in das Passwort-Fenster, speichert verschlüsselte Daten.
        //Löscht dann Daten da durch andere Benutzereingaben neue Daten geladen werden sollen.
        val listenerPassword: Preference.OnPreferenceChangeListener =
            object : Preference.OnPreferenceChangeListener {
                override fun onPreferenceChange(preference: Preference?, newValue: Any?): Boolean {
                    var sucess = false
                    removeFiles()
                    val pwPlain = newValue as String
                    if (!SharedPref.storePw(
                            pwPlain,
                            requireContext()
                        )
                    ) {
                        toast = Toast.makeText(
                            context,
                            "Verschlüsselung hat nicht funktioniert!",
                            Toast.LENGTH_LONG
                        )
                        toast?.show()
                        sucess = true
                    }
                    return sucess
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

    override fun onDestroy() {
        super.onDestroy()
        toast?.cancel()
    }

    //Methode welche die Daten löscht
    private fun removeFiles() {
        requireContext().deleteFile("KontoFile.xml")
        requireContext().deleteFile("KontakteFile.xml")
    }
}