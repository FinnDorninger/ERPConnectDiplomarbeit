package at.sysco.erp_connect.model

import android.content.Context
import android.net.ConnectivityManager
import androidx.preference.PreferenceManager
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.MasterKeys
import at.sysco.erp_connect.constants.FailureCode
import at.sysco.erp_connect.constants.FinishCode
import at.sysco.erp_connect.kontakte_detail.KontakteDetailContract
import at.sysco.erp_connect.network.KontoApi
import at.sysco.erp_connect.pojo.KontakteList
import org.simpleframework.xml.core.PersistenceException
import org.simpleframework.xml.core.Persister
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import java.io.File
import java.io.FileInputStream
import java.io.IOException

const val KONTAKTE_FILE_NAME = "KontakteFile.xml"

class KontakteDetailModel(val context: Context) : KontakteDetailContract.Model {
    private val keyGenParameterSpec = MasterKeys.AES256_GCM_SPEC
    private val masterKeyAlias = MasterKeys.getOrCreate(keyGenParameterSpec)
    
    override fun getKontaktDetail(
        onFinishedListener: KontakteDetailContract.Model.OnFinishedListener,
        kontaktNummer: String
    ) {
        when {
            checkInternetConnection(context) -> loadDataFromWebservice(
                onFinishedListener,
                kontaktNummer
            )
            KONTAKTE_FILE_NAME.doesFileExist() -> loadKontaktDetailFromFile(
                onFinishedListener,
                kontaktNummer
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
        onFinishedListener: KontakteDetailContract.Model.OnFinishedListener,
        kontaktNummer: String
    ) {
        val retrofit = Retrofit.Builder()

        val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
        val userName = sharedPref.getString("user_name", "")
        val userPW = sharedPref.getString("user_password", "")
        val baseURL = sharedPref.getString("base_url", "")

        if (!baseURL.isNullOrEmpty() && !userName.isNullOrEmpty() && !userPW.isNullOrEmpty()) {

            val call = KontoApi.Factory.create(baseURL).getKontakt(userPW, userName, kontaktNummer)

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
        } else {
            tryLoadingFromFile(onFinishedListener, kontaktNummer)
        }
    }

    private fun tryLoadingFromFile(
        onFinishedListener: KontakteDetailContract.Model.OnFinishedListener,
        kontaktNummer: String
    ) {
        if (KONTAKTE_FILE_NAME.doesFileExist()) {
            loadKontaktDetailFromFile(onFinishedListener, kontaktNummer)
        } else {
            if (checkInternetConnection(context)) {
                onFinishedListener.onFailure(FailureCode.NO_DATA)
            } else {
                onFinishedListener.onFailure(FailureCode.NO_CONNECTION)
            }
        }
    }

    private fun loadKontaktDetailFromFile(
        onFinishedListener: KontakteDetailContract.Model.OnFinishedListener,
        kontaktNummer: String
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
        } catch (e: IOException) {
            //Exception: File does not exist or is corrupt
            if (KONTAKTE_FILE_NAME.doesFileExist()) {
                KONTAKTE_LIST_FILE_NAME.removeFile()
                onFinishedListener.onFailure(FailureCode.ERROR_LOADING_FILE)
            } else {
                onFinishedListener.onFailure(FailureCode.NO_DATA)
            }
        } catch (e: PersistenceException) {
            KONTAKTE_LIST_FILE_NAME.removeFile()
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