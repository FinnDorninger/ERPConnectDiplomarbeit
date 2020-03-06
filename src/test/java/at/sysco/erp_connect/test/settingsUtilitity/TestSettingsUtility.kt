package at.sysco.erp_connect.test.settingsUtilitity

import at.sysco.erp_connect.settings.SettingsUtility
import org.junit.Test
import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class TestSettingsUtility {
    @Test
    fun userInputValidationTimeout() {
        var resultPair: Pair<Boolean, Long> = Pair(true, 5)
        assertEquals(resultPair, SettingsUtility.checkInput("5"))
        assertEquals(resultPair, SettingsUtility.checkInput('5'))
        assertEquals(resultPair, SettingsUtility.checkInput(5))

        resultPair = Pair(false, 0)
        assertEquals(resultPair, SettingsUtility.checkInput(null))
        assertEquals(resultPair, SettingsUtility.checkInput(0))
        assertEquals(resultPair, SettingsUtility.checkInput(60))
        assertEquals(resultPair, SettingsUtility.checkInput("avc"))
    }

    @Test
    fun improveUserinputURL() {
        val result = "https://test.at/"
        assertEquals(result, SettingsUtility.improveURL("http://test.at/"))
        assertEquals(result, SettingsUtility.improveURL("http://test.at"))
        assertEquals(result, SettingsUtility.improveURL("https://test.at"))
        assertEquals(result, SettingsUtility.improveURL("test.at/"))
        assertEquals(result, SettingsUtility.improveURL("test.at"))
    }
}

