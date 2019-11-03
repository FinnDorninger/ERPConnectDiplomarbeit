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
import android.content.SharedPreferences
import android.util.Log
import android.view.*
import androidx.preference.PreferenceManager
import at.sysco.erp_connect.SettingsActivity


class KontoListActivity : AppCompatActivity(),
    KontoListContract.View, SharedPreferences.OnSharedPreferenceChangeListener {

    private lateinit var kontoListPresenter: KontoListPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_konto_list)

        initRecyclerView()

        kontoListPresenter = KontoListPresenter(this, KontoListModel(this))
        kontoListPresenter.requestFromWS()
    }

    override fun onResume() {
        super.onResume()
        PreferenceManager.getDefaultSharedPreferences(this)
            .registerOnSharedPreferenceChangeListener(this)
        Log.w("Finn", "Resume")
    }

    override fun onPause() {
        super.onPause()
        PreferenceManager.getDefaultSharedPreferences(this)
            .registerOnSharedPreferenceChangeListener(this)
        Log.w("Finn", "Pause")
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        kontoListPresenter.requestFromWS()
    }

    private fun showSnackbar(title: String, withAction: Boolean) {
        if (withAction) {
            val snackbar: Snackbar =
                Snackbar.make(
                    findViewById(R.id.layoutKonto_List),
                    title,
                    Snackbar.LENGTH_INDEFINITE
                )
            snackbar.setAction(
                "Retry!"
            ) { kontoListPresenter.requestFromWS() }
            snackbar.show()
        } else {
            val snackbar: Snackbar =
                Snackbar.make(this.layoutKonto_List, title, Snackbar.LENGTH_LONG)
            snackbar.show()
        }
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
        val adapter = KontoAdapter(ArrayList(kontoList), this)
        search_konto.visibility = View.VISIBLE
        rv_konto_list.adapter = adapter

        search_konto.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                adapter.filter.filter(newText)
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
        return super.onOptionsItemSelected(item)
    }
}
