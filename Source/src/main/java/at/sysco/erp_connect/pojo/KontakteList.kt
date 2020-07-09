package at.sysco.erp_connect.pojo

import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Root

//Das Wurzel-Element des XML-Dokumentes aus dem Webservice lautet "MESOWebSevice"
@Root(name = "MESOWebService", strict = false)
data class KontakteList(
    //XML beiinhaltet eine Liste von Elementen mit dem Namen "KontakteWebservice" usw.
    @field:ElementList(name = "KontakteWebservice", inline = true)
    var kontakteList: List<Kontakt>? = null
)
