package at.sysco.erp_connect.model

import android.content.Context
import at.sysco.erp_connect.constants.FailureCode
import at.sysco.erp_connect.konto_list.KontoListContract
import at.sysco.erp_connect.network.WebserviceApi
import at.sysco.erp_connect.pojo.Konto
import at.sysco.erp_connect.pojo.KontoList
import org.simpleframework.xml.core.Persister
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.*
import android.net.ConnectivityManager
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.MasterKeys
import at.sysco.erp_connect.SharedPref
import at.sysco.erp_connect.constants.FinishCode
import java.lang.Exception
import java.security.GeneralSecurityException
import javax.xml.stream.FactoryConfigurationError
import javax.xml.stream.XMLOutputFactory
import javax.xml.stream.XMLStreamException

const val KONTO_LIST_FILE_NAME = "KontoFile.xml"

//Geschäftslogik der Kontenlisten
class KontoListModel(val context: Context) : KontoListContract.Model {
    private val keyGenParameterSpec = MasterKeys.AES256_GCM_SPEC
    private val masterKeyAlias = MasterKeys.getOrCreate(keyGenParameterSpec)

    //Methode welche entscheidet welches Verfahren für die Beschaffung der Daten ausgeführt werden soll
    override fun getKontoList(onFinishedListener: KontoListContract.Model.OnFinishedListener) {
        when {
            checkInternetConnection(context) -> loadDataFromWebservice(onFinishedListener)
            KONTO_LIST_FILE_NAME.doesFileExist() -> loadKontoListFromFile(onFinishedListener)
            else -> onFinishedListener.onFailure(FailureCode.NO_CONNECTION)
        }
    }

    //Methode welche die Prüfung der Existenz einer Date erleichtert
    private fun String.doesFileExist(): Boolean {
        if (context.fileList().contains(this)) {
            return true
        }
        return false
    }

    //Prüft ob Internetverbindung besteht
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

    //Ladet Kontenliste aus dem Filesystem (XML-Datei)
    private fun loadKontoListFromFile(onFinishedListener: KontoListContract.Model.OnFinishedListener) {
        lateinit var fileInputStream: FileInputStream
        try {
            val encFile = File(context.filesDir, KONTO_LIST_FILE_NAME)
            val encryptedFile = EncryptedFile.Builder(
                encFile,
                context,
                masterKeyAlias,
                EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
            ).build()

            fileInputStream = encryptedFile.openFileInput()

            val kontoList = Persister().read(KontoList::class.java, fileInputStream).kontenList
            if (kontoList != null) {
                onFinishedListener.onfinished(kontoList, FinishCode.finishedOnFile)
            } else {
                onFinishedListener.onFailure(FailureCode.ERROR_LOADING_FILE)
            }
        } catch (e: IOException) {
            if (KONTAKTE_LIST_FILE_NAME.doesFileExist()) {
                KONTAKTE_LIST_FILE_NAME.removeFile()
                onFinishedListener.onFailure(FailureCode.ERROR_LOADING_FILE)
            } else {
                onFinishedListener.onFailure(FailureCode.NO_DATA)
            }
        } catch (e: Exception) {
            KONTAKTE_LIST_FILE_NAME.removeFile()
            onFinishedListener.onFailure(FailureCode.ERROR_LOADING_FILE)
        } catch (e: GeneralSecurityException) {
            KONTAKTE_LIST_FILE_NAME.removeFile()
            onFinishedListener.onFailure(FailureCode.ERROR_LOADING_FILE)
        } finally {
            try {
                fileInputStream.close()
            } catch (e: IOException) {
                onFinishedListener.onFailure(FailureCode.ERROR_LOADING_FILE)
            }
        }
    }

    //Ladet Daten aus dem Webservice
    private fun loadDataFromWebservice(onFinishedListener: KontoListContract.Model.OnFinishedListener) {
        val userPw = SharedPref.getUserPW(context)
        val userName = SharedPref.getUserName(context)
        val baseURL = SharedPref.getBaseURL(context)
        if (!baseURL.isNullOrBlank() && !userPw.isNullOrBlank() && !userName.isNullOrBlank()) {
            val kontoService = WebserviceApi.Factory.getApi(baseURL)
            val call = kontoService.getKontoList(userPw, userName)
            call.enqueue(object : Callback<KontoList> {
                override fun onResponse(call: Call<KontoList>, response: Response<KontoList>) {
                    var responseKontoList = response.body()?.kontenList
                    responseKontoList = responseKontoList?.sortedWith(compareBy({ it.kName }))
                    if (responseKontoList != null && response.isSuccessful) {
                        onFinishedListener.onfinished(responseKontoList, FinishCode.finishedOnWeb)
                    } else {
                        tryLoadingFromFile(onFinishedListener)
                    }
                }

                override fun onFailure(call: Call<KontoList>, t: Throwable) {
                    tryLoadingFromFile(onFinishedListener)
                }
            })
        } else {
            onFinishedListener.onFailure(FailureCode.NO_DATA)
        }
    }

