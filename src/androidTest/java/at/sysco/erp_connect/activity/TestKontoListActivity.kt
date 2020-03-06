package at.sysco.erp_connect.activity

import androidx.test.espresso.*
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.rule.ActivityTestRule
import at.sysco.erp_connect.R
import at.sysco.erp_connect.konto_list.KontoListActivity
import kotlinx.android.synthetic.main.activity_konto_list.view.*
import at.sysco.erp_connect.R.*
import androidx.test.espresso.matcher.ViewMatchers.*
import at.sysco.erp_connect.constants.FinishCode
import at.sysco.erp_connect.pojo.Konto
import com.google.android.material.bottomnavigation.BottomNavigationView
import org.hamcrest.CoreMatchers.allOf
import org.junit.Assert.*

import org.junit.Test
import org.junit.runner.RunWith


/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */

@RunWith(AndroidJUnit4ClassRunner::class)
class TestKontoListActivity {
    var rule: ActivityTestRule<KontoListActivity> = ActivityTestRule(KontoListActivity::class.java)
    lateinit var activity: KontoListActivity

    @Test
    fun showProgress() {
        val scenario = rule.launchActivity(null)
        scenario.showProgress()
        onView(withId(id.progressBar)).check(matches(isDisplayed()))
    }

    @Test
    fun showSnackbar() {
        val scenario = rule.launchActivity(null)
        scenario.runOnUiThread(object : Runnable {
            override fun run() {
                scenario.showSnackbar(FinishCode.finishedOnFile, false)
            }
        })
        onView(withText(FinishCode.finishedOnFile))
            .check(
                matches(
                    withEffectiveVisibility(
                        Visibility.VISIBLE
                    )
                )
            )
    }

    @Test
    fun navigationCorrect() {
        val scenario = rule.launchActivity(null)
        val view = scenario.findViewById<BottomNavigationView>(R.id.bottomNavigation)
        assertTrue(view.menu.findItem(id.action_Konten).isChecked)
    }

    @Test
    fun testDisplayRecyclerview() {
        val scenario = rule.launchActivity(null)
        val konto = Konto("100")
        val kontolist = listOf<Konto>(konto)
        scenario.runOnUiThread(object : Runnable {
            override fun run() {
                scenario.displayKontoListInRecyclerView(kontolist)
            }
        })

        onView(withId(id.rv_konto_list)).check(matches(isDisplayed()))
        onView(withId(id.search_konto)).check(matches(isDisplayed()))
    }
}
