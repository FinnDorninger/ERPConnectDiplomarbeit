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
import android.net.ConnectivityManager
import androidx.preference.PreferenceManager
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.MasterKeys
import at.sysco.erp_connect.SharedPref
import at.sysco.erp_connect.constants.FinishCode
import at.sysco.erp_connect.kontakte_list.KontakteListContract
import at.sysco.erp_connect.pojo.Kontakt
import at.sysco.erp_connect.pojo.KontakteList
import java.lang.Exception
import java.security.GeneralSecurityException
import javax.xml.stream.FactoryConfigurationError
import javax.xml.stream.XMLOutputFactory
import javax.xml.stream.XMLStreamException

const val KONTAKTE_LIST_FILE_NAME = "KontakteFile.xml"

//Geschäftslogik der Ansprechpartnerlisten
class KontakteListModel(val context: Context) : KontakteListContract.Model {
    private val keyGenParameterSpec = MasterKeys.AES256_GCM_SPEC
    private val masterKeyAlias = MasterKeys.getOrCreate(keyGenParameterSpec)
    val sharedPref: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    var autoSync = sharedPref.getBoolean("auto_sync", true)

    //Methode welche entscheidet welches Verfahren für die Beschaffung der Daten ausgeführt werden soll
    override fun getKontakteList(onFinishedListener: KontakteListContract.Model.OnFinishedListener) {
        when {
            checkInternetConnection(context) -> loadDataFromWebservice(onFinishedListener)
            KONTAKTE_LIST_FILE_NAME.doesFileExist() -> loadKontakteListFromFile(onFinishedListener)
            else -> onFinishedListener.onFailure(FailureCode.NO_CONNECTION)
        }
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

    //Ladet Daten aus dem Webservice
    private fun loadDataFromWebservice(onFinishedListener: KontakteListContract.Model.OnFinishedListener) {
        val userPw = SharedPref.getUserPW(context)
        val userName = SharedPref.getUserName(context)
        val baseURL = SharedPref.getBaseURL(context)
        if (!baseURL.isNullOrBlank() && userName != null && userPw != null) {
            val call = WebserviceApi.Factory.getApi(baseURL).getKontakteList(userPw, userName)

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

    //Prüft ob Laden aus dem File möglich ist
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

    //Funktion welches die Daten in ein XML-File speichert
    fun saveKontakte(listToSave: List<Kontakt>): String {
        KONTAKTE_LIST_FILE_NAME.removeFile()
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
        } catch (e: IOException) {
            KONTAKTE_LIST_FILE_NAME.removeFile()
        } catch (e: XMLStreamException) {
            KONTAKTE_LIST_FILE_NAME.removeFile()
        } catch (e: FactoryConfigurationError) {
            KONTAKTE_LIST_FILE_NAME.removeFile()
        } catch (e: GeneralSecurityException) {
            KONTAKTE_LIST_FILE_NAME.removeFile()
        } finally {
            try {
                writer.close()
            } catch (e: IOException) {
                return FailureCode.ERROR_SAVING_FILE
            }
            return FinishCode.finishedSavingKontakte
        }
    }

    //Funktion welches das Löschen einer Datei erleichtert
    private fun String.removeFile() {
        when {
            this.doesFileExist() -> context.deleteFile(this)
        }
    }

    //Funktion welche das Prüfen der Existenz einer Datei erleichtert
    private fun String.doesFileExist(): Boolean {
        if (context.fileList().contains(this)) {
            return true
        }
        return false
    }
}