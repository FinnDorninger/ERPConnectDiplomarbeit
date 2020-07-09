package at.sysco.erp_connect.pojo

object KontoUtility {
    fun createFullNumber(konto: Konto): String {
        val telNumber = konto.kTelMain
        val telNumberCity = konto.kTelCity
        val telNumberCountry = konto.kTelCountry
        var completeNumber = ""

        if (!telNumber.isNullOrBlank() && !telNumberCity.isNullOrBlank()) {
            completeNumber = "$telNumberCity$telNumber"
            if (!telNumberCountry.isNullOrBlank()) {
                completeNumber = "$telNumberCountry$completeNumber"
            }
        }
        return completeNumber
    }

    fun createMobilNumber(konto: Konto): String {
        var mobilComplete = ""
        val telMobilNumber = konto.kMobilTel
        val telMobilProvider = konto.kMobilOperatorTel
        val telMobilCountry = konto.kMobilCountry

        if (!telMobilNumber.isNullOrBlank() && !telMobilProvider.isNullOrBlank()) {
            mobilComplete = "$telMobilProvider$telMobilNumber"
            if (!telMobilCountry.isNullOrBlank()) {
                mobilComplete = "$telMobilCountry$mobilComplete"
            }
        }
        return mobilComplete
    }
}