package at.sysco.erp_connect.pojo

import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Root

@Root(name = "MESOWebService", strict = false)
data class KontakteList(
    @field:ElementList(name = "KontakteWebservice", inline = true)
    var kontakteList: List<Kontakt>? = null
)
