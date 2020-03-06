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
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.MasterKeys
import at.sysco.erp_connect.settings.SharedPref
import at.sysco.erp_connect.constants.FinishCode
import at.sysco.erp_connect.model.ModelUtitlity.checkInternetConnection
import at.sysco.erp_connect.model.ModelUtitlity.doesFileExist
import at.sysco.erp_connect.model.ModelUtitlity.removeFile
import java.lang.Exception
import javax.xml.stream.XMLOutputFactory

const val KONTO_LIST_FILE_NAME = "KontoFile.xml"

//Geschäftslogik der Kontenlisten
class KontoListModel(val context: Context) : KontoListContract.Model {
    //Methode welche entscheidet welches Verfahren für die Beschaffung der Daten ausgeführt werden soll
    override fun getKontoList(onFinishedListener: KontoListContract.Model.OnFinishedListener) {
        when {
            checkInternetConnection(context) -> loadDataFromWebservice(onFinishedListener)
            doesFileExist(
                context,
                KONTO_LIST_FILE_NAME
            ) -> loadKontoListFromFile(onFinishedListener)
            else -> onFinishedListener.onFailure(FailureCode.NO_CONNECTION)
        }
    }

    //Ladet Kontenliste aus dem Filesystem (XML-Datei)
    private fun loadKontoListFromFile(onFinishedListener: KontoListContract.Model.OnFinishedListener) {
        lateinit var fileInputStream: FileInputStream
        val keyGenParameterSpec = MasterKeys.AES256_GCM_SPEC
        val masterKeyAlias = MasterKeys.getOrCreate(keyGenParameterSpec)
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
        } catch (e: Exception) {
            removeFile(context, KONTO_LIST_FILE_NAME)
            onFinishedListener.onFailure(FailureCode.ERROR_LOADING_FILE)
        } finally {
            try {
                fileInputStream.close()
            } catch (e: Exception) {
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
            if (kontoService != null) {
                val call = kontoService.getKontoList(userPw, userName)
                call.enqueue(object : Callback<KontoList> {
                    override fun onResponse(call: Call<KontoList>, response: Response<KontoList>) {
                        var responseKontoList = response.body()?.kontenList
                        responseKontoList = responseKontoList?.sortedWith(compareBy({ it.kName }))
                        if (responseKontoList != null && response.isSuccessful) {
                            onFinishedListener.onfinished(
                                responseKontoList,
                                FinishCode.finishedOnWeb
                            )
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
        } else {
            onFinishedListener.onFailure(FailureCode.NO_DATA)
        }

    }

    //Prüft ob Laden aus dem File möglich ist
    private fun tryLoadingFromFile(onFinishedListener: KontoListContract.Model.OnFinishedListener) {
        if (doesFileExist(context, KONTO_LIST_FILE_NAME)) {
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
        removeFile(context, KONTO_LIST_FILE_NAME)
        var finishOrErrorCode: String = FinishCode.finishedSavingKonto
        lateinit var writer: OutputStreamWriter

        val keyGenParameterSpec = MasterKeys.AES256_GCM_SPEC
        val masterKeyAlias = MasterKeys.getOrCreate(keyGenParameterSpec)

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
                } else {
                    continue
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
        } catch (e: Exception) {
            finishOrErrorCode = FailureCode.ERROR_SAVING_FILE
            removeFile(context, KONTO_LIST_FILE_NAME)
        } finally {
            try {
                writer.close()
            } catch (e: Exception) {
                finishOrErrorCode = FailureCode.ERROR_SAVING_FILE
            }
        }
        return finishOrErrorCode
    }
}