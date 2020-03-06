package at.sysco.erp_connect.model

import android.content.Context
import android.content.SharedPreferences
import at.sysco.erp_connect.constants.FailureCode
import at.sysco.erp_connect.network.WebserviceApi
import org.simpleframework.xml.core.Persister
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.*
import androidx.preference.PreferenceManager
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.MasterKeys
import at.sysco.erp_connect.settings.SharedPref
import at.sysco.erp_connect.constants.FinishCode
import at.sysco.erp_connect.kontakte_list.KontakteListContract
import at.sysco.erp_connect.model.ModelUtitlity.checkInternetConnection
import at.sysco.erp_connect.model.ModelUtitlity.doesFileExist
import at.sysco.erp_connect.model.ModelUtitlity.removeFile
import at.sysco.erp_connect.pojo.Kontakt
import at.sysco.erp_connect.pojo.KontakteList
import java.lang.Exception
import javax.xml.stream.XMLOutputFactory

const val KONTAKTE_LIST_FILE_NAME = "KontakteFile.xml"

//Geschäftslogik der Ansprechpartnerlisten
class KontakteListModel(val context: Context) : KontakteListContract.Model {
    private val keyGenParameterSpec = MasterKeys.AES256_GCM_SPEC
    private val masterKeyAlias = MasterKeys.getOrCreate(keyGenParameterSpec)
    val sharedPref: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    //Methode welche entscheidet welches Verfahren für die Beschaffung der Daten ausgeführt werden soll
    override fun getKontakteList(onFinishedListener: KontakteListContract.Model.OnFinishedListener) {
        when {
            checkInternetConnection(context) -> loadDataFromWebservice(onFinishedListener)
            doesFileExist(context, KONTAKTE_LIST_FILE_NAME) -> loadKontakteListFromFile(
                onFinishedListener
            )
            else -> onFinishedListener.onFailure(FailureCode.NO_CONNECTION)
        }
    }

    override fun isAutoSyncActivated(): Boolean {
        return sharedPref.getBoolean("auto_sync", true)
    }

