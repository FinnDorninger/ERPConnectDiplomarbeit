package at.sysco.erp_connect.network

import at.sysco.erp_connect.pojo.KontoList
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.SimpleXmlConverterFactory
import retrofit2.http.GET

interface KontoApi {
    //TO-DO: "getKontoDetail(KontoNummer)" -> Darstellung KontoDetails
    //TO-DO-Später: SharedPreferences -> User/Passwort/BaseURL
    @GET("/ewlservice/export?User=meso&Company=300M&Password=meso&Type=1&Vorlage=KontenWebservice&Key=FILTERWSKonten")
    fun getKontoList(): Call<KontoList>

    object Factory {
        fun create(): KontoApi {
            val retrofit = Retrofit.Builder()
                .baseUrl("https://83.164.140.68:13443/")
                .client(UnsafeHTTPClient.getUnsafeOkHttpClient())
                .addConverterFactory(SimpleXmlConverterFactory.create())
                .build()
            return retrofit.create<KontoApi>(KontoApi::class.java)
        }
    }
}