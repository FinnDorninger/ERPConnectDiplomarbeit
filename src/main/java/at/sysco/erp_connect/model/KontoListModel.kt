package at.sysco.erp_connect.model

import android.content.Context
import android.util.Log
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

const val KONTO_LIST_FILE_NAME = "KontoFile.xml"

class KontoListModel(val context: Context) : KontoListContract.Model {
    val retrofit = Retrofit.Builder()

    override fun getKontoList(onFinishedListener: KontoListContract.Model.OnFinishedListener) {
        context.deleteFile(KONTO_LIST_FILE_NAME)
        if (KONTO_LIST_FILE_NAME.doesFileExist()) {
            loadKontoListFromFile(onFinishedListener)
        } else {
            loadDataFromWebservice(onFinishedListener)
        }
    }

    private fun String.doesFileExist(): Boolean {
        if (context.fileList().contains(this)) {
            return true
        }
        return false
    }

    private fun loadKontoListFromFile(onFinishedListener: KontoListContract.Model.OnFinishedListener) {
        val path = context.filesDir.toString() + "/" + KONTO_LIST_FILE_NAME
        var inputStream: InputStream? = null

        try {
            inputStream = File(path).inputStream()
            val kontoList = Persister().read(KontoList::class.java, inputStream).kontenList
            if (kontoList != null) {
                onFinishedListener.onfinished(kontoList)
            }
        } catch (e: IOException) {
            //Exception when File could not be loaded
            onFinishedListener.onFailure(FailureCode.NO_FILE)
        } catch (e: PersistenceException) {
            //Exception when Persister can not serialize object from file.
            onFinishedListener.onFailure(FailureCode.DAMAGED_FILE)
        } finally {
            inputStream?.close()
        }
    }

    //TO-DO: onResponse -> Error!
    private fun loadDataFromWebservice(onFinishedListener: KontoListContract.Model.OnFinishedListener) {
        val call = KontoApi.Factory.create().getKontoList()
        call.enqueue(object : Callback<KontoList> {
            override fun onResponse(call: Call<KontoList>, response: Response<KontoList>) {
                var responseKontoList = response.body()?.kontenList
                responseKontoList = responseKontoList?.sortedWith(compareBy({ it.kName }))

                if (responseKontoList != null) {
                    save(responseKontoList)
                    onFinishedListener.onfinished(responseKontoList)
                } else {
                    onFinishedListener.onFailure(FailureCode.FAILED_CONNECTION)
                }
            }

            override fun onFailure(call: Call<KontoList>, t: Throwable) {
                onFinishedListener.onFailure(FailureCode.FAILED_CONNECTION)
            }
        })
    }

    private fun save(listToSave: List<Konto>) {
        val serializer = Xml.newSerializer()
        val writer = StringWriter()
        val file = File(context.filesDir, KONTO_LIST_FILE_NAME)
        val fileWriter = FileWriter(file, false) //Append -> Overwriting file

        val tagList = arrayOf(
            "KontenWebservice"
            , "Kontonummer"
            , "Kontoname"
            , "Staat"
            , "Postleitzahl"
            , "Ort"
            , "Strasse"
            , "Landesvorwahl"
            , "Ortsvorwahl"
            , "Telefon"
            , "LandesvorwahlMobiltelefonnummer"
            , "BetreibervorwahlMobiltelefonnummer"
            , "Mobiltelefonnummer"
            , "E-Mail-Adresse"
            , "WWW-Adresse"
            , "Notiz"
        )
        serializer.setOutput(writer)
        serializer.startTag("", "MESOWebService")
        for (konto in listToSave) {
            serializer.startTag("", "KontenWebservice")
            serializer.startTag("", "Kontonummer")
            serializer.text(konto.kNumber)
            serializer.endTag("", "Kontonummer")
            serializer.startTag("", "Kontoname")
            serializer.text(konto.kName)
            serializer.endTag("", "Kontoname")
            serializer.startTag("", "Staat")
            serializer.text(konto.kCountry.orEmpty())
            serializer.endTag("", "Staat")
            serializer.startTag("", "Postleitzahl")
            serializer.text(konto.kPlz.orEmpty())
            serializer.endTag("", "Postleitzahl")
            serializer.startTag("", "Ort")
            serializer.text(konto.kCity.orEmpty())
            serializer.endTag("", "Ort")
            serializer.startTag("", "Strasse")
            serializer.text(konto.kStreet.orEmpty())
            serializer.endTag("", "Strasse")
            serializer.startTag("", "Telefon")
            serializer.text(konto.kTelMain.orEmpty())
            serializer.endTag("", "Telefon")
            serializer.startTag("", "E-Mail-Adresse")
            serializer.text(konto.kMail.orEmpty())
            serializer.endTag("", "E-Mail-Adresse")
            serializer.startTag("", "WWW-Adresse")
            serializer.text(konto.kUrl.orEmpty())
            serializer.endTag("", "WWW-Adresse")
            serializer.startTag("", "Notiz")
            serializer.text(konto.kNote.orEmpty())
            serializer.endTag("", "Notiz")
            serializer.endTag("", "KontenWebservice")
        }
        serializer.endTag("", "MESOWebService")
        serializer.endDocument()
        try {
            fileWriter.write(writer.toString())
        } catch (e: IOException) {

        } finally {
            fileWriter.close()
        }

    }
}