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
import at.sysco.erp_connect.SettingsActivity
import at.sysco.erp_connect.kontakte_list.KontakteListActivity
import at.sysco.erp_connect.model.KontakteListModel
import com.google.android.material.bottomnavigation.BottomNavigationView


class KontoListActivity : AppCompatActivity(),
    KontoListContract.View {

    private lateinit var kontoListPresenter: KontoListPresenter
    private val keyGenParameterSpec = MasterKeys.AES256_GCM_SPEC
    private val masterKeyAlias = MasterKeys.getOrCreate(keyGenParameterSpec)
    var snackbar: Snackbar? = null
    var adapterRV: KontoAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_konto_list)
        initRecyclerView()
        PreferenceManager.setDefaultValues(this, R.xml.settings_pref, false)
        kontoListPresenter = KontoListPresenter(this, KontoListModel(this), KontakteListModel(this))
        kontoListPresenter.requestFromWS()
        bottomNavigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
    }

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

    override fun onResume() {
        super.onResume()
        bottomNavigation.menu.findItem(R.id.action_Konten).isChecked = true
        if (!this.fileList().contains("KontoFile.xml") or !this.fileList().contains("KontakteFile.xml")) {
            clearResults()
            kontoListPresenter.requestFromWS()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        kontoListPresenter.onDestroy()
    }

    fun clearResults() {
        snackbar?.dismiss()
        adapterRV?.clearAll()
    }

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
            snackbar =
                Snackbar.make(this.layoutKonto_List, title, Snackbar.LENGTH_LONG)
        }
        snackbar?.show()
    }

    override fun onSucess(finishCode: String) {
        showSnackbar(finishCode, false)
    }

    override fun onError(failureCode: String) {
        when (failureCode) {
            FailureCode.ERROR_LOADING_FILE -> showSnackbar(failureCode, true)
            FailureCode.NO_DATA -> showSnackbar(failureCode, true)
            FailureCode.ERROR_SAVING_FILE -> showSnackbar(failureCode, false)
            FailureCode.NOT_ENOUGH_SPACE -> showSnackbar(failureCode, false)
        }
    }

    override fun showProgress() {
        progressBar.visibility = View.VISIBLE
    }

    override fun hideProgress() {
        progressBar.visibility = View.GONE
    }

    private fun initRecyclerView() {
        rv_konto_list.layoutManager = LinearLayoutManager(this)
        rv_konto_list.addItemDecoration(DividerItemDecoration(rv_konto_list.context, 1))
    }

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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.settings_menu, menu)
        return true
    }

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
