package at.sysco.erp_connect.model

import android.content.Context
import android.util.Log
import android.util.Xml
import at.sysco.erp_connect.konto_detail.KontoDetailContract
import at.sysco.erp_connect.konto_list.KontoListContract
import at.sysco.erp_connect.network.KontoApi
import at.sysco.erp_connect.pojo.KontoList
import org.simpleframework.xml.core.Persister
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import java.io.File
import java.io.FileWriter
import java.io.StringWriter
import java.lang.Exception

const val KONTO_DETAIL_FILE_NAME = "KontoFile.xml"

class KontoDetailModel(val context: Context) : KontoDetailContract.Model {
    override fun getKontoDetail(
        onFinishedListener: KontoDetailContract.Model.OnFinishedListener,
        kontoNummer: String
    ) {
        if (KONTO_DETAIL_FILE_NAME.doesFileExist()) {
            loadKontoDetailFromFile(onFinishedListener, kontoNummer)
        }
    }

    private fun String.doesFileExist(): Boolean {
        if (context.fileList().contains(this)) {
            return true
        }
        return false
    }

    private fun loadKontoDetailFromFile(
        onFinishedListener: KontoDetailContract.Model.OnFinishedListener,
        kontoNummer: String
    ) {
        Log.w("Test", "Laden von Datei..")
        try {
            val path = context.filesDir.toString() + "/" + KONTO_DETAIL_FILE_NAME
            val file = File(path)
            val test = file.inputStream()
            val kontoList = Persister().read(KontoList::class.java, test).kontenList
            if (kontoList != null) {
                val konto = kontoList.find { it.kNumber == kontoNummer }
                onFinishedListener.onfinished(konto!!)
            } else {
                context.deleteFile(KONTO_DETAIL_FILE_NAME)
                onFinishedListener.onFailureFileLoad()
            }
        } catch (e: Exception) {
            onFinishedListener.onFailureFileLoad()
        }
    }
}