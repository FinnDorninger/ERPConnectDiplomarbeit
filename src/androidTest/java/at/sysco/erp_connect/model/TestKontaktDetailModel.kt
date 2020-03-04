package at.sysco.erp_connect.model

import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.platform.app.InstrumentationRegistry
import at.sysco.erp_connect.constants.FailureCode
import at.sysco.erp_connect.constants.FinishCode
import at.sysco.erp_connect.pojo.Konto

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import android.content.Context.MODE_PRIVATE
import java.io.OutputStreamWriter
import androidx.preference.PreferenceManager
import at.sysco.erp_connect.settings.SharedPref
import at.sysco.erp_connect.kontakte_detail.KontakteDetailContract
import at.sysco.erp_connect.pojo.Kontakt
import java.util.concurrent.CountDownLatch


/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
//TO: emulate fullstorage.

@RunWith(AndroidJUnit4ClassRunner::class)
class TestKontaktDetailModel {
    var context = InstrumentationRegistry.getInstrumentation().targetContext
    var model = KontakteDetailModel(context)
    var kontaktNR = "230000"
    var savedKontakt = Kontakt(kontaktNR)
    var listKontakt = listOf(savedKontakt)
    var pref = PreferenceManager.getDefaultSharedPreferences(context)
    var correctURL = "https://83.164.140.68:13443/"
    var correctUser = "meso"
    var correctPW = "meso"
    var isConnected = ModelUtitlity.checkInternetConnection(context)
    var fileName = "KontakteFile.xml"

    @Test
    fun offlineLoadingNoFileExisting() {
        context.deleteFile(fileName)
        if (!isConnected) {
            val latch = CountDownLatch(1)
            model.getKontaktDetail(object : KontakteDetailContract.Model.OnFinishedListener {
                override fun onfinished(kontakt: Kontakt, finishCode: String) {
                    assertNull(finishCode)
                    latch.countDown()
                }

                override fun onFailure(failureCode: String) {
                    assertEquals(FailureCode.NO_CONNECTION, failureCode)
                    latch.countDown()
                }
            }, kontaktNR)
            latch.await()
        } else {
            fail("connected")
        }
    }

    @Test
    fun offlineLoadingNotExistingKonto() {
        context.deleteFile(fileName)
        KontakteListModel(context).saveKontakte(listKontakt)
        val latch = CountDownLatch(1)

        if (!isConnected) {
            model.getKontaktDetail(object : KontakteDetailContract.Model.OnFinishedListener {
                override fun onfinished(kontakt: Kontakt, finishCode: String) {
                    assertNull(finishCode)
                    latch.countDown()
                }

                override fun onFailure(failureCode: String) {
                    assertEquals(FailureCode.NOT_SAVED, failureCode)
                    latch.countDown()
                }

            }, "wrongNumber")
            latch.await()
        } else {
            fail("connected")
        }
    }

    @Test
    fun offlineLoadingFileExisting() {
        KontakteListModel(context).saveKontakte(listKontakt)
        val latch = CountDownLatch(1)

        if (!isConnected) {
            model.getKontaktDetail(object : KontakteDetailContract.Model.OnFinishedListener {
                override fun onfinished(kontakt: Kontakt, finishCode: String) {
                    assertEquals(savedKontakt, kontakt)
                    assertEquals(FinishCode.finishedOnFile, finishCode)
                    latch.countDown()
                }

                override fun onFailure(failureCode: String) {
                    assertNull(failureCode)
                    latch.countDown()
                }
            }, kontaktNR)
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
            model.getKontaktDetail(object : KontakteDetailContract.Model.OnFinishedListener {
                override fun onfinished(kontakt: Kontakt, finishCode: String) {
                    assertNull(finishCode)
                    latch.countDown()
                }

                override fun onFailure(failureCode: String) {
                    assertEquals(FailureCode.ERROR_LOADING_FILE, failureCode)
                    latch.countDown()
                }
            }, kontaktNR)
            latch.await()
        } else {
            fail("Connected")
        }
    }

