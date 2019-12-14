package at.sysco.erp_connect.network

import okhttp3.OkHttpClient
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.*

class UnsafeHTTPClient {
    companion object {
        var conTimeout: Long = 5
        var readTimeout: Long = 5
        fun getUnsafeOkHttpClient(): OkHttpClient {
            try {
                // Create a trust manager that does not validate certificate chains
                /*
                val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
                    override fun getAcceptedIssuers(): Array<X509Certificate> {
                        return arrayOf()
                    }


                    @Throws(CertificateException::class)
                    override fun checkClientTrusted(
                        chain: Array<java.security.cert.X509Certificate>,
                        authType: String
                    ) {
                    }

                    @Throws(CertificateException::class)
                    override fun checkServerTrusted(
                        chain: Array<java.security.cert.X509Certificate>,
                        authType: String
                    ) {
                    }
                })

                // Install the all-trusting trust manager
                val sslContext = SSLContext.getInstance("SSL")
                sslContext.init(null, trustAllCerts, java.security.SecureRandom())
                // Create an ssl socket factory with our all-trusting manager
                val sslSocketFactory = sslContext.getSocketFactory()
                */
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