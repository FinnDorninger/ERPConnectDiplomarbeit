package at.sysco.erp_connect.model

import android.content.Context
import android.util.Log
import at.sysco.erp_connect.constants.FailureCode
import at.sysco.erp_connect.konto_detail.KontoDetailContract
import at.sysco.erp_connect.pojo.KontoList
import org.simpleframework.xml.core.PersistenceException
import org.simpleframework.xml.core.Persister
import java.io.File
import java.io.FileInputStream
import java.io.IOException
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
        val path = context.filesDir.toString() + "/" + KONTO_DETAIL_FILE_NAME
        var fileInputStream: FileInputStream? = null

        try {
            fileInputStream = File(path).inputStream()
            val kontoList = Persister().read(KontoList::class.java, fileInputStream).kontenList
            if (kontoList != null) {
                val konto = kontoList.find { it.kNumber == kontoNummer }
                onFinishedListener.onfinished(konto!!)
            } else {
                context.deleteFile(KONTO_DETAIL_FILE_NAME)
                onFinishedListener.onFailureFileLoad(FailureCode.DAMAGED_FILE)
            }
        } catch (e: IOException) {
            //Exception when File could not be loaded
            onFinishedListener.onFailureFileLoad(FailureCode.NO_FILE)
        } catch (e: PersistenceException) {
            context.deleteFile(KONTO_DETAIL_FILE_NAME)
            onFinishedListener.onFailureFileLoad(FailureCode.DAMAGED_FILE)
            Log.w("Finn", "Lol")
        } finally {
            fileInputStream?.close()
        }
    }
}