    @Test
    fun loadFromWebservice() {
        context.deleteFile(fileName)
        val latch = CountDownLatch(1)
        pref.edit().putString("base_url", correctURL).apply()
        pref.edit().putString("user_name", correctUser).apply()
        SharedPref.storePw(correctPW, context)
        val originalKonto = Konto("230000", "Diverse Debitoren", "A")

        if (isConnected) {
            model.getKontaktDetail(object : KontakteDetailContract.Model.OnFinishedListener {
                override fun onfinished(kontakt: Kontakt, finishCode: String) {
                    assertEquals(originalKonto, kontakt)
                    assertEquals(FinishCode.finishedOnWeb, finishCode)
                    latch.countDown()
                }

                override fun onFailure(failureCode: String) {
                    assertNull(failureCode)
                    latch.countDown()
                }
            }, kontaktNR)
            latch.await()
        } else {
            fail("not connected")
        }
    }

    @Test
    fun loadWebserviceWrongData() {
        context.deleteFile(fileName)
        val latch = CountDownLatch(1)
        pref.edit().putString("base_url", "https://83.164.140.68:13443/").apply()
        pref.edit().putString("user_name", "mes").apply()
        SharedPref.storePw("mes", context)

        if (isConnected) {
            model.getKontaktDetail(object : KontakteDetailContract.Model.OnFinishedListener {
                override fun onfinished(kontakt: Kontakt, finishCode: String) {
                    assertNull(finishCode)
                    latch.countDown()
                }

                override fun onFailure(failureCode: String) {
                    assertEquals(FailureCode.NO_DATA, failureCode)
                    latch.countDown()
                }
            }, kontaktNR)
            latch.await()
        } else {
            fail("not connected")
        }
    }

    @Test
    fun loadWebserviceUnsafeURL() {
        context.deleteFile(fileName)
        val latch = CountDownLatch(1)
        pref.edit().putString("base_url", "http://83.164.140.68:13443/").apply()
        pref.edit().putString("user_name", "meso").apply()
        SharedPref.storePw("meso", context)

        if (isConnected) {
            model.getKontaktDetail(object : KontakteDetailContract.Model.OnFinishedListener {
                override fun onfinished(kontakt: Kontakt, finishCode: String) {
                    assertNull(finishCode)
                    latch.countDown()
                }

                override fun onFailure(failureCode: String) {
                    assertEquals(FailureCode.NO_DATA, failureCode)
                    latch.countDown()
                }
            }, kontaktNR)
            latch.await()
        } else {
            fail("not connected")
        }
    }

    @Test
    fun loadWebserviceWrongURL() {
        context.deleteFile(fileName)
        val latch = CountDownLatch(1)
        pref.edit().putString("base_url", "http://83.164.140.68:13443").apply()
        pref.edit().putString("user_name", "meso").apply()
        SharedPref.storePw("meso", context)

        if (isConnected) {
            model.getKontaktDetail(object : KontakteDetailContract.Model.OnFinishedListener {
                override fun onfinished(kontakt: Kontakt, finishCode: String) {
                    assertNull(finishCode)
                    latch.countDown()
                }

                override fun onFailure(failureCode: String) {
                    assertEquals(FailureCode.NO_DATA, failureCode)
                    latch.countDown()
                }
            }, kontaktNR)
            latch.await()
        } else {
            fail("not connected")
        }
    }

    @Test
    fun loadWebserviceWrongkontaktNR() {
        context.deleteFile(fileName)
        val latch = CountDownLatch(1)
        pref.edit().putString("base_url", "http://83.164.140.68:13443").apply()
        pref.edit().putString("user_name", "meso").apply()
        SharedPref.storePw("meso", context)

        if (isConnected) {
            model.getKontaktDetail(object : KontakteDetailContract.Model.OnFinishedListener {
                override fun onfinished(kontakt: Kontakt, finishCode: String) {
                    assertNull(finishCode)
                    latch.countDown()
                }

                override fun onFailure(failureCode: String) {
                    assertEquals(FailureCode.NO_DATA, failureCode)
                    latch.countDown()
                }
            }, "ABDJSFHN")
            latch.await()
        } else {
            fail("not connected")
        }
    }
}
