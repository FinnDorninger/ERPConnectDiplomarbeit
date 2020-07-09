package at.sysco.erp_connect.activity

import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.intent.rule.IntentsTestRule
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.rule.ActivityTestRule
import at.sysco.erp_connect.R
import at.sysco.erp_connect.R.*
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.platform.app.InstrumentationRegistry
import at.sysco.erp_connect.constants.FailureCode
import at.sysco.erp_connect.constants.FinishCode
import at.sysco.erp_connect.kontakte_detail.KontakteDetailActivity
import at.sysco.erp_connect.kontakte_list.KontakteListActivity
import at.sysco.erp_connect.pojo.Kontakt
import at.sysco.erp_connect.settings.SettingsActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import org.junit.Assert.*

import org.junit.Test
import org.junit.runner.RunWith


/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */

//Klasse welche die KontakteListActivity testet
//Dafür wurde die Library Espresso verwendet

@RunWith(AndroidJUnit4ClassRunner::class)
class TestKontakteListActivity {
    //Für normale Activity-Tests.
    var rule: ActivityTestRule<KontakteListActivity> =
        ActivityTestRule(KontakteListActivity::class.java)
    //Benötigt wenn bei einer Activity auch Intents geprüft werden sollen.
    var testRuleWithIntents: IntentsTestRule<KontakteListActivity> =
        IntentsTestRule<KontakteListActivity>(KontakteListActivity::class.java)
    lateinit var activity: KontakteListActivity

    //Prüft ob Funktion welche das Ladessymbol anzeigt richtig funktioniert!
    @Test
    fun showAndHideProgress() {
        val scenario = rule.launchActivity(null)
        scenario.runOnUiThread(object : Runnable {
            override fun run() {
                scenario.showProgress()
            }
        })
        onView(withId(id.rv_kontakte_list)).check(
            matches(
                withEffectiveVisibility(
                    Visibility.GONE
                )
            )
        )
        onView(withId(id.progressBar)).check(matches(isDisplayed()))
        scenario.runOnUiThread(object : Runnable {
            override fun run() {
                scenario.hideProgress()
            }
        })
        onView(withId(id.progressBar)).check(
            matches(
                withEffectiveVisibility(
                    Visibility.GONE
                )
            )
        )
    }

    //Prüft ob die Navigation richtig eingestellt ist
    @Test
    fun navigationCorrect() {
        val scenario = rule.launchActivity(null)
        val view = scenario.findViewById<BottomNavigationView>(id.bottomNavigation)
        assertTrue(view.menu.findItem(id.action_Kontakte).isChecked)
    }

    //Prüft ob die Darstellung in dem Recyclerview funktioniert!
    //Und prüft ob bei Auswahl eines Eintrages auch ein Intent an Detail-Activity gesendet wird.
    @Test
    fun testDisplayRecyclerview() {
        val scenario = testRuleWithIntents.launchActivity(null)
        val kontakt = Kontakt("100", "Test")
        val kontolist = listOf(kontakt)
        scenario.runOnUiThread(object : Runnable {
            override fun run() {
                scenario.displayKontakteListInRecyclerView(kontolist)
            }
        })

        onView(withId(id.rv_kontakte_list)).check(matches(isDisplayed()))
        onView(withId(id.searchView)).check(matches(isDisplayed()))

        onView(withId(R.id.rv_kontakte_list)).perform(
            RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(0, click())
        )

        intended(hasComponent(KontakteDetailActivity::class.java.name))
        Intents.release()
    }

    //Stellt sicher, dass eine Snackbar mit der Success-Meldung dargestellt wird!
    @Test
    fun testOnSucess() {
        val scenario = rule.launchActivity(null)
        scenario.runOnUiThread(object : Runnable {
            override fun run() {
                scenario.onSucess(FinishCode.finishedOnFile)
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

    //Prüft ob Fehler in einer Snackbar dargestellt werden!
    @Test
    fun testOnFailure() {
        val scenario = rule.launchActivity(null)
        scenario.runOnUiThread(object : Runnable {
            override fun run() {
                scenario.onError(FailureCode.NO_CONNECTION)
            }
        })
        onView(withText(FailureCode.NO_CONNECTION))
            .check(
                matches(
                    withEffectiveVisibility(
                        Visibility.VISIBLE
                    )
                )
            )
    }

    //Prüft ob bei Ausführung des Retry-Buttons aus dem Menü auch wirklich ein erneutes Laden geladen wird!
    @Test
    fun retryButton() {
        val scenario = rule.launchActivity(null)
        openActionBarOverflowOrOptionsMenu(InstrumentationRegistry.getInstrumentation().context)
        onView(withText("Retry")).perform(click())
        onView(withId(id.progressBar)).check(matches(isDisplayed()))
    }

    //Prüft ob bei Auswahl des "Einstellungsmenü" - Eintrages auch die Einstellungen gestartet werden.
    @Test
    fun settingsButton() {
        val test = testRuleWithIntents.launchActivity(null)
        openActionBarOverflowOrOptionsMenu(InstrumentationRegistry.getInstrumentation().context)
        onView(withText("Verbindungs-Eigenschaften")).perform(click())
        intended(hasComponent(SettingsActivity::class.java.name))
        Intents.release()
    }

    //Prüft ob eine Snackbar dargestellt wird
    //Leider sind Snackbar-Tests fehleranfällig - funktionieren nur manchmal.
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

    //Prüft ob Suche mit Kontoname möglich
    @Test
    fun testSearch() {
        val scenario = rule.launchActivity(null)
        val kontakt = Kontakt("1", "Test", "Vorname")
        val kontakt2 = Kontakt("2", "Nachname2")
        val kontaktlist = listOf(kontakt, kontakt2)
        scenario.runOnUiThread(object : Runnable {
            override fun run() {
                scenario.displayKontakteListInRecyclerView(kontaktlist)
                scenario.adapterRV?.filter?.filter("Test")
            }
        })
        onView(withText("Test"))
            .check(
                matches(
                    withEffectiveVisibility(
                        Visibility.VISIBLE
                    )
                )
            )
        onView(withText("Nachname2"))
            .check(
                doesNotExist()
            )
    }

    //Prüft ob Suche mit KontoNummer möglich
    @Test
    fun searchWithKontoNumber() {
        val scenario = rule.launchActivity(null)

        val kontakt = Kontakt("1", "Test")
        kontakt.kNumber = "TestA1"
        val kontakt2 = Kontakt("2", "Nachname2")
        kontakt2.kNumber = "B123123"
        val kontaktlist = listOf(kontakt, kontakt2)

        scenario.runOnUiThread(object : Runnable {
            override fun run() {
                scenario.displayKontakteListInRecyclerView(kontaktlist)
                scenario.adapterRV?.filter?.filter("B123123")
            }
        })

        onView(withText("Nachname2"))
            .check(
                matches(
                    withEffectiveVisibility(
                        Visibility.VISIBLE
                    )
                )
            )
        onView(withText("Test"))
            .check(
                doesNotExist()
            )
    }
}