    //Ladet Daten aus dem Webservice
    private fun loadDataFromWebservice(onFinishedListener: KontakteListContract.Model.OnFinishedListener) {
        val userPw = SharedPref.getUserPW(context)
        val userName = SharedPref.getUserName(context)
        val baseURL = SharedPref.getBaseURL(context)
        if (!baseURL.isNullOrBlank() && userName != null && userPw != null) {
            val api = WebserviceApi.Factory.getApi(baseURL)
            if (api != null) {
                val call = api.getKontakteList(userPw, userName)
                call.enqueue(object : Callback<KontakteList> {
                    override fun onResponse(
                        call: Call<KontakteList>,
                        response: Response<KontakteList>
                    ) {
                        var responseKontakteList = response.body()?.kontakteList
                        responseKontakteList =
                            responseKontakteList?.sortedWith(compareBy { it.kLastName })
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

    //Ladet Ansrechpartnerliste aus dem Filesystem (XML-Datei)
    private fun loadKontakteListFromFile(onFinishedListener: KontakteListContract.Model.OnFinishedListener) {
        lateinit var fileInputStream: FileInputStream
        try {
            val encFile = File(context.filesDir, KONTAKTE_LIST_FILE_NAME)
            val encryptedFile = EncryptedFile.Builder(
                encFile,
                context,
                masterKeyAlias,
                EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
            ).build()
            fileInputStream = encryptedFile.openFileInput()

            val kontakteList =
                Persister().read(KontakteList::class.java, fileInputStream).kontakteList
            if (kontakteList != null) {
                onFinishedListener.onfinished(kontakteList, FinishCode.finishedOnFile)
            } else {
                onFinishedListener.onFailure(FailureCode.ERROR_LOADING_FILE)
            }
        } catch (e: Exception) {
            removeFile(context, KONTAKTE_LIST_FILE_NAME)
            onFinishedListener.onFailure(FailureCode.ERROR_LOADING_FILE)
        } finally {
            try {
                fileInputStream.close()
            } catch (e: IOException) {
                onFinishedListener.onFailure(FailureCode.ERROR_LOADING_FILE)
            }
        }
    }

    //Prüft ob Laden aus dem File möglich ist
    private fun tryLoadingFromFile(onFinishedListener: KontakteListContract.Model.OnFinishedListener) {
        if (doesFileExist(context, KONTAKTE_LIST_FILE_NAME)) {
            loadKontakteListFromFile(onFinishedListener)
        } else {
            if (checkInternetConnection(context)) {
                onFinishedListener.onFailure(FailureCode.NO_DATA)
            } else {
                onFinishedListener.onFailure(FailureCode.NO_CONNECTION)
            }
        }
    }

    //Funktion welches die Daten in ein XML-File speichert
    fun saveKontakte(listToSave: List<Kontakt>): String {
        removeFile(context, KONTAKTE_LIST_FILE_NAME)
        var finishOrErrorCode: String = FinishCode.finishedSavingKontakte
        lateinit var writer: OutputStreamWriter
        try {
            val encryptedFile = EncryptedFile.Builder(
                File(context.filesDir, KONTAKTE_LIST_FILE_NAME),
                context,
                masterKeyAlias,
                EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
            ).build()

            writer = encryptedFile.openFileOutput().writer(charset = Charsets.UTF_8)
            val outFactory: XMLOutputFactory = XMLOutputFactory.newInstance()
            val xmlStreamWriter = outFactory.createXMLStreamWriter(writer)

            xmlStreamWriter.writeStartDocument()
            xmlStreamWriter.writeStartElement("MESOWebService")
            for (kontakt in listToSave) {
                xmlStreamWriter.writeStartElement("KontakteWebservice")
                if (kontakt.kKontaktNumber != null) {
                    xmlStreamWriter.writeStartElement("Kontaktnummer")
                    xmlStreamWriter.writeCharacters(kontakt.kKontaktNumber)
                    xmlStreamWriter.writeEndElement()
                } else {
                    continue
                }
                if (kontakt.kLastName != null) {
                    xmlStreamWriter.writeStartElement("Name")
                    xmlStreamWriter.writeCharacters(kontakt.kLastName)
                    xmlStreamWriter.writeEndElement()

                }
                if (kontakt.kNumber != null) {
                    xmlStreamWriter.writeStartElement("Kontonummer")
                    xmlStreamWriter.writeCharacters(kontakt.kNumber)
                    xmlStreamWriter.writeEndElement()
                }
                if (kontakt.kFirstName != null) {
                    xmlStreamWriter.writeStartElement("Vorname")
                    xmlStreamWriter.writeCharacters(kontakt.kFirstName)
                    xmlStreamWriter.writeEndElement()
                }
                if (kontakt.kFunction != null) {
                    xmlStreamWriter.writeStartElement("Funktion")
                    xmlStreamWriter.writeCharacters(kontakt.kFunction)
                    xmlStreamWriter.writeEndElement()
                }
                if (kontakt.kSex != null) {
                    xmlStreamWriter.writeStartElement("Geschlecht")
                    xmlStreamWriter.writeCharacters(kontakt.kSex)
                    xmlStreamWriter.writeEndElement()
                }
                if (kontakt.kAbteilung != null) {
                    xmlStreamWriter.writeStartElement("Abteilung")
                    xmlStreamWriter.writeCharacters(kontakt.kAbteilung)
                    xmlStreamWriter.writeEndElement()
                }
                if (kontakt.kTelCountry != null) {
                    xmlStreamWriter.writeStartElement("Landesvorwahl")
                    xmlStreamWriter.writeCharacters(kontakt.kTelCountry)
                    xmlStreamWriter.writeEndElement()
                }
                if (kontakt.kTelCity != null) {
                    xmlStreamWriter.writeStartElement("Ortsvorwahl")
                    xmlStreamWriter.writeCharacters(kontakt.kTelCity)
                    xmlStreamWriter.writeEndElement()
                }
                if (kontakt.kTelCountry != null) {
                    xmlStreamWriter.writeStartElement("Telefon1Land")
                    xmlStreamWriter.writeCharacters(kontakt.kTelCountry)
                    xmlStreamWriter.writeEndElement()
                }
                if (kontakt.kMobilCountry != null) {
                    xmlStreamWriter.writeStartElement("LandesvorwahlMobiltelefonnummer")
                    xmlStreamWriter.writeCharacters(kontakt.kMobilCountry)
                    xmlStreamWriter.writeEndElement()
                }
                if (kontakt.kTelCity != null) {
                    xmlStreamWriter.writeStartElement("Telefon1Vorwahl")
                    xmlStreamWriter.writeCharacters(kontakt.kTelCity)
                    xmlStreamWriter.writeEndElement()
                }
                if (kontakt.kTelNumber != null) {
                    xmlStreamWriter.writeStartElement("Telefon1Durchwahl")
                    xmlStreamWriter.writeCharacters(kontakt.kTelNumber)
                    xmlStreamWriter.writeEndElement()
                }
                if (kontakt.kMobilCountry != null) {
                    xmlStreamWriter.writeStartElement("MobiltelefonLand")
                    xmlStreamWriter.writeCharacters(kontakt.kMobilCountry)
                    xmlStreamWriter.writeEndElement()
                }
                if (kontakt.kMobilOperator != null) {
                    xmlStreamWriter.writeStartElement("MobiltelefonVorwahl")
                    xmlStreamWriter.writeCharacters(kontakt.kMobilOperator)
                    xmlStreamWriter.writeEndElement()
                }
                if (kontakt.kMobilNumber != null) {
                    xmlStreamWriter.writeStartElement("MobiltelefonNummer")
                    xmlStreamWriter.writeCharacters(kontakt.kMobilNumber)
                    xmlStreamWriter.writeEndElement()
                }
                if (kontakt.kMail != null) {
                    xmlStreamWriter.writeStartElement("eMailadresse")
                    xmlStreamWriter.writeCharacters(kontakt.kMail)
                    xmlStreamWriter.writeEndElement()
                }
                if (kontakt.kURL != null) {
                    xmlStreamWriter.writeStartElement("Homepage")
                    xmlStreamWriter.writeCharacters(kontakt.kURL)
                    xmlStreamWriter.writeEndElement()
                }
                xmlStreamWriter.writeEndElement()
            }
            xmlStreamWriter.writeEndElement()
            xmlStreamWriter.writeEndDocument()
        } catch (e: Exception) {
            removeFile(context, KONTAKTE_LIST_FILE_NAME)
            finishOrErrorCode = FailureCode.ERROR_SAVING_FILE
        } finally {
            try {
                writer.close()
            } catch (e: Exception) {
                finishOrErrorCode = FailureCode.ERROR_SAVING_FILE
            }
            return finishOrErrorCode
        }
    }
}