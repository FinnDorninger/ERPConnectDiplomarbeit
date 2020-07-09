package at.sysco.erp_connect

import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import at.sysco.erp_connect.network.WebserviceApi

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4ClassRunner::class)
class TestWebserviceApi {
    //Bei falschen Eingaben darf die Api nicht erstellt werden
    @Test
    fun getApiWithEmptyURL() {
        assertNull(WebserviceApi.Factory.getApi(""))
        assertNull(WebserviceApi.Factory.getApi("www.test.com"))
    }
}
