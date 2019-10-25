package at.sysco.erp_connect.constants

class FailureCode {
    companion object {
        const val ERROR_SAVING_FILE = "Nicht genug Speicher um Daten lokal zu speichern!"
        const val ERROR_LOADING_FILE =
            "Die lokal gespeicherten Daten konnten leider nicht geladen werden." //Remove(in Methode), new Load
        const val NO_FILE = "Kein Internet und keine lokale Daten!" //New loading!
    }
}