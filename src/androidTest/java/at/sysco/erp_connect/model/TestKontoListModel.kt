package at.sysco.erp_connect.model

import android.content.Context.MODE_PRIVATE
import androidx.preference.PreferenceManager
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.platform.app.InstrumentationRegistry
import at.sysco.erp_connect.settings.SharedPref
import at.sysco.erp_connect.constants.FailureCode
import at.sysco.erp_connect.constants.FinishCode
import at.sysco.erp_connect.konto_list.KontoListContract
import at.sysco.erp_connect.pojo.Konto
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.io.OutputStreamWriter
import java.util.concurrent.CountDownLatch


/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */

@RunWith(AndroidJUnit4ClassRunner::class)
class TestKontoListModel {
    var context = InstrumentationRegistry.getInstrumentation().targetContext
    var model = KontoListModel(context)
    var konto = Konto("500")
    @Suppress("IncorrectScope")
    var listKonto = listOf(konto)
    var pref = PreferenceManager.getDefaultSharedPreferences(context)
    var correctURL = "https://83.164.140.68:13443/"
    var correctUser = "meso"
    var correctPW = "meso"
    var isConnected = ModelUtitlity.checkInternetConnection(context)
    var fileName = "KontoFile.xml"

    @Test
    fun offlineLoadingNoFileExisting() {
        context.deleteFile(fileName)
        val latch = CountDownLatch(1)

        if (!isConnected) {
            model.getKontoList(object : KontoListContract.Model.OnFinishedListener {
                override fun onfinished(kontoArrayList: List<Konto>, finishCode: String) {
                    assertNull(finishCode)
                    latch.countDown()
                }

                override fun onFailure(failureCode: String) {
                    assertEquals(FailureCode.NO_CONNECTION, failureCode)
                    latch.countDown()
                }
            })
            latch.await()
        } else {
            fail("connected")
        }
    }

    @Test
    fun offlineLoadingFileExisting() {
        model.saveKonto(listKonto)
        val latch = CountDownLatch(1)

        if (!isConnected) {
            model.getKontoList(object : KontoListContract.Model.OnFinishedListener {
                override fun onfinished(kontoArrayList: List<Konto>, finishCode: String) {
                    assertEquals(FinishCode.finishedOnFile, finishCode)
                    assertEquals(kontoArrayList, listKonto)
                    latch.countDown()
                }

                override fun onFailure(failureCode: String) {
                    assertNull(failureCode)
                    latch.countDown()
                }
            })
            latch.await()
        } else {
            fail("Connected")
        }
    }

    @Test
    fun offlineLoadingCorruptFile() {
        ModelUtitlity.removeFile(context, fileName)
        val latch = CountDownLatch(1)
        val outputStreamWriter = OutputStreamWriter(context.openFileOutput(fileName, MODE_PRIVATE))
        outputStreamWriter.write("</>jkaslfd<>asjfdh")
        outputStreamWriter.close()

        if (!isConnected) {
            model.getKontoList(object : KontoListContract.Model.OnFinishedListener {
                override fun onfinished(kontoArrayList: List<Konto>, finishCode: String) {
                    assertNull(finishCode)
                    latch.countDown()
                }

                override fun onFailure(failureCode: String) {
                    assertEquals(FailureCode.ERROR_LOADING_FILE, failureCode)
                    latch.countDown()
                }
            })
            latch.await()
        } else {
            fail("Connected")
        }
    }

    @Test
    fun saveAndLoadOfflineFromFileKonto() {
        val latch = CountDownLatch(1)
        assertEquals(model.saveKonto(listKonto), FinishCode.finishedSavingKonto)
        if (!isConnected) {
            model.getKontoList(object : KontoListContract.Model.OnFinishedListener {
                override fun onfinished(kontoArrayList: List<Konto>, finishCode: String) {
                    assertEquals(listKonto, kontoArrayList)
                    assertEquals(FinishCode.finishedOnFile, finishCode)
                    latch.countDown()
                }

                override fun onFailure(failureCode: String) {
                    assertNull(failureCode)
                    latch.countDown()
                }
            })
            latch.await()
        } else {
            fail("Connected")
        }

    }

