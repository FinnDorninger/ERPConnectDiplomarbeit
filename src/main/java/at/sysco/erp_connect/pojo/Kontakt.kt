package at.sysco.erp_connect.pojo

import org.simpleframework.xml.Element
import org.simpleframework.xml.Root

@Root(name = "KontenWebservice", strict = false)
data class Kontakt(
    @field:Element(name = "Kontonummer")
    var kNumber: String? = null,

    @field:Element(name = "Vorname", required = false)
    var kVorName: String? = null,

    @field:Element(name = "Name", required = false)
    var kName: String? = null

)
