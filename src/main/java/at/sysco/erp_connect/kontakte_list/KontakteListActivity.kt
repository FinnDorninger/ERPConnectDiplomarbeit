package at.sysco.erp_connect.kontakte_list

import at.sysco.erp_connect.R
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import at.sysco.erp_connect.constants.FailureCode
import com.google.android.material.snackbar.Snackbar
import android.content.Intent
import android.view.*
import at.sysco.erp_connect.settings.SettingsActivity
import at.sysco.erp_connect.adapter.KontakteListAdapter
import at.sysco.erp_connect.model.KontakteListModel
import at.sysco.erp_connect.pojo.Kontakt
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.activity_kontakte_list.*

//Activity für die Listen-Darstellung aller Kontakt/Ansprechpartner
class KontakteListActivity : AppCompatActivity(),
    KontakteListContract.View {
    private lateinit var kontakteListPresenter: KontakteListPresenter
    var snackbar: Snackbar? = null
    var adapterRV: KontakteListAdapter? = null
    var search: String? = ""

    //Setzt Layout und beauftragt Presenter für Beschaffung der Daten.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_kontakte_list)
        val intent = intent
        //Ruft Extras auf welche angehängt wurden. Extra ist die ID des zu ladenden Kontakt/Ansprechpartner
        search = intent.getStringExtra("searchdetail")
        bottomNavigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        startPresenterRequest()
        initRecyclerView()
    }

    override fun startPresenterRequest() {
        kontakteListPresenter = KontakteListPresenter(this, KontakteListModel(this))
        kontakteListPresenter.requestFromWS()
    }

    //Listener auf die Auswahl in dem Bottom-Navigation-Menu
    private val mOnNavigationItemSelectedListener =
        BottomNavigationView.OnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.action_Kontakte -> {
                    return@OnNavigationItemSelectedListener false
                }
                R.id.action_Konten -> {
                    finish()
                    return@OnNavigationItemSelectedListener true
                }
            }
            false
        }

    //Hier werden Listener registriert und die Bottom-Navigation-Auswahl richtig eingestellt.
    override fun onResume() {
        super.onResume()
        bottomNavigation.menu.findItem(R.id.action_Kontakte).isChecked = true
    }

    //Wenn die Activity geschlossen wird, kann der nicht benötigte Listener geschlossen werden.
    override fun onDestroy() {
        super.onDestroy()
        kontakteListPresenter.onDestroy()
    }

    //Stellt Snackbar dar.
    private fun showSnackbar(title: String, withAction: Boolean) {
        if (withAction) {
            snackbar =
                Snackbar.make(
                    findViewById(R.id.layoutKontakte_List),
                    title,
                    Snackbar.LENGTH_INDEFINITE
                )
            snackbar?.setAction(
                "Retry!"
            ) { kontakteListPresenter.requestFromWS() }
        } else {
            snackbar =
                Snackbar.make(this.layoutKontakte_List, title, Snackbar.LENGTH_LONG)
        }
        snackbar?.show()
    }

    //Bei Erfolg wird eine kurze Snackbar dargestellt.
    override fun onSucess(finishCode: String) {
        showSnackbar(finishCode, false)
    }

    //Prüft welcher Fehler vorherrscht, und ruft dann showSnackbar auf.
    override fun onError(failureCode: String) {
        search_konto.visibility = View.GONE
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

    //Versteckt den Ladebalken
    override fun hideProgress() {
        progressBar.visibility = View.GONE
    }

    //Set Layoutmanager und Trennung der einzelnen Einträge des Recyclerviews.
    private fun initRecyclerView() {
        rv_kontakte_list.layoutManager = LinearLayoutManager(this)
        rv_kontakte_list.addItemDecoration(DividerItemDecoration(rv_kontakte_list.context, 1))
    }

    //Bindet Daten aus dem Model mit Recylcerview. Implementiert auch Such-Listener (SearchView)
    override fun displayKontakteListInRecyclerView(kontakteList: List<Kontakt>) {
        rv_kontakte_list.visibility = View.VISIBLE
        adapterRV = KontakteListAdapter(ArrayList(kontakteList), this)
        search_konto.visibility = View.VISIBLE
        rv_kontakte_list.adapter = adapterRV

        search_konto.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (search == query) {
                    adapterRV?.getSubFilter()?.filter(search)
                }
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                adapterRV?.filter?.filter(newText)
                return false
            }
        })
        if (!search.isNullOrEmpty()) {
            search_konto.setQuery(search, true)
            search = ""
        }
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
        //"Verbindungseigenschaften-Eintrag" - Finished diese Activity, damit nach den Einstellungsänderungen wieder die Startseite startet.
        if (id == R.id.action_settings) {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
            finish()
            return true
        }
        //Retry: Neuer Webservice-Call
        if (id == R.id.action_retry) {
            kontakteListPresenter.requestFromWS()
        }
        return super.onOptionsItemSelected(item)
    }
}