    @Test
    fun loadFromWebservice() {
        ModelUtitlity.removeFile(context, "KontoFile.xml")

        pref.edit().putString("base_url", correctURL).apply()
        pref.edit().putString("user_name", correctUser).apply()
        SharedPref.storePw(correctPW, context)

        val latch = CountDownLatch(1)

        if (isConnected) {
            model.getKontoList(object : KontoListContract.Model.OnFinishedListener {
                override fun onfinished(kontoArrayList: List<Konto>, finishCode: String) {
                    assertEquals(FinishCode.finishedOnWeb, finishCode)
                    assertNotNull(kontoArrayList)
                    latch.countDown()
                }

                override fun onFailure(failureCode: String) {
                    assertNull(failureCode)
                    latch.countDown()
                }
            })
            latch.await()
        } else {
            fail("no connection")
        }
    }

    @Test
    fun loadWebserviceAndSave() {
        ModelUtitlity.removeFile(context, "KontoFile.xml")
        pref.edit().putString("base_url", "").apply()
        pref.edit().putString("user_name", "").apply()
        SharedPref.storePw("", context)
        val latch = CountDownLatch(1)

        if (isConnected) {
            model.getKontoList(object : KontoListContract.Model.OnFinishedListener {
                override fun onfinished(kontoArrayList: List<Konto>, finishCode: String) {
                    assertNull(finishCode)
                    assertEquals(model.saveKonto(kontoArrayList), FinishCode.finishedSavingKonto)
                    latch.countDown()
                }

                override fun onFailure(failureCode: String) {
                    assertEquals(FailureCode.NO_DATA, failureCode)
                    latch.countDown()
                }
            })
            latch.await()
        } else {
            fail("not connected")
        }
    }

    @Test
    fun loadWebserviceWrongData() {
        ModelUtitlity.removeFile(context, "KontoFile.xml")
        pref.edit().putString("base_url", "https://83.164.140.68:13443/").apply()
        pref.edit().putString("user_name", "mes").apply()
        SharedPref.storePw("mes", context)
        val latch = CountDownLatch(1)

        if (isConnected) {
            model.getKontoList(object : KontoListContract.Model.OnFinishedListener {
                override fun onfinished(kontoArrayList: List<Konto>, finishCode: String) {
                    println(kontoArrayList[0].kNumber + "Hallo")
                    assertNotNull(finishCode)
                    latch.countDown()
                }

                override fun onFailure(failureCode: String) {
                    assertEquals(FailureCode.NO_DATA, failureCode)
                    latch.countDown()
                }
            })
            latch.await()
        } else {
            fail("not connected")
        }
    }

    @Test
    fun loadWebserviceUnsafeURL() {
        ModelUtitlity.removeFile(context, "KontoFile.xml")
        pref.edit().putString("base_url", "http://83.164.140.68:13443/").apply()
        pref.edit().putString("user_name", "meso").apply()
        SharedPref.storePw("meso", context)
        val latch = CountDownLatch(1)

        if (isConnected) {
            model.getKontoList(object : KontoListContract.Model.OnFinishedListener {
                override fun onfinished(kontoArrayList: List<Konto>, finishCode: String) {
                    assertNull(finishCode)
                    latch.countDown()
                }

                override fun onFailure(failureCode: String) {
                    assertEquals(FailureCode.NO_DATA, failureCode)
                    latch.countDown()
                }
            })
            latch.await()
        } else {
            fail("not connected")
        }
    }

    @Test
    fun loadWebserviceWrongURL() {
        ModelUtitlity.removeFile(context, "KontoFile.xml")
        val latch = CountDownLatch(1)
        pref.edit().putString("base_url", "http://83.164.140.68:13443").apply()
        pref.edit().putString("user_name", "meso").apply()
        SharedPref.storePw("meso", context)

        if (isConnected) {
            model.getKontoList(object : KontoListContract.Model.OnFinishedListener {
                override fun onfinished(kontoArrayList: List<Konto>, finishCode: String) {
                    assertNull(finishCode)
                    latch.countDown()
                }

                override fun onFailure(failureCode: String) {
                    assertEquals(FailureCode.NO_DATA, failureCode)
                    latch.countDown()
                }
            })
            latch.await()
        } else {
            fail("not connected")
        }
    }
}
