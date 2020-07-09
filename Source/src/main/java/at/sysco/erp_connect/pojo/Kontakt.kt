package at.sysco.erp_connect.pojo

import org.simpleframework.xml.Element
import org.simpleframework.xml.Root

//Für Speicherung und Laden aus dem Webservice benötigt
//Beschreibt Aufbau eines Kontaktes/Ansprechpartners
//@Root beschreibt das Root-Element des Objektes.
@Root(name = "KontakteWebservice", strict = false)
data class Kontakt(
    //XML beiinhaltet ELement mit dem Namen "Kontaktnummer" usw.
    @field:Element(name = "Kontaktnummer")
    var kKontaktNumber: String? = null,

    @field:Element(name = "Name", required = false)
    var kLastName: String? = null,

    @field:Element(name = "Kontonummer", required = false)
    var kNumber: String? = null,

    @field:Element(name = "Vorname", required = false)
    var kFirstName: String? = null,

    @field:Element(name = "Funktion", required = false)
    var kFunction: String? = null,

    @field:Element(name = "Geschlecht", required = false)
    var kSex: String? = null,

    @field:Element(name = "Abteilung", required = false)
    var kAbteilung: String? = null,

    @field:Element(name = "Telefon1Land", required = false)
    var kTelCountry: String? = null,

    @field:Element(name = "Telefon1Vorwahl", required = false)
    var kTelCity: String? = null,

    @field:Element(name = "Telefon1Durchwahl", required = false)
    var kTelNumber: String? = null,

    @field:Element(name = "MobiltelefonLand", required = false)
    var kMobilCountry: String? = null,

    @field:Element(name = "MobiltelefonVorwahl", required = false)
    var kMobilOperator: String? = null,

    @field:Element(name = "MobiltelefonNummer", required = false)
    var kMobilNumber: String? = null,

    @field:Element(name = "eMailadresse", required = false)
    var kMail: String? = null,

    @field:Element(name = "Homepage", required = false)
    var kURL: String? = null
)
