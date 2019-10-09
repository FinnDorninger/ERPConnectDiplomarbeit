package at.sysco.erp_connect.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import at.sysco.erp_connect.R
import at.sysco.erp_connect.konto_detail.KontoDetailActivity
import at.sysco.erp_connect.pojo.Konto
import kotlinx.android.synthetic.main.konto_list_item.view.*

class KontoAdapter(val items: List<Konto>, val context: Context) :
    RecyclerView.Adapter<ViewHolder>() {
    // Gets the size of item
    override fun getItemCount(): Int {
        return items.size
    }

    // Inflates the item views
    //ViewGroup?
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.konto_list_item,
                parent,
                false
            )
        )
    }

    // Binds each Konto in the ArrayList to a view
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder?.tvKontoName?.text = items[position].kName
        holder?.tvKontoNumber?.text = items[position].kNumber

        val intent = Intent(context, KontoDetailActivity::class.java)
        intent.putExtra("id", items[position].kNumber)
        holder?.tvHolder.setOnClickListener { v -> context.startActivity(intent) }
    }
}

class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    // Holds the TextView that will add each konto to
    val tvKontoName = view.tv_konto_name
    val tvKontoNumber = view.tv_konto_number
    val tvHolder = view.test
}