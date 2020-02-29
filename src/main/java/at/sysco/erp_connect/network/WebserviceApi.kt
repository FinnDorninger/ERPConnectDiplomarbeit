package at.sysco.erp_connect.network

import at.sysco.erp_connect.pojo.KontakteList
import at.sysco.erp_connect.pojo.KontoList
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.SimpleXmlConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

//Interface für die Erstellung der Retrofit-API
interface WebserviceApi {
    //Mit @Get Annotationen werden
    @GET("/ewlservice/export?Company=300M&Type=1&Vorlage=KontenWebservice&Key=FILTERWSKonten")
    fun getKontoList(@Query("Password") pw: String, @Query("User") user: String): Call<KontoList>

    @GET("/ewlservice/export?Company=300M&Type=1&Vorlage=KontenWebservice")
    fun getKonto(@Query("Password") pw: String, @Query("User") user: String, @Query("Key") kontoNummer: String): Call<KontoList>

    @GET("/ewlservice/export?Company=300M&Type=7&Vorlage=KontakteWebservice&Key=FILTERWSKontakte")
    fun getKontakteList(@Query("Password") pw: String, @Query("User") user: String): Call<KontakteList>

    @GET("/ewlservice/export?Company=300M&Type=7&Vorlage=KontakteWebservice&Key=FILTERWSKontakte")
    fun getKontakt(@Query("Password") pw: String, @Query("User") user: String, @Query("Key") kontoNummer: String): Call<KontakteList>

    object Factory {
        fun getApi(baseURL: String): WebserviceApi {
            val retrofit = Retrofit.Builder()
                .baseUrl(baseURL)
                .client(HTTPClient.getOkHttpClient())
                .addConverterFactory(SimpleXmlConverterFactory.create())
                .build()
            return retrofit.create(WebserviceApi::class.java)
        }
    }
}