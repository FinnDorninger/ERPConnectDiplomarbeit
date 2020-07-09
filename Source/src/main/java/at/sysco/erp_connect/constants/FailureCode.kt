package at.sysco.erp_connect.constants

//Beinhaltet Strings welche beschreiben welche Fehler im Model geschehen sind, für die Darstellung im View.
class FailureCode {
    companion object {
        const val NOT_SAVED = "Leider nicht gespeichert. Bitte erneut starten!."
        const val NO_CONNECTION = "Keine Daten vorhanden! Bitte starten Sie das Internet."
        const val ERROR_SAVING_FILE = "Speichern der lokalen Daten fehlgeschlagen."
        const val ERROR_LOADING_FILE = "Die lokal gespeicherten Daten konnten nicht geladen werden."
        const val NO_DATA = "Keine Verbindung zu Daten. Prüfen Sie ihre Einstellungen!"
        const val NO_DETAIL_NUMBER = "Fehler beim Starten, bitte kehre zurück!"
    }
}