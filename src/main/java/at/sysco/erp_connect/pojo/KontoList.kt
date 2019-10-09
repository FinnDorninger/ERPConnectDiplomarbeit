package at.sysco.erp_connect.pojo

import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Root

@Root(name = "MESOWebService", strict = false)
data class KontoList(
    @field:ElementList(name = "KontenWebservice", inline = true)
    var kontenList: List<Konto>? = null
)