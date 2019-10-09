package at.sysco.erp_connect.constants

class FailureCode {
    companion object {
        const val DAMAGED_FILE = "DAMAGED_FILE" //Remove(in Methode), new Load
        const val FAILED_CONNECTION = "FAILED_CONNECTION" //Retry Button anzeigen
        const val NO_FILE = "NO_FILE" //New loading!
    }
}