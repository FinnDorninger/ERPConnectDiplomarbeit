package at.sysco.erp_connect.network

import at.sysco.erp_connect.pojo.KontoList
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.SimpleXmlConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface KontoApi {
    //TO-DO: "getKontoDetail(KontoNummer)" -> Darstellung KontoDetails
    //TO-DO-Später: SharedPreferences -> User/Passwort/BaseURL
    @GET("/ewlservice/export?User=meso&Company=300M&Type=1&Vorlage=KontenWebservice&Key=FILTERWSKonten")
    fun getKontoList(@Query("Password") pw: String, @Query("User") user: String): Call<KontoList>

    @GET("/ewlservice/export?User=meso&Company=300M&Password=meso&Type=1&Vorlage=KontenWebservice")
    fun getKonto(@Query("Password") pw: String, @Query("User") user: String, @Query("Key") kontoNummer: String): Call<KontoList>

    object Factory {
        fun create(baseURL: String): KontoApi {
            val retrofit = Retrofit.Builder()
                .baseUrl(baseURL)
                .client(UnsafeHTTPClient.getUnsafeOkHttpClient())
                .addConverterFactory(SimpleXmlConverterFactory.create())
                .build()
            return retrofit.create(KontoApi::class.java)
        }
    }
}