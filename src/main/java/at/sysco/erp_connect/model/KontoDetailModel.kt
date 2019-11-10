package at.sysco.erp_connect.model

import android.content.Context
import android.net.ConnectivityManager
import android.util.Log
import android.util.Patterns
import androidx.preference.PreferenceManager
import at.sysco.erp_connect.constants.FailureCode
import at.sysco.erp_connect.constants.FinishCode
import at.sysco.erp_connect.konto_detail.KontoDetailContract
import at.sysco.erp_connect.konto_list.KontoListContract
import at.sysco.erp_connect.network.KontoApi
import at.sysco.erp_connect.pojo.KontoList
import org.simpleframework.xml.core.PersistenceException
import org.simpleframework.xml.core.Persister
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import java.io.File
import java.io.FileInputStream
import java.io.IOException

const val KONTO_FILE_NAME = "KontoFile.xml"

class KontoDetailModel(val context: Context) : KontoDetailContract.Model {
    override fun getKontoDetail(
        onFinishedListener: KontoDetailContract.Model.OnFinishedListener,
        kontoNummer: String
    ) {
        when {
            checkInternetConnection(context) -> loadDataFromWebservice(
                onFinishedListener,
                kontoNummer
            )
            KONTO_FILE_NAME.doesFileExist() -> loadKontoDetailFromFile(
                onFinishedListener,
                kontoNummer
            )
            else -> onFinishedListener.onFailure(FailureCode.NO_DATA)
        }
    }

    private fun String.doesFileExist(): Boolean {
        if (context.fileList().contains(this)) {
            return true
        }
        return false
    }

    private fun checkInternetConnection(context: Context): Boolean {
        val connectivity =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val info = connectivity.allNetworks
        for (i in info.indices) {
            if (info[i] != null && connectivity.getNetworkInfo(info[i])!!.isConnected) {
                return true
            }
        }
        return false
    }

    private fun loadDataFromWebservice(
        onFinishedListener: KontoDetailContract.Model.OnFinishedListener,
        kontoNummer: String
    ) {
        val retrofit = Retrofit.Builder()
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
        val userName = sharedPref.getString("user_name", "")
        val userPW = sharedPref.getString("user_password", "")
        var baseURL = sharedPref.getString("base_url", "")
        baseURL = modifyURL(baseURL)

        if (checkURL(baseURL) && !userName.isNullOrEmpty() && !userPW.isNullOrEmpty()) {
            val call = KontoApi.Factory.create(baseURL!!).getKonto(userPW, userName, kontoNummer)

            call.enqueue(object : Callback<KontoList> {
                override fun onResponse(call: Call<KontoList>, response: Response<KontoList>) {
                    val responseKontoList = response.body()?.kontenList

                    if (responseKontoList != null) {
                        Log.w("Test", responseKontoList[0].kName)
                        onFinishedListener.onfinished(
                            responseKontoList[0],
                            FinishCode.finishedOnWeb
                        )
                    } else {
                        tryLoadingFromFile(onFinishedListener, kontoNummer)
                    }
                }

                override fun onFailure(call: Call<KontoList>, t: Throwable) {
                    tryLoadingFromFile(onFinishedListener, kontoNummer)
                }
            })
        } else {
            tryLoadingFromFile(onFinishedListener, kontoNummer)
        }
    }

    private fun tryLoadingFromFile(
        onFinishedListener: KontoDetailContract.Model.OnFinishedListener,
        kontoNummer: String
    ) {
        if (KONTO_LIST_FILE_NAME.doesFileExist()) {
            loadKontoDetailFromFile(onFinishedListener, kontoNummer)
        } else {
            if (checkInternetConnection(context)) {
                onFinishedListener.onFailure(FailureCode.NO_DATA)
            } else {
                onFinishedListener.onFailure(FailureCode.NO_CONNECTION)
            }
        }
    }

    private fun loadKontoDetailFromFile(
        onFinishedListener: KontoDetailContract.Model.OnFinishedListener,
        kontoNummer: String
    ) {
        val path = context.filesDir.toString() + "/" + KONTO_FILE_NAME
        var inputStream: FileInputStream? = null
        try {
            inputStream = File(path).inputStream()
            val kontoList = Persister().read(KontoList::class.java, inputStream).kontenList
            if (kontoList != null) {
                val konto = kontoList.find { it.kNumber == kontoNummer }
                if (konto != null) {
                    onFinishedListener.onfinished(konto, FinishCode.finishedOnFile)
                } else {
                    loadDataFromWebservice(onFinishedListener, kontoNummer)
                }
            } else {
                onFinishedListener.onFailure(FailureCode.ERROR_LOADING_FILE)
            }
        } catch (e: IOException) {
            //Exception: File does not exist or is corrupt
            if (KONTO_FILE_NAME.doesFileExist()) {
                removeFile()
                onFinishedListener.onFailure(FailureCode.ERROR_LOADING_FILE)
            } else {
                onFinishedListener.onFailure(FailureCode.NO_DATA)
            }
        } catch (e: PersistenceException) {
            removeFile()
            onFinishedListener.onFailure(FailureCode.ERROR_LOADING_FILE)
        } finally {
            inputStream?.close()
        }
    }

    private fun removeFile() {
        when {
            KONTO_LIST_FILE_NAME.doesFileExist() -> context.deleteFile(KONTO_LIST_FILE_NAME)
        }
    }

    private fun modifyURL(baseURL: String?): String? {
        var newURL: String

        if (baseURL.isNullOrEmpty()) {
            return ""
        } else {
            newURL = baseURL
            if (newURL.startsWith("http://") || newURL.startsWith("https://")) {
                Log.w("Test", "Ich checke null")
                return newURL
            } else {
                newURL = "https://".plus(newURL)
                return newURL
            }
        }
    }

    private fun checkURL(baseURL: String?): Boolean {
        return Patterns.WEB_URL.matcher(baseURL).matches()
    }
}