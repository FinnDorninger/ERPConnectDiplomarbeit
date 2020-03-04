package at.sysco.erp_connect.instrumented.pojoUtilitity

import at.sysco.erp_connect.pojo.KontoUtility
import at.sysco.erp_connect.pojo.Konto
import org.junit.Test
import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class TestKontoUtility {
    @Test
    fun createMobileNumber() {
        val konto = Konto("1")
        konto.kMobilCountry = "0043"
        konto.kMobilOperatorTel = "0660"
        konto.kMobilTel = "1229345"
        val fullNumber = KontoUtility.createMobilNumber(konto)
        assertEquals("004306601229345", fullNumber)
    }

    @Test
    fun createMobileNumberNoCountry() {
        val konto = Konto("1")
        konto.kMobilCountry = ""
        konto.kMobilOperatorTel = "0660"
        konto.kMobilTel = "1229345"
        val fullNumber = KontoUtility.createMobilNumber(konto)
        assertEquals("06601229345", fullNumber)
    }

    @Test
    fun createMobileNumberNoOperator() {
        val konto = Konto("1")
        konto.kMobilCountry = "0043"
        konto.kMobilTel = "1229345"
        konto.kMobilOperatorTel = ""
        val fullNumber = KontoUtility.createMobilNumber(konto)
        assertEquals("", fullNumber)
    }

    @Test
    fun createMobileNumberNoMain() {
        val konto = Konto("1")
        konto.kMobilCountry = "0043"
        konto.kMobilOperatorTel = "0660"
        konto.kMobilTel = ""
        val fullNumber = KontoUtility.createMobilNumber(konto)
        assertEquals("", fullNumber)
    }

    @Test
    fun createMobileNumberNullCountry() {
        val konto = Konto("1")
        konto.kMobilCountry = null
        konto.kMobilOperatorTel = "0660"
        konto.kMobilTel = "1229345"
        val fullNumber = KontoUtility.createMobilNumber(konto)
        assertEquals("06601229345", fullNumber)
    }

    @Test
    fun createMobileNumberNullOperator() {
        val konto = Konto("1")
        konto.kMobilCountry = "0043"
        konto.kMobilOperatorTel = null
        konto.kMobilTel = "1229345"
        val fullNumber = KontoUtility.createMobilNumber(konto)
        assertEquals("", fullNumber)
    }

    @Test
    fun createMobileNumberNullOnlyMain() {
        val konto = Konto("1")
        konto.kMobilCountry = "0043"
        konto.kMobilOperatorTel = "0660"
        konto.kMobilTel = null
        val fullNumber = KontoUtility.createMobilNumber(konto)
        assertEquals("", fullNumber)
    }

    @Test
    fun createMobilTelBothEmptyOnlyMain() {
        val konto = Konto("1")
        konto.kMobilCountry = ""
        konto.kMobilOperatorTel = ""
        konto.kMobilTel = "5009123"
        val fullNumber = KontoUtility.createMobilNumber(konto)
        assertEquals("", fullNumber)
    }

    @Test
    fun createMobilTelEverythingEmpty() {
        val konto = Konto("1")
        konto.kMobilCountry = ""
        konto.kMobilOperatorTel = ""
        konto.kMobilTel = ""
        val fullNumber = KontoUtility.createMobilNumber(konto)
        assertEquals("", fullNumber)
    }

    @Test
    fun createMobilTelEverythingNull() {
        val konto = Konto("1")
        val fullNumber = KontoUtility.createMobilNumber(konto)
        assertEquals("", fullNumber)
    }

    ///

    @Test
    fun createTelNumber() {
        val konto = Konto("1")
        konto.kTelCountry = "0043"
        konto.kTelCity = "0660"
        konto.kTelMain = "1229345"
        val fullNumber = KontoUtility.createFullNumber(konto)
        assertEquals("004306601229345", fullNumber)
    }

    @Test
    fun createTelNumberNoCountry() {
        val konto = Konto("1")
        konto.kTelCountry = ""
        konto.kTelCity = "0660"
        konto.kTelMain = "1229345"
        val fullNumber = KontoUtility.createFullNumber(konto)
        assertEquals("06601229345", fullNumber)
    }

    @Test
    fun createTelNumberNoOperator() {
        val konto = Konto("1")
        konto.kTelCountry = "0043"
        konto.kTelCity = "1229345"
        konto.kTelMain = ""
        val fullNumber = KontoUtility.createFullNumber(konto)
        assertEquals("", fullNumber)
    }

    @Test
    fun createTelNumberNoMain() {
        val konto = Konto("1")
        konto.kTelCountry = "0043"
        konto.kTelCity = "0660"
        konto.kTelMain = ""
        val fullNumber = KontoUtility.createFullNumber(konto)
        assertEquals("", fullNumber)
    }

    @Test
    fun createTelNumberNullCountry() {
        val konto = Konto("1")
        konto.kTelCountry = null
        konto.kTelCity = "0660"
        konto.kTelMain = "1229345"
        val fullNumber = KontoUtility.createFullNumber(konto)
        assertEquals("06601229345", fullNumber)
    }

    @Test
    fun createTelNumberNullOperator() {
        val konto = Konto("1")
        konto.kTelCity = "0043"
        konto.kTelCity = null
        konto.kTelMain = "1229345"
        val fullNumber = KontoUtility.createFullNumber(konto)
        assertEquals("", fullNumber)
    }

    @Test
    fun createTelNumberNullOnlyMain() {
        val konto = Konto("1")
        konto.kTelCountry = "0043"
        konto.kTelCity = "0660"
        konto.kTelMain = null
        val fullNumber = KontoUtility.createFullNumber(konto)
        assertEquals("", fullNumber)
    }

    @Test
    fun createTelBothEmptyOnlyMain() {
        val konto = Konto("1")
        konto.kTelCountry = ""
        konto.kTelCity = ""
        konto.kTelMain = "5009123"
        val fullNumber = KontoUtility.createFullNumber(konto)
        assertEquals("", fullNumber)
    }

    @Test
    fun createTelEverythingEmpty() {
        val konto = Konto("1")
        konto.kTelCountry = ""
        konto.kTelCity = ""
        konto.kTelMain = ""
        val fullNumber = KontoUtility.createMobilNumber(konto)
        assertEquals("", fullNumber)
    }

    @Test
    fun createTelEverythingNull() {
        val konto = Konto("1")
        val fullNumber = KontoUtility.createFullNumber(konto)
        assertEquals("", fullNumber)
    }
}

