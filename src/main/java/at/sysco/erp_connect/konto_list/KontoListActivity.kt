package at.sysco.erp_connect.konto_list

import at.sysco.erp_connect.R
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import at.sysco.erp_connect.adapter.KontoAdapter
import at.sysco.erp_connect.constants.FailureCode
import at.sysco.erp_connect.pojo.Konto
import at.sysco.erp_connect.model.KontoListModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_konto_list.*
import android.content.Intent
import android.view.*
import androidx.preference.PreferenceManager
import androidx.security.crypto.MasterKeys
import at.sysco.erp_connect.settings.SettingsActivity
import at.sysco.erp_connect.constants.FinishCode
import at.sysco.erp_connect.kontakte_list.KontakteListActivity
import at.sysco.erp_connect.model.KontakteListModel
import com.google.android.material.bottomnavigation.BottomNavigationView

//Activity für die Listen-Darstellung von allen Konten/Ansprechpartnerdetails
class KontoListActivity : AppCompatActivity(),
    KontoListContract.View {

    private lateinit var kontoListPresenter: KontoListPresenter
    private val keyGenParameterSpec = MasterKeys.AES256_GCM_SPEC
    private val masterKeyAlias = MasterKeys.getOrCreate(keyGenParameterSpec)
    var firstCall: Boolean = true
    var snackbar: Snackbar? = null
    var adapterRV: KontoAdapter? = null

    //Setzt Layout und beauftragt Presenter für Beschaffung der Daten.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_konto_list)
        initRecyclerView()
        PreferenceManager.setDefaultValues(this, R.xml.settings_pref, false)
        kontoListPresenter = KontoListPresenter(this, KontoListModel(this), KontakteListModel(this))
        bottomNavigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        kontoListPresenter.requestFromWS()
    }

    //Listener auf die Auswahl in dem Bottom-Navigation-Menu
    private val mOnNavigationItemSelectedListener =
        BottomNavigationView.OnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.action_Kontakte -> {
                    val intent = Intent(this, KontakteListActivity::class.java)
                    startActivity(intent)
                    return@OnNavigationItemSelectedListener true
                }
                R.id.action_Konten -> {
                    return@OnNavigationItemSelectedListener false
                }
            }
            false
        }

    //Hier werden Listener registriert und die Bottom-Navigation-Auswahl richtig eingestellt.
    override fun onResume() {
        super.onResume()
        bottomNavigation.menu.findItem(R.id.action_Konten).isChecked = true
        if (!firstCall && !this.fileList().contains("KontoFile.xml")) {
            clearResults()
            kontoListPresenter.requestFromWS()
        } else {
            firstCall = false
        }
    }

    //Wenn die Activity geschlossen wird, kann der nicht benötigte Listener geschlossen werden.
    override fun onDestroy() {
        super.onDestroy()
        kontoListPresenter.onDestroy()
    }

    //Löscht Daten aus Recyclerview
    fun clearResults() {
        snackbar?.dismiss()
        adapterRV?.clearAll()
    }

    //Stellt Snackbar dar.
    private fun showSnackbar(title: String, withAction: Boolean) {
        if (withAction) {
            snackbar =
                Snackbar.make(
                    findViewById(R.id.layoutKonto_List),
                    title,
                    Snackbar.LENGTH_INDEFINITE
                )
            snackbar?.setAction("Retry!") { kontoListPresenter.requestFromWS() }
        } else {
            val text = when (title) {
                FinishCode.finishedSavingKontakte -> "Alles gespeichert!"
                else -> title
            }
            snackbar = Snackbar.make(this.layoutKonto_List, text, Snackbar.LENGTH_LONG)
        }
        snackbar?.show()
    }

    //Bei Erfolg wird eine kurze Snackbar dargestellt.
    override fun onSucess(finishCode: String) {
        showSnackbar(finishCode, false)
    }

    //Prüft welcher Fehler vorherrscht, und ruft dann showSnackbar auf.
    override fun onError(failureCode: String) {
        when (failureCode) {
            FailureCode.ERROR_LOADING_FILE -> showSnackbar(failureCode, true)
            FailureCode.NO_DATA -> showSnackbar(failureCode, true)
            FailureCode.NO_CONNECTION -> showSnackbar(failureCode, true)
            else -> showSnackbar(failureCode, false)
        }
    }

    //Zeigt den Ladebalken
    override fun showProgress() {
        progressBar.visibility = View.VISIBLE
    }

    //Schließt ("Versteckt")  Ladebalken
    override fun hideProgress() {
        progressBar.visibility = View.GONE
    }

    //Set Layoutmanager und Trennung der einzelnen Einträge des Recyclerviews.
    private fun initRecyclerView() {
        rv_konto_list.layoutManager = LinearLayoutManager(this)
        rv_konto_list.addItemDecoration(DividerItemDecoration(rv_konto_list.context, 1))
    }

    //Bindet Daten aus dem Model mit Recylcerview. Implementiert auch Such-Listener (SearchView)
    override fun displayKontoListInRecyclerView(kontoList: List<Konto>) {
        rv_konto_list.visibility = View.VISIBLE
        adapterRV = KontoAdapter(ArrayList(kontoList), this)
        search_konto.visibility = View.VISIBLE
        rv_konto_list.adapter = adapterRV

        search_konto.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                adapterRV?.filter?.filter(newText)
                return false
            }
        })
    }

    //Zeichnet das Optionsmenü
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.settings_menu, menu)
        return true
    }

    //Wird aufgerufen bei dem Abruf einer Option aus dem Optionsmenü
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.action_settings) {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
            return true
        }
        if (id == R.id.action_retry) {
            kontoListPresenter.requestFromWS()
        }
        return super.onOptionsItemSelected(item)
    }
}
