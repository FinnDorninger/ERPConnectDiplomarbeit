package at.sysco.erp_connect.settings

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import at.sysco.erp_connect.R

//Activity welche die Einstellungen anzeigt
class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        //Stellt Settings Fragment dar.
        supportFragmentManager
            .beginTransaction()
            .replace(
                R.id.fragmentContainer,
                SettingsFragment()
            )
            .commit()
    }
}