package at.sysco.erp_connect

import androidx.preference.PreferenceManager
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.platform.app.InstrumentationRegistry
import at.sysco.erp_connect.settings.SharedPref

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4ClassRunner::class)
class TestSharedPref {
    val testURL = "www.test.at"
    val testPW = "weakpassword"
    val testUser = "user"
    var context = InstrumentationRegistry.getInstrumentation().targetContext
    var pref = PreferenceManager.getDefaultSharedPreferences(context)

    @Test
    fun setAndGetInhalt() {
        pref.edit().putString("base_url", testURL).apply()
        pref.edit().putString("user_name", testUser).apply()
        SharedPref.storePw(testPW, context)

        assertEquals(pref.getString("base_url", ""), testURL)
        assertEquals(pref.getString("user_name", ""), testUser)
        assertNotEquals(
            pref.getString("user_password", ""),
            testPW
        ) //Darf nicht gleich sein, wegen Verschlüsselung.

        assertEquals(SharedPref.getUserName(context), testUser)
        assertEquals(SharedPref.getUserPW(context), testPW)
        assertEquals(SharedPref.getBaseURL(context), testURL)
    }
}
