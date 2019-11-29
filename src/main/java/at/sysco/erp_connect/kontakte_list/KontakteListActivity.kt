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
import android.content.SharedPreferences
import android.util.Log
import android.view.*
import androidx.preference.PreferenceManager
import at.sysco.erp_connect.SettingsActivity
import at.sysco.erp_connect.adapter.KontakteListAdapter
import at.sysco.erp_connect.konto_list.KontoListActivity
import at.sysco.erp_connect.model.KontakteListModel
import at.sysco.erp_connect.pojo.Kontakt
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.activity_kontakte_list.*


class KontakteListActivity : AppCompatActivity(),
    KontakteListContract.View, SharedPreferences.OnSharedPreferenceChangeListener {

    private lateinit var kontakteListPresenter: KontakteListPresenter
    var snackbar: Snackbar? = null
    var adapterRV: KontakteListAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_kontakte_list)

        initRecyclerView()

        kontakteListPresenter = KontakteListPresenter(this, KontakteListModel(this))
        kontakteListPresenter.requestFromWS()

        bottomNavigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        bottomNavigation.menu.findItem(R.id.action_Kontakte).isChecked = true
    }

    private val mOnNavigationItemSelectedListener =
        BottomNavigationView.OnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.action_Kontakte -> {
                    return@OnNavigationItemSelectedListener true
                }
                R.id.action_Konten -> {
                    finish()
                    return@OnNavigationItemSelectedListener true
                }
            }
            false
        }

    override fun onResume() {
        super.onResume()
        PreferenceManager.getDefaultSharedPreferences(this)
            .registerOnSharedPreferenceChangeListener(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        PreferenceManager.getDefaultSharedPreferences(this)
            .unregisterOnSharedPreferenceChangeListener(this)
        kontakteListPresenter.onDestroy()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (this.fileList().contains("KontakteFile.xml")) {
            this.deleteFile("KontakteFile.xml")
        }
        clearResults()
        kontakteListPresenter.requestFromWS()
    }

    fun clearResults() {
        snackbar?.dismiss()
        adapterRV?.clearAll()
    }

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
        rv_kontakte_list.layoutManager = LinearLayoutManager(this)
        rv_kontakte_list.addItemDecoration(DividerItemDecoration(rv_kontakte_list.context, 1))
    }

    override fun displayKontakteListInRecyclerView(kontakteList: List<Kontakt>) {
        rv_kontakte_list.visibility = View.VISIBLE
        adapterRV = KontakteListAdapter(ArrayList(kontakteList), this)
        search_konto.visibility = View.VISIBLE
        rv_kontakte_list.adapter = adapterRV

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
        // Inflate the menu to use in the action bar
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
            kontakteListPresenter.requestFromWS()
        }
        return super.onOptionsItemSelected(item)
    }
}
