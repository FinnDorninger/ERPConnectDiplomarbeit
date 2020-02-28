package at.sysco.erp_connect.constants

//Beinhaltet Strings welche beschreiben wie das Model Daten bekommen hat, für die Darstellung im View.
class FinishCode {
    companion object {
        const val finishedSavingKontakte: String = "Kontakte geladen und gespeichert!"
        const val finishedSavingKonto: String = "Konten geladen und gespeichert!"
        const val finishedOnWeb = "Daten aktuell!"
        const val finishedOnFile = "Lokale Dateien geladen. Vielleicht nicht aktuell."
    }
}