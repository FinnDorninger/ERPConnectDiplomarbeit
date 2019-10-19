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
import android.net.NetworkInfo
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import androidx.core.content.ContextCompat.getSystemService


const val KONTO_LIST_FILE_NAME = "KontoFile.xml"

class KontoListModel(val context: Context) : KontoListContract.Model {
    override fun getKontoList(onFinishedListener: KontoListContract.Model.OnFinishedListener) {
        if (checkInternetConnection(context)) {
            loadDataFromWebservice(onFinishedListener)
        } else if (KONTO_LIST_FILE_NAME.doesFileExist()) {
            loadKontoListFromFile(onFinishedListener)
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
                onFinishedListener.onfinished(kontoList)
            }
        } catch (e: IOException) {
            //Exception: File does not exist or is corrupt
            if (path.doesFileExist()) {
                context.deleteFile(KONTO_LIST_FILE_NAME)
                onFinishedListener.onFailure(FailureCode.DAMAGED_FILE)
            } else {
                onFinishedListener.onFailure(FailureCode.NO_FILE)
            }
        } catch (e: PersistenceException) {
            //Exception when Persister can not serialize object from file.
            context.deleteFile(KONTO_LIST_FILE_NAME)
            onFinishedListener.onFailure(FailureCode.DAMAGED_FILE)
        } finally {
            inputStream?.close()
        }
    }

    //TO-DO: onResponse -> Error!
    private fun loadDataFromWebservice(onFinishedListener: KontoListContract.Model.OnFinishedListener) {
        val retrofit = Retrofit.Builder()
        val call = KontoApi.Factory.create().getKontoList()

        call.enqueue(object : Callback<KontoList> {
            override fun onResponse(call: Call<KontoList>, response: Response<KontoList>) {
                var responseKontoList = response.body()?.kontenList
                responseKontoList = responseKontoList?.sortedWith(compareBy({ it.kName }))

                if (responseKontoList != null) {
                    save(responseKontoList, onFinishedListener)
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

    private fun save(
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

            fileWriter.write(writer.toString())
            fileWriter.close()
        } catch (e: IOException) {
            onFinishedListener.onFailure(FailureCode.NO_FILE)
        } catch (e: PersistenceException) {
            context.deleteFile(KONTO_LIST_FILE_NAME)
            onFinishedListener.onFailure(FailureCode.DAMAGED_FILE)
        } finally {
            fileWriter?.close()
        }
    }
}