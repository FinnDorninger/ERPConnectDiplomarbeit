package at.sysco.erp_connect.konto_list

import android.R
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import at.sysco.erp_connect.adapter.KontoAdapter
import at.sysco.erp_connect.constants.FailureCode
import at.sysco.erp_connect.pojo.Konto
import at.sysco.erp_connect.model.KontoListModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_konto_list.*


class KontoListActivity : AppCompatActivity(),
    KontoListContract.View {
    private lateinit var kontoListPresenter: KontoListPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(at.sysco.erp_connect.R.layout.activity_konto_list)

        initRecyclerView()
        kontoListPresenter = KontoListPresenter(this, KontoListModel(this))
        kontoListPresenter.requestFromWS()
    }

    private fun showSnackbar(title: String, withAction: Boolean) {
        if (withAction) {
            val snackbar: Snackbar =
                Snackbar.make(findViewById(R.id.content), title, Snackbar.LENGTH_INDEFINITE)
            snackbar.setAction(
                "Retry!"
            ) { kontoListPresenter.requestFromWS() }
            snackbar.show()
        } else {
            val snackbar: Snackbar =
                Snackbar.make(findViewById(R.id.content), title, Snackbar.LENGTH_LONG)
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
}
