package at.sysco.erp_connect

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import at.sysco.erp_connect.konto_list.KontoListActivity

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragmentContainer, SettingsFragment())
            .commit()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, KontoListActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
    }
}