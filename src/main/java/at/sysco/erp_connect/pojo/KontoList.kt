package at.sysco.erp_connect.pojo

import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Root

//Das Wurzel-Element des XML-Dokumentes aus dem Webservice lautet "MESOWebSevice"
@Root(name = "MESOWebService", strict = false)
data class KontoList(
    //Beeinhaltet Liste von Elementen mit dem Namen "KontenWebservice"
    @field:ElementList(name = "KontenWebservice", inline = true)
    var kontenList: List<Konto>? = null
)