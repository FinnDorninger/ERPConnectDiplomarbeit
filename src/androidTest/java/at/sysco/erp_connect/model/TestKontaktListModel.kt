package at.sysco.erp_connect.model

import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.platform.app.InstrumentationRegistry
import at.sysco.erp_connect.constants.FailureCode
import at.sysco.erp_connect.constants.FinishCode
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*
import android.content.Context.MODE_PRIVATE
import java.io.OutputStreamWriter
import androidx.preference.PreferenceManager
import at.sysco.erp_connect.settings.SharedPref
import at.sysco.erp_connect.kontakte_list.KontakteListContract
import at.sysco.erp_connect.pojo.Kontakt
import java.util.concurrent.CountDownLatch


/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
//to: emulate fullstorage.

@RunWith(AndroidJUnit4ClassRunner::class)
class TestKontaktListModel {
    var context = InstrumentationRegistry.getInstrumentation().targetContext
    var model = KontakteListModel(context)
    var kontakt = Kontakt("400")
    var listKontakt = listOf(kontakt)
    var pref = PreferenceManager.getDefaultSharedPreferences(context)
    var correctURL = "https://83.164.140.68:13443/"
    var correctUser = "meso"
    var correctPW = "meso"
    var fileName = "KontakteFile.xml"
    var isConnected = ModelUtitlity.checkInternetConnection(context)

    @Test
    fun offlineLoadingNoFileExisting() {
        val latch = CountDownLatch(1)
        context.deleteFile(fileName)
        if (!isConnected) {
            model.getKontakteList(object : KontakteListContract.Model.OnFinishedListener {
                override fun onfinished(kontaktArrayList: List<Kontakt>, finishCode: String) {
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
        model.saveKontakte(listKontakt)
        val latch = CountDownLatch(1)

        if (!isConnected) {
            model.getKontakteList(object : KontakteListContract.Model.OnFinishedListener {
                override fun onfinished(kontaktArrayList: List<Kontakt>, finishCode: String) {
                    assertEquals(FinishCode.finishedOnFile, finishCode)
                    assertEquals(kontaktArrayList, listKontakt)
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
        val outputStreamWriter = OutputStreamWriter(context.openFileOutput(fileName, MODE_PRIVATE))
        outputStreamWriter.write("</>jkaslfd<>asjfdh")
        outputStreamWriter.close()
        val latch = CountDownLatch(1)

        if (!isConnected) {
            model.getKontakteList(object : KontakteListContract.Model.OnFinishedListener {
                override fun onfinished(kontaktArrayList: List<Kontakt>, finishCode: String) {
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
        assertEquals(model.saveKontakte(listKontakt), FinishCode.finishedSavingKontakte)
        val latch = CountDownLatch(1)
        if (!isConnected) {
            model.getKontakteList(object : KontakteListContract.Model.OnFinishedListener {
                override fun onfinished(kontaktArrayList: List<Kontakt>, finishCode: String) {
                    assertEquals(listKontakt, kontaktArrayList)
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
        ModelUtitlity.removeFile(context, fileName)

        pref.edit().putString("base_url", correctURL).apply()
        pref.edit().putString("user_name", correctUser).apply()
        SharedPref.storePw(correctPW, context)
        val latch = CountDownLatch(1)

        if (isConnected) {
            model.getKontakteList(object : KontakteListContract.Model.OnFinishedListener {
                override fun onfinished(kontaktArrayList: List<Kontakt>, finishCode: String) {
                    assertNotNull(kontaktArrayList)
                    assertEquals(FinishCode.finishedOnWeb, finishCode)
                    latch.countDown()
                }

                override fun onFailure(failureCode: String) {
                    assertNull(failureCode)
                    latch.countDown()
                }
            })
            latch.await()
        } else {
            fail("not connected")
        }
    }

    @Test
    fun loadWebserviceAndSave() {
        ModelUtitlity.removeFile(context, fileName)
        pref.edit().putString("base_url", correctURL).apply()
        pref.edit().putString("user_name", correctUser).apply()
        SharedPref.storePw(correctPW, context)
        val latch = CountDownLatch(1)

        if (isConnected) {
            model.getKontakteList(object : KontakteListContract.Model.OnFinishedListener {
                override fun onfinished(kontaktArrayList: List<Kontakt>, finishCode: String) {
                    assertNotNull(kontaktArrayList)
                    assertEquals(model.saveKontakte(listKontakt), FinishCode.finishedSavingKontakte)
                    latch.countDown()
                }

                override fun onFailure(failureCode: String) {
                    assertNull(failureCode)
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
        ModelUtitlity.removeFile(context, fileName)
        pref.edit().putString("base_url", "https://83.164.140.68:13443/").apply()
        pref.edit().putString("user_name", "mes").apply()
        SharedPref.storePw("mes", context)
        val latch = CountDownLatch(1)

        if (isConnected) {
            model.getKontakteList(object : KontakteListContract.Model.OnFinishedListener {
                override fun onfinished(kontaktArrayList: List<Kontakt>, finishCode: String) {
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
    fun loadWebserviceUnsafeURL() {
        ModelUtitlity.removeFile(context, fileName)
        pref.edit().putString("base_url", "http://83.164.140.68:13443/").apply()
        pref.edit().putString("user_name", "meso").apply()
        SharedPref.storePw("meso", context)
        val latch = CountDownLatch(1)

        if (isConnected) {
            model.getKontakteList(object : KontakteListContract.Model.OnFinishedListener {
                override fun onfinished(kontaktArrayList: List<Kontakt>, finishCode: String) {
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
        ModelUtitlity.removeFile(context, fileName)
        pref.edit().putString("base_url", "http://83.164.140.68:13443").apply()
        pref.edit().putString("user_name", "meso").apply()
        SharedPref.storePw("meso", context)
        val latch = CountDownLatch(1)

        if (isConnected) {
            model.getKontakteList(object : KontakteListContract.Model.OnFinishedListener {
                override fun onfinished(kontaktArrayList: List<Kontakt>, finishCode: String) {
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
