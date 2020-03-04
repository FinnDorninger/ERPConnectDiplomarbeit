package at.sysco.erp_connect.model

import android.content.Context
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.MasterKeys
import at.sysco.erp_connect.settings.SharedPref
import at.sysco.erp_connect.constants.FailureCode
import at.sysco.erp_connect.constants.FinishCode
import at.sysco.erp_connect.konto_detail.KontoDetailContract
import at.sysco.erp_connect.model.ModelUtitlity.checkInternetConnection
import at.sysco.erp_connect.model.ModelUtitlity.doesFileExist
import at.sysco.erp_connect.model.ModelUtitlity.removeFile
import at.sysco.erp_connect.network.WebserviceApi
import at.sysco.erp_connect.pojo.KontoList
import org.simpleframework.xml.core.Persister
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileInputStream
import java.lang.Exception

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
            doesFileExist(context, KONTO_FILE_NAME) -> loadKontoDetailFromFile(
                onFinishedListener,
                kontoNummer
            )
            else -> onFinishedListener.onFailure(FailureCode.NO_CONNECTION)
        }
    }

    private fun loadDataFromWebservice(
        onFinishedListener: KontoDetailContract.Model.OnFinishedListener,
        kontoNummer: String
    ) {

        val userPw = SharedPref.getUserPW(context)
        val userName = SharedPref.getUserName(context)
        val baseURL = SharedPref.getBaseURL(context)
        if (!baseURL.isNullOrBlank() && userName != null && userPw != null) {
            val api = WebserviceApi.Factory.getApi(baseURL)
            if (api != null) {
                val call = api.getKonto(userPw, userName, kontoNummer)
                call.enqueue(object : Callback<KontoList> {
                    override fun onResponse(call: Call<KontoList>, response: Response<KontoList>) {
                        val responseKontoList = response.body()?.kontenList

                        if (responseKontoList != null) {
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
                onFinishedListener.onFailure(FailureCode.NO_DATA)
            }
        } else {
            onFinishedListener.onFailure(FailureCode.NO_DATA)
        }

    }

    private fun tryLoadingFromFile(
        onFinishedListener: KontoDetailContract.Model.OnFinishedListener,
        kontoNummer: String
    ) {
        if (doesFileExist(context, KONTO_FILE_NAME)) {
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
                    onFinishedListener.onfinished(konto, FinishCode.finishedOnFile)
                } else {
                    onFinishedListener.onFailure(FailureCode.NOT_SAVED)
                }
            } else {
                onFinishedListener.onFailure(FailureCode.ERROR_LOADING_FILE)
            }
        } catch (e: Exception) {
            if (doesFileExist(context, KONTO_FILE_NAME)) {
                removeFile(context, KONTO_LIST_FILE_NAME)
                onFinishedListener.onFailure(FailureCode.ERROR_LOADING_FILE)
            } else {
                onFinishedListener.onFailure(FailureCode.NO_DATA)
            }
        } finally {
            fileInputStream.close()
        }
    }
}