package at.sysco.erp_connect.network

import okhttp3.OkHttpClient
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.*

//Eigener HTTP-Client inklusiver Methode zur Setzung des Timeouts
class HTTPClient {
    companion object {
        var conTimeout: Long = 5
        var readTimeout: Long = 5
        fun getOkHttpClient(): OkHttpClient {
            try {
                val builder = OkHttpClient.Builder()
                //builder.sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)
                builder.hostnameVerifier(object : HostnameVerifier {
                    override fun verify(p0: String?, p1: SSLSession?): Boolean {
                        if (p0 != null) {
                            if (p0 == "83.164.140.68") {
                                return true
                            }
                        }
                        return false
                    }
                })

                //Setzt Timeout
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