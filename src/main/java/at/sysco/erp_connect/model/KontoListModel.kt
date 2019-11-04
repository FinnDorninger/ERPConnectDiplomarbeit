package at.sysco.erp_connect.model

import android.content.Context
import android.util.Xml
import at.sysco.erp_connect.constants.FailureCode
import at.sysco.erp_connect.konto_list.KontoListContract
import at.sysco.erp_connect.network.KontoApi
import at.sysco.erp_connect.pojo.Konto
import at.sysco.erp_connect.pojo.KontoList
import org.simpleframework.xml.core.PersistenceException
import org.simpleframework.xml.core.Persister
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import java.io.*
import android.net.ConnectivityManager
import android.util.Log
import android.util.Patterns
import androidx.preference.PreferenceManager
import at.sysco.erp_connect.constants.FinishCode
import java.lang.IllegalArgumentException


const val KONTO_LIST_FILE_NAME = "KontoFile.xml"

class KontoListModel(val context: Context) : KontoListContract.Model {
    override fun getKontoList(onFinishedListener: KontoListContract.Model.OnFinishedListener) {
        when {
            checkInternetConnection(context) -> loadDataFromWebservice(onFinishedListener)
            KONTO_LIST_FILE_NAME.doesFileExist() -> loadKontoListFromFile(onFinishedListener)
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

    private fun loadKontoListFromFile(onFinishedListener: KontoListContract.Model.OnFinishedListener) {
        val path = context.filesDir.toString() + "/" + KONTO_LIST_FILE_NAME
        var inputStream: FileInputStream? = null
        try {
            inputStream = File(path).inputStream()
            val kontoList = Persister().read(KontoList::class.java, inputStream).kontenList
            if (kontoList != null) {
                onFinishedListener.onfinished(kontoList, FinishCode.finishedOnFile)
            } else {
                onFinishedListener.onFailure(FailureCode.ERROR_LOADING_FILE)
            }
        } catch (e: IOException) {
            //Exception: File does not exist or is corrupt
            if (KONTO_LIST_FILE_NAME.doesFileExist()) {
                removeFile()
                onFinishedListener.onFailure(FailureCode.ERROR_LOADING_FILE)
            } else {
                onFinishedListener.onFailure(FailureCode.NO_DATA)
            }
        } catch (e: PersistenceException) {
            //Exception when Persister can not serialize object from file.
            removeFile()
            onFinishedListener.onFailure(FailureCode.ERROR_LOADING_FILE)
        } finally {
            inputStream?.close()
        }
    }

    //TO-DO: onResponse -> Error!
    private fun loadDataFromWebservice(onFinishedListener: KontoListContract.Model.OnFinishedListener) {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
        val userName = sharedPref.getString("user_name", "")
        val userPW = sharedPref.getString("user_password", "")
        var baseURL = sharedPref.getString("base_url", "")
        baseURL = modifyURL(baseURL)

        if (checkURL(baseURL) && !userName.isNullOrEmpty() && !userPW.isNullOrEmpty()) {
            val retrofit = Retrofit.Builder()

            val call = KontoApi.Factory.create(baseURL!!).getKontoList(userName, userPW)
            call.enqueue(object : Callback<KontoList> {
                override fun onResponse(call: Call<KontoList>, response: Response<KontoList>) {
                    var responseKontoList = response.body()?.kontenList
                    responseKontoList = responseKontoList?.sortedWith(compareBy({ it.kName }))

                    if (responseKontoList != null) {
                        saveKonto(responseKontoList, onFinishedListener)
                        onFinishedListener.onfinished(responseKontoList, FinishCode.finishedOnWeb)
                    } else {
                        //When file exists load from file
                        if (KONTO_LIST_FILE_NAME.doesFileExist()) {
                            loadKontoListFromFile(onFinishedListener)
                        } else {
                            onFinishedListener.onFailure(FailureCode.NO_DATA)
                        }
                    }
                }

                override fun onFailure(call: Call<KontoList>, t: Throwable) {
                    //When file exists load from file
                    if (KONTO_LIST_FILE_NAME.doesFileExist()) {
                        loadKontoListFromFile(onFinishedListener)
                    } else {
                        if (checkInternetConnection(context)) {
                            onFinishedListener.onFailure(FailureCode.NO_DATA)
                        } else {
                            onFinishedListener.onFailure(FailureCode.NO_CONNECTION)
                        }
                    }
                }
            })
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

    private fun saveKonto(
        listToSave: List<Konto>,
        onFinishedListener: KontoListContract.Model.OnFinishedListener
    ) {
        var fileWriter: FileWriter? = null
        val writer = StringWriter()
        val serializer = Xml.newSerializer()
        serializer.setOutput(writer)

        try {
            val file = File(context.filesDir, KONTO_LIST_FILE_NAME)
            fileWriter = FileWriter(file, false)

            serializer.startTag("", "MESOWebService")
            for (konto in listToSave) {
                serializer.startTag("", "KontenWebservice")
                if (konto.kNumber != null) {
                    serializer.startTag("", "Kontonummer")
                    serializer.text(konto.kNumber)
                    serializer.endTag("", "Kontonummer")
                }
                if (konto.kName != null) {
                    serializer.startTag("", "Kontoname")
                    serializer.text(konto.kName)
                    serializer.endTag("", "Kontoname")
                }
                if (konto.kCountry != null) {
                    serializer.startTag("", "Staat")
                    serializer.text(konto.kCountry)
                    serializer.endTag("", "Staat")
                }
                if (konto.kPlz != null) {
                    serializer.startTag("", "Postleitzahl")
                    serializer.text(konto.kPlz)
                    serializer.endTag("", "Postleitzahl")
                }
                if (konto.kCity != null) {
                    serializer.startTag("", "Ort")
                    serializer.text(konto.kCity)
                    serializer.endTag("", "Ort")
                }
                if (konto.kStreet != null) {
                    serializer.startTag("", "Strasse")
                    serializer.text(konto.kStreet)
                    serializer.endTag("", "Strasse")
                }
                if (konto.kTelCountry != null) {
                    serializer.startTag("", "Landesvorwahl")
                    serializer.text(konto.kTelCountry)
                    serializer.endTag("", "Landesvorwahl")
                }
                if (konto.kTelCity != null) {
                    serializer.startTag("", "Ortsvorwahl")
                    serializer.text(konto.kTelCity)
                    serializer.endTag("", "Ortsvorwahl")
                }
                if (konto.kTelMain != null) {
                    serializer.startTag("", "Telefon")
                    serializer.text(konto.kTelMain)
                    serializer.endTag("", "Telefon")
                }
                if (konto.kMobilCountry != null) {
                    serializer.startTag("", "LandesvorwahlMobiltelefonnummer")
                    serializer.text(konto.kMobilCountry)
                    serializer.endTag("", "LandesvorwahlMobiltelefonnummer")
                }
                if (konto.kMobilOperatorTel != null) {
                    serializer.startTag("", "BetreibervorwahlMobiltelefonnummer")
                    serializer.text(konto.kMobilOperatorTel)
                    serializer.endTag("", "BetreibervorwahlMobiltelefonnummer")
                }
                if (konto.kMobilOperatorTel != null) {
                    serializer.startTag("", "Mobiltelefonnummer")
                    serializer.text(konto.kMobilTel)
                    serializer.endTag("", "Mobiltelefonnummer")
                }
                if (konto.kMail != null) {
                    serializer.startTag("", "E-Mail-Adresse")
                    serializer.text(konto.kMail)
                    serializer.endTag("", "E-Mail-Adresse")
                }
                if (konto.kUrl != null) {
                    serializer.startTag("", "WWW-Adresse")
                    serializer.text(konto.kUrl)
                    serializer.endTag("", "WWW-Adresse")
                }
                if (konto.kNote != null) {
                    serializer.startTag("", "Notiz")
                    serializer.text(konto.kNote)
                    serializer.endTag("", "Notiz")
                }
                serializer.endTag("", "KontenWebservice")
            }
            serializer.endTag("", "MESOWebService")
            serializer.endDocument()

            val bytesOfFile = writer.toString().toByteArray(charset = Charsets.UTF_8).size
            if (context.filesDir.freeSpace > bytesOfFile) {
                fileWriter.write(writer.toString())
            } else {
                onFinishedListener.onFailure(FailureCode.NOT_ENOUGH_SPACE)
            }
        } catch (e: IOException) {
            removeFile()
            onFinishedListener.onFailure(FailureCode.ERROR_SAVING_FILE)
        } catch (e: IllegalArgumentException) {
            removeFile()
            onFinishedListener.onFailure(FailureCode.ERROR_SAVING_FILE)
        } catch (e: IllegalStateException) {
            removeFile()
            onFinishedListener.onFailure(FailureCode.ERROR_SAVING_FILE)
        } finally {
            fileWriter?.close()
        }
    }

    private fun removeFile() {
        when {
            KONTO_LIST_FILE_NAME.doesFileExist() -> context.deleteFile(KONTO_LIST_FILE_NAME)
        }
    }
}