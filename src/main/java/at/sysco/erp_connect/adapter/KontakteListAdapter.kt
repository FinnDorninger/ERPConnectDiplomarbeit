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
import at.sysco.erp_connect.kontakte_detail.KontakteDetailActivity
import at.sysco.erp_connect.pojo.Kontakt
import kotlinx.android.synthetic.main.activity_kontakte_detail.*
import kotlinx.android.synthetic.main.kontakt_list_item.view.*

class KontakteListAdapter(exampleList: ArrayList<Kontakt>, val context: Context) :
    RecyclerView.Adapter<ViewHolderKontakt>(), Filterable {
    var kontaktList: ArrayList<Kontakt> = exampleList
    var kontaktListFull: ArrayList<Kontakt>

    init {
        this.kontaktListFull = ArrayList(exampleList)
    }

    override fun onBindViewHolder(holder: ViewHolderKontakt, position: Int) {
        var name = kontaktList[position].kLastName
        val nameFirstname = kontaktList[position].kFirstName
        var task = kontaktList[position].kAbteilung

        if (name != null) {
            if (nameFirstname != null) {
                name = name.plus(" ").plus(nameFirstname)
            }
        } else {
            if (nameFirstname != null) {
                name = nameFirstname
            } else {
                name = ""
            }
        }

        if (task.isNullOrEmpty()) {
            task = kontaktList[position].kFunction
        } else if (!kontaktList[position].kFunction.isNullOrEmpty()) {
            task = task.plus(" | ").plus(kontaktList[position].kFunction)
        }

        holder.tvKontaktName?.text = name
        holder.tvKontaktFunction?.text = task

        val intent = Intent(context, KontakteDetailActivity::class.java)
        intent.putExtra("id", kontaktList[position].kKontaktNumber)
        holder.tvHolder.setOnClickListener { v -> context.startActivity(intent) }
    }

    override fun getItemCount(): Int {
        return kontaktList.size
    }

    //Inflates the item views: ViewGroup?
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderKontakt {
        return ViewHolderKontakt(
            LayoutInflater.from(context).inflate(
                R.layout.kontakt_list_item,
                parent, false
            )
        )
    }

    override fun getFilter(): Filter {
        return exampleFilter
    }

    fun clearAll() {
        val sizeListFull = kontaktListFull.size
        kontaktList.clear()
        kontaktListFull.clear()
        notifyItemRangeRemoved(0, sizeListFull)
    }

    private var exampleFilter: Filter = object : Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            var filteredList = ArrayList<Kontakt>()

            if (constraint == null || constraint.length == 0) {
                filteredList.addAll(kontaktListFull)
            } else {
                var filterPattern = constraint.toString().toLowerCase().trim()

                for (kontakt in kontaktListFull) {
                    if (!kontakt.kFirstName.isNullOrEmpty()) {
                        if (kontakt.kFirstName!!.toLowerCase().contains(filterPattern)) {
                            filteredList.add(kontakt)
                        }
                    }
                    if (!kontakt.kLastName.isNullOrEmpty()) {
                        if (kontakt.kLastName!!.toLowerCase().contains(filterPattern)) {
                            filteredList.add(kontakt)
                        }
                    }
                    if (!kontakt.kNumber.isNullOrEmpty()) {
                        if (kontakt.kNumber!!.toLowerCase().contains(filterPattern)) {
                            filteredList.add(kontakt)
                        }
                    }
                }
            }
            var results = FilterResults()
            results.values = filteredList
            return results
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            exampleList.clear()
            exampleList.addAll(results?.values as ArrayList<Kontakt>)
            notifyDataSetChanged()
        }
    }
}

class ViewHolderKontakt(view: View) : RecyclerView.ViewHolder(view) {
    val tvKontaktName = view.tv_kontakt_name
    val tvKontaktFunction = view.tv_kontakt_function
    val tvHolder = view.holder
}