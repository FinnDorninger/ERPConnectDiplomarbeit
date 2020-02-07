package at.sysco.erp_connect.model

import android.content.Context
import android.net.ConnectivityManager
import android.util.Log
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.MasterKeys
import at.sysco.erp_connect.SharedPref
import at.sysco.erp_connect.constants.FailureCode
import at.sysco.erp_connect.constants.FinishCode
import at.sysco.erp_connect.konto_detail.KontoDetailContract
import at.sysco.erp_connect.network.WebserviceApi
import at.sysco.erp_connect.pojo.KontoList
import org.simpleframework.xml.core.PersistenceException
import org.simpleframework.xml.core.Persister
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileInputStream
import java.io.IOException

const val KONTO_FILE_NAME = "KontoFile.xml"

class KontoDetailModel(val context: Context) : KontoDetailContract.Model {
    private val keyGenParameterSpec = MasterKeys.AES256_GCM_SPEC
    private val masterKeyAlias = MasterKeys.getOrCreate(keyGenParameterSpec)

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
        val userPw = SharedPref.getUserPW(context)
        val userName = SharedPref.getUserName(context)
        val baseURL = SharedPref.getBaseURL(context)
        if (!baseURL.isNullOrBlank() && userName != null && userPw != null) {
            val call = WebserviceApi.Factory.getApi(baseURL).getKonto(userPw, userName, kontoNummer)

            call.enqueue(object : Callback<KontoList> {
                override fun onResponse(call: Call<KontoList>, response: Response<KontoList>) {
                    val responseKontoList = response.body()?.kontenList

                    if (responseKontoList != null) {
                        onFinishedListener.onfinished(
                            responseKontoList[0],
                            FinishCode.finishedOnWeb
                        )
                        Log.w("Test", call.request().url().toString())
                    } else {
                        tryLoadingFromFile(onFinishedListener, kontoNummer)
                    }
                }

                override fun onFailure(call: Call<KontoList>, t: Throwable) {
                    tryLoadingFromFile(onFinishedListener, kontoNummer)
                }
            })
        } else {
            onFinishedListener.onFailure(FailureCode.NO_DATA)
        }
    }

    private fun tryLoadingFromFile(
        onFinishedListener: KontoDetailContract.Model.OnFinishedListener,
        kontoNummer: String
    ) {
        Log.w("Finn", "hier komme ich rein")
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
        Log.w("Finn", "hier komme ich rein")
        val encFile = File(context.filesDir, KONTO_LIST_FILE_NAME)
        val encryptedFile = EncryptedFile.Builder(
            encFile,
            context,
            masterKeyAlias,
            EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
        ).build()
        lateinit var fileInputStream: FileInputStream

        try {
            fileInputStream = encryptedFile.openFileInput()
            val kontoList = Persister().read(KontoList::class.java, fileInputStream).kontenList
            if (kontoList != null) {
                val konto = kontoList.find { it.kNumber == kontoNummer }
                if (konto != null) {
                    Log.w("Finn", konto.kName)
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
                KONTO_LIST_FILE_NAME.removeFile()
                onFinishedListener.onFailure(FailureCode.ERROR_LOADING_FILE)
            } else {
                onFinishedListener.onFailure(FailureCode.NO_DATA)
            }
        } catch (e: PersistenceException) {
            KONTO_LIST_FILE_NAME.removeFile()
            onFinishedListener.onFailure(FailureCode.ERROR_LOADING_FILE)
        } finally {
            fileInputStream.close()
        }
    }

    private fun String.removeFile() {
        when {
            this.doesFileExist() -> context.deleteFile(this)
        }
    }
}