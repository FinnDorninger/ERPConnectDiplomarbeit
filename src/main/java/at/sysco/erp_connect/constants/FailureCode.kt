package at.sysco.erp_connect.constants

class FailureCode {
    companion object {
        const val NOT_ENOUGH_SPACE = "Nicht genug Speicher! Offline-Daten wurden nicht gespeichert."
        const val ERROR_SAVING_FILE = "Speichern der lokalen Daten fehlgeschlagen."
        const val ERROR_LOADING_FILE = "Die lokal gespeicherten Daten konnten nicht geladen werden."
        const val NO_FILE = "Keine Offline-Daten vorhanden! Bitte Daten herunterladen."
    }
}