    //Prüft ob Laden aus dem File möglich ist
    private fun tryLoadingFromFile(onFinishedListener: KontoListContract.Model.OnFinishedListener) {
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

    //Funktion welches die Kontenliste in ein XML-File speichert
    fun saveKonto(listToSave: List<Konto>): String {
        KONTO_LIST_FILE_NAME.removeFile()
        lateinit var writer: OutputStreamWriter
        try {
            val encryptedFile = EncryptedFile.Builder(
                File(context.filesDir, KONTO_LIST_FILE_NAME),
                context,
                masterKeyAlias,
                EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
            ).build()
            writer = encryptedFile.openFileOutput().writer(charset = Charsets.UTF_8)

            val xmlStreamWriter = XMLOutputFactory.newInstance().createXMLStreamWriter(writer)
            xmlStreamWriter.writeStartDocument()
            xmlStreamWriter.writeStartElement("MESOWebService")
            for (konto in listToSave) {
                xmlStreamWriter.writeStartElement("KontenWebservice")
                if (konto.kNumber != null) {
                    xmlStreamWriter.writeStartElement("Kontonummer")
                    xmlStreamWriter.writeCharacters(konto.kNumber)
                    xmlStreamWriter.writeEndElement()
                }
                if (konto.kName != null) {
                    xmlStreamWriter.writeStartElement("Kontoname")
                    xmlStreamWriter.writeCharacters(konto.kName)
                    xmlStreamWriter.writeEndElement()
                }
                if (konto.kCountry != null) {
                    xmlStreamWriter.writeStartElement("Staat")
                    xmlStreamWriter.writeCharacters(konto.kCountry)
                    xmlStreamWriter.writeEndElement()
                }
                if (konto.kPlz != null) {
                    xmlStreamWriter.writeStartElement("Postleitzahl")
                    xmlStreamWriter.writeCharacters(konto.kPlz)
                    xmlStreamWriter.writeEndElement()
                }
                if (konto.kCity != null) {
                    xmlStreamWriter.writeStartElement("Ort")
                    xmlStreamWriter.writeCharacters(konto.kCity)
                    xmlStreamWriter.writeEndElement()
                }
                if (konto.kStreet != null) {
                    xmlStreamWriter.writeStartElement("Strasse")
                    xmlStreamWriter.writeCharacters(konto.kStreet)
                    xmlStreamWriter.writeEndElement()
                }
                if (konto.kTelCountry != null) {
                    xmlStreamWriter.writeStartElement("Landesvorwahl")
                    xmlStreamWriter.writeCharacters(konto.kTelCountry)
                    xmlStreamWriter.writeEndElement()
                }
                if (konto.kTelCity != null) {
                    xmlStreamWriter.writeStartElement("Ortsvorwahl")
                    xmlStreamWriter.writeCharacters(konto.kTelCity)
                    xmlStreamWriter.writeEndElement()
                }
                if (konto.kTelMain != null) {
                    xmlStreamWriter.writeStartElement("Telefon")
                    xmlStreamWriter.writeCharacters(konto.kTelMain)
                    xmlStreamWriter.writeEndElement()
                }
                if (konto.kMobilCountry != null) {
                    xmlStreamWriter.writeStartElement("LandesvorwahlMobiltelefonnummer")
                    xmlStreamWriter.writeCharacters(konto.kMobilCountry)
                    xmlStreamWriter.writeEndElement()
                }
                if (konto.kMobilOperatorTel != null) {
                    xmlStreamWriter.writeStartElement("BetreibervorwahlMobiltelefonnummer")
                    xmlStreamWriter.writeCharacters(konto.kMobilOperatorTel)
                    xmlStreamWriter.writeEndElement()
                }
                if (konto.kMobilTel != null) {
                    xmlStreamWriter.writeStartElement("Mobiltelefonnummer")
                    xmlStreamWriter.writeCharacters(konto.kMobilTel)
                    xmlStreamWriter.writeEndElement()
                }
                if (konto.kMail != null) {
                    xmlStreamWriter.writeStartElement("E-Mail-Adresse")
                    xmlStreamWriter.writeCharacters(konto.kMail)
                    xmlStreamWriter.writeEndElement()
                }
                if (konto.kUrl != null) {
                    xmlStreamWriter.writeStartElement("WWW-Adresse")
                    xmlStreamWriter.writeCharacters(konto.kUrl)
                    xmlStreamWriter.writeEndElement()
                }
                if (konto.kNote != null) {
                    xmlStreamWriter.writeStartElement("Notiz")
                    xmlStreamWriter.writeCharacters(konto.kNote)
                    xmlStreamWriter.writeEndElement()
                }
                xmlStreamWriter.writeEndElement()
            }
            xmlStreamWriter.writeEndElement()
            xmlStreamWriter.writeEndDocument()
        } catch (e: IOException) {
            KONTO_LIST_FILE_NAME.removeFile()
        } catch (e: XMLStreamException) {
            KONTO_LIST_FILE_NAME.removeFile()
        } catch (e: FactoryConfigurationError) {
            KONTO_LIST_FILE_NAME.removeFile()
        } catch (e: GeneralSecurityException) {
            KONTO_LIST_FILE_NAME.removeFile()
        } finally {
            try {
                writer.close()
            } catch (e: IOException) {
                return FailureCode.ERROR_SAVING_FILE
            }
            return FinishCode.finishedSavingKonto
        }
    }

    //Methode welches das Löschen einer Datei erleichtert
    private fun String.removeFile() {
        when {
            this.doesFileExist() -> context.deleteFile(this)
        }
    }
}