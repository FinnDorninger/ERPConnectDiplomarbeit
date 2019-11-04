package at.sysco.erp_connect.adapter

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import at.sysco.erp_connect.R
import at.sysco.erp_connect.konto_detail.KontoDetailActivity
import at.sysco.erp_connect.pojo.Konto
import kotlinx.android.synthetic.main.konto_list_item.view.*

class KontoAdapter(exampleList: ArrayList<Konto>, val context: Context) :
    RecyclerView.Adapter<ViewHolder>(), Filterable {
    var kontoList: ArrayList<Konto> = exampleList
    var kontoListFull: ArrayList<Konto>

    init {
        this.kontoListFull = ArrayList(exampleList)
    }

    override fun getItemCount(): Int {
        return kontoList.size
    }

    fun removeAll() {
        if (!kontoList.isEmpty()) {
            kontoList.removeAll(kontoList)
        }
    }

    //Inflates the item views: ViewGroup?
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.konto_list_item,
                parent, false
            )
        )
    }

    // Binds each Konto in the ArrayList to a view
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder?.tvKontoName?.text = kontoList[position].kName
        holder?.tvKontoNumber?.text = kontoList[position].kNumber

        val intent = Intent(context, KontoDetailActivity::class.java)
        intent.putExtra("id", kontoList[position].kNumber)
        holder?.tvHolder.setOnClickListener { v -> context.startActivity(intent) }
    }

    override fun getFilter(): Filter {
        return exampleFilter
    }

    private var exampleFilter: Filter = object : Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            var filteredList = ArrayList<Konto>()

            if (constraint == null || constraint.length == 0) {
                filteredList.addAll(kontoListFull)
            } else {
                var filterPattern = constraint.toString().toLowerCase().trim()
                Log.w("Finn", filterPattern)

                for (konto in kontoListFull) {
                    if (konto.kName?.toLowerCase()!!.contains(filterPattern)) {
                        filteredList.add(konto)
                    }
                }
            }

            var results = FilterResults()
            results.values = filteredList
            return results
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            exampleList.clear()
            exampleList.addAll(results?.values as ArrayList<Konto>)
            notifyDataSetChanged()
        }
    }
}

class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    // Holds the TextView that will add each konto to
    val tvKontoName = view.tv_konto_name
    val tvKontoNumber = view.tv_konto_number
    val tvHolder = view.test
}