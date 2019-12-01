package at.sysco.erp_connect.model

import android.content.Context
import android.content.SharedPreferences
import at.sysco.erp_connect.constants.FailureCode
import at.sysco.erp_connect.network.KontoApi
import org.simpleframework.xml.core.PersistenceException
import org.simpleframework.xml.core.Persister
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import java.io.*
import android.net.ConnectivityManager
import android.os.Handler
import android.util.Log
import android.util.Patterns
import android.util.Xml
import androidx.preference.PreferenceManager
import at.sysco.erp_connect.constants.FinishCode
import at.sysco.erp_connect.kontakte_list.KontakteListContract
import at.sysco.erp_connect.pojo.Kontakt
import at.sysco.erp_connect.pojo.KontakteList

const val KONTAKTE_LIST_FILE_NAME = "KontakteFile.xml"

class KontakteListModel(val context: Context) : KontakteListContract.Model {
    val sharedPref: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    var autoSync = sharedPref.getBoolean("auto_sync", true)

    override fun getKontakteList(onFinishedListener: KontakteListContract.Model.OnFinishedListener) {
        when {
            checkInternetConnection(context) -> loadDataFromWebservice(onFinishedListener)
            KONTAKTE_LIST_FILE_NAME.doesFileExist() -> loadKontakteListFromFile(onFinishedListener)
            else -> onFinishedListener.onFailure(FailureCode.NO_DATA)
        }
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

    private fun loadDataFromWebservice(onFinishedListener: KontakteListContract.Model.OnFinishedListener) {
        val userName = sharedPref.getString("user_name", "")
        val userPW = sharedPref.getString("user_password", "")
        val baseURL = sharedPref.getString("base_url", "")

        if (!baseURL.isNullOrEmpty() && !userName.isNullOrEmpty() && !userPW.isNullOrEmpty()) {
            val retrofit = Retrofit.Builder()
            val call = KontoApi.Factory.create(baseURL).getKontakteList()

            call.enqueue(object : Callback<KontakteList> {
                override fun onResponse(
                    call: Call<KontakteList>,
                    response: Response<KontakteList>
                ) {
                    Log.w("Stop", "onResponse")
                    var responseKontakteList = response.body()?.kontakteList
                    responseKontakteList =
                        responseKontakteList?.sortedWith(compareBy { it.kFirstName })
                    if (responseKontakteList != null) {
                        onFinishedListener.onfinished(
                            responseKontakteList,
                            FinishCode.finishedOnWeb
                        )
                    } else {
                        tryLoadingFromFile(onFinishedListener)
                    }
                }

                override fun onFailure(call: Call<KontakteList>, t: Throwable) {
                    Log.w("Stop", "onFailure")
                    tryLoadingFromFile(onFinishedListener)
                }
            })
        } else {
            tryLoadingFromFile(onFinishedListener)
        }
    }

    private fun loadKontakteListFromFile(onFinishedListener: KontakteListContract.Model.OnFinishedListener) {
        val path = context.filesDir.toString() + "/" + KONTAKTE_LIST_FILE_NAME
        var inputStream: FileInputStream? = null
        try {
            inputStream = File(path).inputStream()
            val kontakteList = Persister().read(KontakteList::class.java, inputStream).kontakteList
            if (kontakteList != null) {
                onFinishedListener.onfinished(kontakteList, FinishCode.finishedOnFile)
            } else {
                onFinishedListener.onFailure(FailureCode.ERROR_LOADING_FILE)
            }
        } catch (e: IOException) {
            //Exception: File does not exist or is corrupt
            if (KONTAKTE_LIST_FILE_NAME.doesFileExist()) {
                removeFile()
                onFinishedListener.onFailure(FailureCode.ERROR_LOADING_FILE)
            } else {
                onFinishedListener.onFailure(FailureCode.NO_DATA)
            }
        } catch (e: PersistenceException) {
            //Exception when Persister can not serialize object from file.
            Log.w("Test", "Persistence")
            removeFile()
            onFinishedListener.onFailure(FailureCode.ERROR_LOADING_FILE)
        } finally {
            inputStream?.close()
        }
    }

    private fun tryLoadingFromFile(onFinishedListener: KontakteListContract.Model.OnFinishedListener) {
        if (KONTAKTE_LIST_FILE_NAME.doesFileExist()) {
            loadKontakteListFromFile(onFinishedListener)
        } else {
            if (checkInternetConnection(context)) {
                onFinishedListener.onFailure(FailureCode.NO_DATA)
            } else {
                onFinishedListener.onFailure(FailureCode.NO_CONNECTION)
            }
        }
    }

    fun saveKontakte(listToSave: List<Kontakt>): String {
        val writer = StringWriter()
        lateinit var fileWriter: FileWriter
        val serializer = Xml.newSerializer()
        serializer.setOutput(writer)
        try {
            val file = File(context.filesDir, KONTAKTE_LIST_FILE_NAME)
            fileWriter = FileWriter(file, false)
            serializer.startTag("", "MESOWebService")
            for (kontakt in listToSave) {
                serializer.startTag("", "KontakteWebservice")
                if (kontakt.kKontaktNumber != null) {
                    serializer.startTag("", "Kontaktnummer")
                    serializer.text(kontakt.kKontaktNumber)
                    serializer.endTag("", "Kontaktnummer")
                }
                if (kontakt.kLastName != null) {
                    serializer.startTag("", "Name")
                    serializer.text(kontakt.kLastName)
                    serializer.endTag("", "Name")
                }
                if (kontakt.kNumber != null) {
                    serializer.startTag("", "Kontonummer")
                    serializer.text(kontakt.kNumber)
                    serializer.endTag("", "Kontonummer")
                }
                if (kontakt.kFirstName != null) {
                    serializer.startTag("", "Vorname")
                    serializer.text(kontakt.kFirstName)
                    serializer.endTag("", "Vorname")
                }
                if (kontakt.kFunction != null) {
                    serializer.startTag("", "Funktion")
                    serializer.text(kontakt.kFunction)
                    serializer.endTag("", "Funktion")
                }
                if (kontakt.kSex != null) {
                    serializer.startTag("", "Geschlecht")
                    serializer.text(kontakt.kSex)
                    serializer.endTag("", "Geschlecht")
                }
                if (kontakt.kAbteilung != null) {
                    serializer.startTag("", "Abteilung")
                    serializer.text(kontakt.kAbteilung)
                    serializer.endTag("", "Abteilung")
                }
                if (kontakt.kTelCountry != null) {
                    serializer.startTag("", "Landesvorwahl")
                    serializer.text(kontakt.kTelCountry)
                    serializer.endTag("", "Landesvorwahl")
                }
                if (kontakt.kTelCity != null) {
                    serializer.startTag("", "Ortsvorwahl")
                    serializer.text(kontakt.kTelCity)
                    serializer.endTag("", "Ortsvorwahl")
                }
                if (kontakt.kTelCountry != null) {
                    serializer.startTag("", "Telefon1Land")
                    serializer.text(kontakt.kTelCountry)
                    serializer.endTag("", "Telefon1Land")
                }
                if (kontakt.kMobilCountry != null) {
                    serializer.startTag("", "LandesvorwahlMobiltelefonnummer")
                    serializer.text(kontakt.kMobilCountry)
                    serializer.endTag("", "LandesvorwahlMobiltelefonnummer")
                }
                if (kontakt.kTelCity != null) {
                    serializer.startTag("", "Telefon1Vorwahl")
                    serializer.text(kontakt.kTelCity)
                    serializer.endTag("", "Telefon1Vorwahl")
                }
                if (kontakt.kTelNumber != null) {
                    serializer.startTag("", "Telefon1Durchwahl")
                    serializer.text(kontakt.kTelNumber)
                    serializer.endTag("", "Telefon1Durchwahl")
                }
                if (kontakt.kMobilCountry != null) {
                    serializer.startTag("", "MobiltelefonLand")
                    serializer.text(kontakt.kMobilCountry)
                    serializer.endTag("", "MobiltelefonLand")
                }
                if (kontakt.kMobilOperator != null) {
                    serializer.startTag("", "MobiltelefonVorwahl")
                    serializer.text(kontakt.kMobilOperator)
                    serializer.endTag("", "MobiltelefonVorwahl")
                }
                if (kontakt.kMobilNumber != null) {
                    serializer.startTag("", "MobiltelefonNummer")
                    serializer.text(kontakt.kMobilNumber)
                    serializer.endTag("", "MobiltelefonNummer")
                }
                if (kontakt.kMail != null) {
                    serializer.startTag("", "eMailadresse")
                    serializer.text(kontakt.kMail)
                    serializer.endTag("", "eMailadresse")
                }
                if (kontakt.kURL != null) {
                    serializer.startTag("", "Homepage")
                    serializer.text(kontakt.kURL)
                    serializer.endTag("", "Homepage")
                }
                serializer.endTag("", "KontakteWebservice")
            }
            serializer.endTag("", "MESOWebService")
            serializer.endDocument()

            val bytesOfFile = writer.toString().toByteArray(charset = Charsets.UTF_8).size
            if (context.filesDir.freeSpace > bytesOfFile) {
                fileWriter.write(writer.toString())
                return FinishCode.finishedSavingKontakte
            } else {
                return FailureCode.NOT_ENOUGH_SPACE
            }
        } catch (e: IOException) {
            removeFile()
            return FailureCode.ERROR_SAVING_FILE
        } catch (e: IllegalArgumentException) {
            removeFile()
            return FailureCode.ERROR_SAVING_FILE
        } catch (e: IllegalStateException) {
            removeFile()
            return FailureCode.ERROR_SAVING_FILE
        } finally {
            fileWriter.close()
        }
    }

    private fun removeFile() {
        when {
            KONTAKTE_LIST_FILE_NAME.doesFileExist() -> context.deleteFile(KONTAKTE_LIST_FILE_NAME)
        }
    }

    private fun String.doesFileExist(): Boolean {
        if (context.fileList().contains(this)) {
            return true
        }
        return false
    }
}