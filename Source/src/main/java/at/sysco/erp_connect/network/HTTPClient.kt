package at.sysco.erp_connect.network

import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import javax.net.ssl.*

class HTTPClient {
    companion object {
        var conTimeout: Long = 5
        var readTimeout: Long = 5
        fun getOkHttpClient(): OkHttpClient {
            try {
                val builder = OkHttpClient.Builder()
                //Diese Methode wird nur aufgerufen, wenn ein Fehler bei der Verifizierung auftretet
                //Return true: Verbindung dennoch erlauben
                //Return false: Verbindung nicht erlauben
                builder.hostnameVerifier(object : HostnameVerifier {
                    override fun verify(p0: String?, p1: SSLSession?): Boolean {
                        val isHostVerified: Boolean
						isHostVerified = p0 == p1?.peerHost
                        return isHostVerified
                    }
                })
                return builder
                    .connectTimeout(conTimeout, TimeUnit.SECONDS)
                    .readTimeout(readTimeout, TimeUnit.SECONDS)
                    .build()
            } catch (e: Exception) {
                throw RuntimeException(e)
            }
        }
    }
}