package at.sysco.erp_connect.konto_list

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import at.sysco.erp_connect.adapter.KontoAdapter
import at.sysco.erp_connect.pojo.Konto
import at.sysco.erp_connect.model.KontoListModel
import kotlinx.android.synthetic.main.activity_konto_list.*


class KontoListActivity : AppCompatActivity(),
    KontoListContract.View {
    var kontoListPresenter = KontoListPresenter(this, KontoListModel(this))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(at.sysco.erp_connect.R.layout.activity_konto_list)

        search_konto.visibility = View.GONE
        kontoListPresenter = KontoListPresenter(this, KontoListModel(this))
        kontoListPresenter.requestFromWS()
    }

    override fun showProgress() {
        progressBar.visibility = View.VISIBLE
        search_konto.visibility = View.GONE
    }

    override fun hideProgress() {
        progressBar.visibility = View.GONE
    }

    override fun displayKontoListInRecyclerView(kontoArrayList: List<Konto>) {
        val test2: ArrayList<Konto> = ArrayList(kontoArrayList)
        val adapter = KontoAdapter(test2, this)

        try_again.visibility = View.GONE
        search_konto.visibility = View.VISIBLE

        rv_konto_list.layoutManager = LinearLayoutManager(this)
        rv_konto_list.addItemDecoration(DividerItemDecoration(rv_konto_list.context, 1))
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

    override fun showLoadingError() {
        try_again.visibility = View.VISIBLE
        try_again.setOnClickListener { kontoListPresenter.requestFromWS() }
    }
}
