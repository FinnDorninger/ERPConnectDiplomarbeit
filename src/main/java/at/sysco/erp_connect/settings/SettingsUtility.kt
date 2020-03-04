package at.sysco.erp_connect.settings

import java.lang.NumberFormatException

object SettingsUtility {
    fun checkInput(newValue: Any?): Pair<Boolean, Long> {
        var returnPair: Pair<Boolean, Long> = Pair(false, 0)
        try {
            val value = Integer.parseInt(newValue.toString()).toLong()
            if ((value > 0) && (value < 60)) {
                returnPair = Pair(true, value)
            }
        } catch (e: NumberFormatException) {
        }
        return returnPair
    }

    fun improveURL(oldURL: String): String {
        var newURL: String
        newURL = when {
            oldURL.startsWith("https://") -> {
                oldURL
            }
            oldURL.startsWith("http://") -> oldURL.replace("http://", "https://")
            else -> "https://".plus(oldURL)
        }
        if (!oldURL.endsWith("/")) {
            newURL = newURL.plus("/")
        }
        return newURL
    }
}