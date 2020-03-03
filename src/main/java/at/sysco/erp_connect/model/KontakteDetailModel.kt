package at.sysco.erp_connect.model

import android.content.Context
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.MasterKeys
import at.sysco.erp_connect.SharedPref
import at.sysco.erp_connect.constants.FailureCode
import at.sysco.erp_connect.constants.FinishCode
import at.sysco.erp_connect.kontakte_detail.KontakteDetailContract
import at.sysco.erp_connect.model.ModelUtitlity.checkInternetConnection
import at.sysco.erp_connect.model.ModelUtitlity.doesFileExist
import at.sysco.erp_connect.model.ModelUtitlity.removeFile
import at.sysco.erp_connect.network.WebserviceApi
import at.sysco.erp_connect.pojo.KontakteList
import org.simpleframework.xml.core.PersistenceException
import org.simpleframework.xml.core.Persister
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.lang.Exception

const val KONTAKTE_FILE_NAME = "KontakteFile.xml"

//Geschäftslogik der Ansprechpartnerdetails
class KontakteDetailModel(val context: Context) : KontakteDetailContract.Model {
    private val keyGenParameterSpec = MasterKeys.AES256_GCM_SPEC
    private val masterKeyAlias = MasterKeys.getOrCreate(keyGenParameterSpec)

    //Methode welche entscheidet welches Verfahren für die Beschaffung der Daten ausgeführt werden soll
    override fun getKontaktDetail(
        onFinishedListener: KontakteDetailContract.Model.OnFinishedListener,
        kontaktNummer: String
    ) {
        when {
            checkInternetConnection(context) -> loadDataFromWebservice(
                onFinishedListener,
                kontaktNummer
            )
            doesFileExist(context, KONTAKTE_FILE_NAME) -> loadKontaktDetailFromFile(
                onFinishedListener,
                kontaktNummer
            )
            else -> onFinishedListener.onFailure(FailureCode.NO_CONNECTION)
        }
    }

    //Ladet Daten aus dem Webservice
    private fun loadDataFromWebservice(
        onFinishedListener: KontakteDetailContract.Model.OnFinishedListener,
        kontaktNummer: String
    ) {
        val userPw = SharedPref.getUserPW(context)
        val userName = SharedPref.getUserName(context)
        val baseURL = SharedPref.getBaseURL(context)
        if (!baseURL.isNullOrBlank() && userName != null && userPw != null) {
            val api = WebserviceApi.Factory.getApi(baseURL)
            if (api != null) {
                val call = api.getKontakt(userPw, userName, kontaktNummer)
                call.enqueue(object : Callback<KontakteList> {
                    override fun onResponse(
                        call: Call<KontakteList>,
                        response: Response<KontakteList>
                    ) {
                        val responseKontakteList = response.body()?.kontakteList
                        if (responseKontakteList != null) {
                            onFinishedListener.onfinished(
                                responseKontakteList[0],
                                FinishCode.finishedOnWeb
                            )
                        } else {
                            tryLoadingFromFile(onFinishedListener, kontaktNummer)
                        }
                    }

                    override fun onFailure(call: Call<KontakteList>, t: Throwable) {
                        tryLoadingFromFile(onFinishedListener, kontaktNummer)
                    }
                })
            }
        }
        onFinishedListener.onFailure(FailureCode.NO_DATA)
    }

    //Prüft ob Laden aus dem Filesystem möglich ist
    private fun tryLoadingFromFile(
        onFinishedListener: KontakteDetailContract.Model.OnFinishedListener,
        kontaktNummer: String
    ) {
        if (doesFileExist(context, KONTAKTE_FILE_NAME)) {
            loadKontaktDetailFromFile(onFinishedListener, kontaktNummer)
        } else {
            if (checkInternetConnection(context)) {
                onFinishedListener.onFailure(FailureCode.NO_DATA)
            } else {
                onFinishedListener.onFailure(FailureCode.NO_CONNECTION)
            }
        }
    }

    //Ladet Ansprechpartner aus dem Filesystem
    private fun loadKontaktDetailFromFile(
        onFinishedListener: KontakteDetailContract.Model.OnFinishedListener,
        kontaktNummer: String
    ) {
        val encFile = File(context.filesDir, KONTAKTE_LIST_FILE_NAME)
        val encryptedFile = EncryptedFile.Builder(
            encFile,
            context,
            masterKeyAlias,
            EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
        ).build()
        lateinit var fileInputStream: FileInputStream
        try {
            fileInputStream = encryptedFile.openFileInput()
            val kontaktList =
                Persister().read(KontakteList::class.java, fileInputStream).kontakteList
            if (kontaktList != null) {
                val kontakt = kontaktList.find { it.kKontaktNumber == kontaktNummer }
                if (kontakt != null) {
                    onFinishedListener.onfinished(kontakt, FinishCode.finishedOnFile)
                } else {
                    loadDataFromWebservice(onFinishedListener, kontaktNummer)
                }
            } else {
                onFinishedListener.onFailure(FailureCode.ERROR_LOADING_FILE)
            }
        } catch (e: Exception) {
            removeFile(context, KONTAKTE_LIST_FILE_NAME)
            onFinishedListener.onFailure(FailureCode.ERROR_LOADING_FILE)
        } finally {
            fileInputStream.close()
        }
    }
}