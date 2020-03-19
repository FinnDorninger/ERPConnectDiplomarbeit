package at.sysco.erp_connect.adapter

import android.content.Context
import android.content.Intent
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

//Adapter welcher beschreibt wie Daten an Recyclerview gebunden werden sollen
class KontoAdapter(kontoList: ArrayList<Konto>, val context: Context) :
    RecyclerView.Adapter<ViewHolder>(), Filterable {
    var kontoList: ArrayList<Konto> = kontoList
    var kontoListFull: ArrayList<Konto>

    init {
        this.kontoListFull = ArrayList(kontoList)
    }

    //Liefert die aktuelle Größe der Recyclerview-Liste
    override fun getItemCount(): Int {
        return kontoList.size
    }

    //Legt Layout eines Recyclerview-Eintrages fest
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.konto_list_item,
                parent, false
            )
        )
    }

    //Füllt die einzelnen Einträge eines Recyclerviews mit Daten
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.tvKontoName?.text = kontoList[position].kName
        holder.tvKontoNumber?.text = kontoList[position].kNumber

        val intent = Intent(context, KontoDetailActivity::class.java)
        intent.putExtra("id", kontoList[position].kNumber)
        holder.tvHolder.setOnClickListener { v -> context.startActivity(intent) }
    }

    override fun getFilter(): Filter {
        return rvFilter
    }

    //Methode zum löschen des Inhaltes des Recyclerviews
    fun clearAll() {
        val sizeListFull = kontoListFull.size
        kontoList.clear()
        kontoListFull.clear()
        notifyItemRangeRemoved(0, sizeListFull)
    }

    //Filter, beschreibt wie gefiltert werden soll
    private var rvFilter: Filter = object : Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            var filteredList = ArrayList<Konto>()

            if (constraint == null || constraint.isEmpty()) {
                filteredList.addAll(kontoListFull)
            } else {
                var filterPattern = constraint.toString().toLowerCase().trim()
                for (konto in kontoListFull) {
                    val kontoName = konto.kName
                    val kontoNumber = konto.kNumber
                    if (kontoName != null) {
                        if (kontoName.toLowerCase().contains(filterPattern)) {
                            filteredList.add(konto)
                        }
                    }
                    if (kontoNumber != null) {
                        if (kontoNumber.toLowerCase().contains(filterPattern)) {
                            filteredList.add(konto)
                        }
                    }
                }
            }

            var results = FilterResults()
            results.values = filteredList
            return results
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            kontoList.clear()
            kontoList.addAll(results?.values as ArrayList<Konto>)
            notifyDataSetChanged()
        }
    }
}

//ViewHolder beschreibt das Layout der einzelnen Einträge
class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    // Holds the TextView that will add each konto to
    val tvKontoName = view.tv_konto_name
    val tvKontoNumber = view.tv_konto_number
    val tvHolder = view.test
}