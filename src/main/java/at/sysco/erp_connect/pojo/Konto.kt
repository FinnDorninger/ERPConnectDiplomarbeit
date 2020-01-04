package at.sysco.erp_connect.pojo

import org.simpleframework.xml.Element
import org.simpleframework.xml.Root

@Root(name = "KontenWebservice", strict = false)
data class Konto(
    @field:Element(name = "Kontonummer")
    var kNumber: String? = null,

    @field:Element(name = "Kontoname", required = false)
    var kName: String? = null,

    @field:Element(name = "Staat", required = false)
    var kCountry: String? = null,

    @field:Element(name = "Postleitzahl", required = false)
    var kPlz: String? = null, //PA in WS-Response!

    @field:Element(name = "Ort", required = false)
    var kCity: String? = null,

    @field:Element(name = "Strasse", required = false)
    var kStreet: String? = null,

    @field:Element(name = "Landesvorwahl", required = false)
    var kTelCountry: String? = null,

    @field:Element(name = "Ortsvorwahl", required = false)
    var kTelCity: String? = null, //(01)

    @field:Element(name = "Telefon", required = false)
    var kTelMain: String? = null, //866-0

    @field:Element(name = "LandesvorwahlMobiltelefonnummer", required = false)
    var kMobilCountry: String? = null,

    @field:Element(name = "BetreibervorwahlMobiltelefonnummer", required = false)
    var kMobilOperatorTel: String? = null,

    @field:Element(name = "Mobiltelefonnummer", required = false)
    var kMobilTel: String? = null,

    @field:Element(name = "E-Mail-Adresse", required = false)
    var kMail: String? = null,

    @field:Element(name = "WWW-Adresse", required = false)
    var kUrl: String? = null,

    @field:Element(name = "Notiz", required = false)
    var kNote: String? = null,

    var kTelComplete: String? = null,
    var kMobilComplete: String? = null
)