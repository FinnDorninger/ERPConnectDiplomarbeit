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
    var exampleList: ArrayList<Konto> = exampleList
    var exampleListFull: ArrayList<Konto>

    init {
        this.exampleListFull = ArrayList(exampleList)
    }

    override fun getItemCount(): Int {
        return exampleList.size
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
        holder?.tvKontoName?.text = exampleList[position].kName
        holder?.tvKontoNumber?.text = exampleList[position].kNumber

        val intent = Intent(context, KontoDetailActivity::class.java)
        intent.putExtra("id", exampleList[position].kNumber)
        holder?.tvHolder.setOnClickListener { v -> context.startActivity(intent) }
    }

    override fun getFilter(): Filter {
        return exampleFilter
    }

    private var exampleFilter: Filter = object : Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            var filteredList = ArrayList<Konto>()

            if (constraint == null || constraint.length == 0) {
                filteredList.addAll(exampleListFull)
            } else {
                var filterPattern = constraint.toString().toLowerCase().trim()
                Log.w("Finn", filterPattern)

                for (konto in exampleListFull) {
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