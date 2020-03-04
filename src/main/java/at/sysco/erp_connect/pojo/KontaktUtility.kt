package at.sysco.erp_connect.pojo

object KontaktUtility {
    fun calculateSex(kSex: String?): String {
        var sex = when (kSex) {
            "1" -> "Weiblich"
            "0" -> "Männlich"
            else -> kSex
        }
        if (sex.isNullOrBlank()) {
            sex = "/"
        }
        return sex
    }

    fun calculateName(kontakt: Kontakt): String {
        val fullname: String
        if (!kontakt.kLastName.isNullOrBlank()) {
            if (!kontakt.kFirstName.isNullOrBlank()) {
                fullname = kontakt.kLastName.plus(" ").plus(kontakt.kFirstName)
            } else {
                fullname = kontakt.kLastName!!
            }
        } else {
            if (!kontakt.kFirstName.isNullOrBlank()) {
                fullname = kontakt.kFirstName!!
            } else {
                fullname = ""
            }
        }
        return fullname
    }

    fun calculateNumber(kontakt: Kontakt): String {
        val telNumber = kontakt.kTelNumber
        val telNumberCity = kontakt.kTelCity
        val telNumberCountry = kontakt.kTelCountry
        var completeNumber = ""

        if (!telNumber.isNullOrBlank() && !telNumberCity.isNullOrBlank()) {
            completeNumber = "$telNumberCity$telNumber"
            if (!telNumberCountry.isNullOrBlank()) {
                completeNumber = "$telNumberCountry$completeNumber"
            }
        }
        return completeNumber
    }
}