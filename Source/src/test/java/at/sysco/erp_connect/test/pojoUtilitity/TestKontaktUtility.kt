package at.sysco.erp_connect.test.pojoUtilitity

import at.sysco.erp_connect.pojo.KontaktUtility
import at.sysco.erp_connect.pojo.Kontakt
import org.junit.Test
import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class TestKontaktUtility {
    @Test
    fun createTelNumber() {
        val kontakt = Kontakt("1")
        kontakt.kTelCountry = "0043"
        kontakt.kTelCity = "0660"
        kontakt.kTelNumber = "1229345"
        val fullNumber = KontaktUtility.calculateNumber(kontakt)
        assertEquals("004306601229345", fullNumber)
    }

    @Test
    fun createTelNumberNoCountry() {
        val kontakt = Kontakt("1")
        kontakt.kTelCountry = ""
        kontakt.kTelCity = "0660"
        kontakt.kTelNumber = "1229345"
        val fullNumber = KontaktUtility.calculateNumber(kontakt)
        assertEquals("06601229345", fullNumber)
    }

    @Test
    fun createTelNumberNoOperator() {
        val kontakt = Kontakt("1")
        kontakt.kTelCountry = "0043"
        kontakt.kTelCity = "1229345"
        kontakt.kTelNumber = ""
        val fullNumber = KontaktUtility.calculateNumber(kontakt)
        assertEquals("", fullNumber)
    }

    @Test
    fun createTelNumberNoMain() {
        val kontakt = Kontakt("1")
        kontakt.kTelCountry = "0043"
        kontakt.kTelCity = "0660"
        kontakt.kTelNumber = ""
        val fullNumber = KontaktUtility.calculateNumber(kontakt)
        assertEquals("", fullNumber)
    }

    @Test
    fun createTelNumberNullCountry() {
        val kontakt = Kontakt("1")
        kontakt.kTelCountry = null
        kontakt.kTelCity = "0660"
        kontakt.kTelNumber = "1229345"
        val fullNumber = KontaktUtility.calculateNumber(kontakt)
        assertEquals("06601229345", fullNumber)
    }

    @Test
    fun createTelNumberNullOperator() {
        val kontakt = Kontakt("1")
        kontakt.kTelCity = "0043"
        kontakt.kTelCity = null
        kontakt.kTelNumber = "1229345"
        val fullNumber = KontaktUtility.calculateNumber(kontakt)
        assertEquals("", fullNumber)
    }

    @Test
    fun createTelNumberNullOnlyMain() {
        val kontakt = Kontakt("1")
        kontakt.kTelCountry = "0043"
        kontakt.kTelCity = "0660"
        kontakt.kTelNumber = null
        val fullNumber = KontaktUtility.calculateNumber(kontakt)
        assertEquals("", fullNumber)
    }

    @Test
    fun createTelBothEmptyOnlyMain() {
        val kontakt = Kontakt("1")
        kontakt.kTelCountry = ""
        kontakt.kTelCity = ""
        kontakt.kTelNumber = "5009123"
        val fullNumber = KontaktUtility.calculateNumber(kontakt)
        assertEquals("", fullNumber)
    }

    @Test
    fun createTelEverythingEmpty() {
        val kontakt = Kontakt("1")
        kontakt.kTelCountry = ""
        kontakt.kTelCity = ""
        kontakt.kTelNumber = ""
        val fullNumber = KontaktUtility.calculateNumber(kontakt)
        assertEquals("", fullNumber)
    }

    @Test
    fun createTelEverythingNull() {
        val kontakt = Kontakt("1")
        val fullNumber = KontaktUtility.calculateNumber(kontakt)
        assertEquals("", fullNumber)
    }

    @Test
    fun testSex() {
        assertEquals("Weiblich", KontaktUtility.calculateSex("1"))
        assertEquals("Männlich", KontaktUtility.calculateSex("0"))
        assertEquals("/", KontaktUtility.calculateSex(""))
        assertEquals("asfd", KontaktUtility.calculateSex("asfd"))
    }

    @Test
    fun testCalculateName() {
        val kontakt = Kontakt("")
        kontakt.kFirstName = "Finn"
        kontakt.kLastName = "Dorninger"
        assertEquals("Dorninger Finn", KontaktUtility.calculateName(kontakt))

        kontakt.kFirstName = ""
        kontakt.kLastName = "Dorninger"
        assertEquals("Dorninger", KontaktUtility.calculateName(kontakt))

        kontakt.kFirstName = "Finn"
        kontakt.kLastName = ""
        assertEquals("Finn", KontaktUtility.calculateName(kontakt))

        kontakt.kFirstName = ""
        kontakt.kLastName = ""
        assertEquals("", KontaktUtility.calculateName(kontakt))

        kontakt.kFirstName = null
        kontakt.kLastName = null
        assertEquals("", KontaktUtility.calculateName(kontakt))
    }
}

