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
import at.sysco.erp_connect.kontakte_detail.KontakteDetailActivity
import at.sysco.erp_connect.pojo.Kontakt
import kotlinx.android.synthetic.main.kontakt_list_item.view.*

//Adapter welcher beschreibt wie Daten an Recyclerview gebunden werden sollen
class KontakteListAdapter(kontaktList: ArrayList<Kontakt>, val context: Context) :
    RecyclerView.Adapter<ViewHolderKontakt>(), Filterable {
    var kontaktList: ArrayList<Kontakt> = kontaktList
    var kontaktListFull: ArrayList<Kontakt>

    init {
        this.kontaktListFull = ArrayList(kontaktList)
    }

    //Füllt die einzelnen Einträge eines Recyclerviews mit Daten
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

        //Auswahl eines Recyclerview-Eintrages startet die zugehörige Kontakt-Detailseite
        val intent = Intent(context, KontakteDetailActivity::class.java)
        intent.putExtra("id", kontaktList[position].kKontaktNumber)
        holder.tvHolder.setOnClickListener { v -> context.startActivity(intent) }
    }

    //Liefert die aktuelle Größe der Recyclerview-Liste
    override fun getItemCount(): Int {
        return kontaktList.size
    }

    //Legt Layout eines Recyclerview-Eintrages fest
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderKontakt {
        return ViewHolderKontakt(
            LayoutInflater.from(context).inflate(
                R.layout.kontakt_list_item,
                parent, false
            )
        )
    }

    override fun getFilter(): Filter {
        return kontaktFilter
    }

    fun getSubFilter(): Filter {
        return subFilter
    }

    private var subFilter: Filter = object : Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            var filteredList = ArrayList<Kontakt>()
            var filterPattern = constraint.toString().toLowerCase().trim()
            for (kontakt in kontaktListFull) {
                if (!kontakt.kFirstName.isNullOrEmpty()) {
                    if (kontakt.kFirstName!!.toLowerCase() == filterPattern) {
                        filteredList.add(kontakt)
                    }
                }
                if (!kontakt.kLastName.isNullOrEmpty()) {
                    if (kontakt.kLastName!!.toLowerCase() == filterPattern) {
                        filteredList.add(kontakt)
                    }
                }
                if (!kontakt.kNumber.isNullOrEmpty()) {
                    if (kontakt.kNumber!!.toLowerCase() == filterPattern) {
                        filteredList.add(kontakt)
                    }
                }
            }
            var results = FilterResults()
            results.values = filteredList
            return results
        }

        //Methode welche die gefilterte Liste veröffentlicht und die alte Liste cleared.
        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            kontaktList.clear()
            kontaktList.addAll(results?.values as ArrayList<Kontakt>)
            notifyDataSetChanged()
        }
    }

    //Filter, beschreibt wie gefiltert werden soll
    private var kontaktFilter: Filter = object : Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            var filteredList = ArrayList<Kontakt>()

            if (constraint == null || constraint.isEmpty()) {
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

        //Methode welche die gefilterte Liste veröffentlicht und die alte Liste cleared.
        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            kontaktList.clear()
            kontaktList.addAll(results?.values as ArrayList<Kontakt>)
            notifyDataSetChanged()
        }
    }
}

//ViewHolder beschreibt das Layout der einzelnen Einträge
class ViewHolderKontakt(view: View) : RecyclerView.ViewHolder(view) {
    val tvKontaktName = view.tv_kontakt_name
    val tvKontaktFunction = view.tv_kontakt_function
    val tvHolder = view.holder
}