package at.sysco.erp_connect.activity

import android.content.Intent
import android.net.Uri
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.*
import androidx.test.espresso.intent.rule.IntentsTestRule
import androidx.test.espresso.matcher.RootMatchers.withDecorView
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.rule.ActivityTestRule
import at.sysco.erp_connect.R.*
import androidx.test.espresso.matcher.ViewMatchers.*
import at.sysco.erp_connect.kontakte_list.KontakteListActivity
import org.junit.Before

import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import at.sysco.erp_connect.constants.FailureCode
import at.sysco.erp_connect.constants.FinishCode
import at.sysco.erp_connect.kontakte_detail.KontakteDetailActivity
import at.sysco.erp_connect.pojo.Kontakt
import org.hamcrest.CoreMatchers.*

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */

//Klasse welche die KontakteListActivity testet
//Dafür wurde die Library Espresso verwendet

@RunWith(AndroidJUnit4ClassRunner::class)
class TestKontaktDetailActivity {
    //Für normale Activity-Tests.
    var rule: ActivityTestRule<KontakteDetailActivity> =
        ActivityTestRule(KontakteDetailActivity::class.java)
    //Benötigt wenn bei einer Activity auch Intents geprüft werden sollen.
    var testRuleWithIntents: IntentsTestRule<KontakteDetailActivity> =
        IntentsTestRule<KontakteDetailActivity>(KontakteDetailActivity::class.java)
    lateinit var activity: KontakteListActivity
    val kontakt = Kontakt("230A002")

    @Before
    fun init() {
        kontakt.kFirstName = "Max"
        kontakt.kLastName = "Mustermann"
        kontakt.kSex = "Männlich"
        kontakt.kAbteilung = "Toll"
        kontakt.kFunction = "funktion"
        kontakt.kMail = "test@web.de"
        kontakt.kURL = "www.test.at"
        kontakt.kMobilCountry = "0043"
        kontakt.kMobilNumber = "4000"
        kontakt.kMobilOperator = "0660"
        kontakt.kTelNumber = "400"
        kontakt.kTelCity = "1111"
        kontakt.kTelCountry = "43"

    }

    //Prüft ob Funktion welche das Ladessymbol anzeigt richtig funktioniert!
    @Test
    fun showAndHideProgress() {
        val scenario = rule.launchActivity(null)
        scenario.runOnUiThread(object : Runnable {
            override fun run() {
                scenario.showProgress()
            }
        })
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

    //Prüft ob Sucess-Snackbar dargestellt wird
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

    //Testet ob alle Daten dargestellt werden
    @Test
    fun testDisplayData() {
        val scenario = rule.launchActivity(null)
        scenario.runOnUiThread(object : Runnable {
            override fun run() {
                scenario.setTextData(kontakt)
            }
        })
        onView(withText("Max")).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
        onView(withText("Mustermann")).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
        onView(withText("Männlich")).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
        onView(withText("Toll")).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
        onView(withText("funktion")).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
        onView(withText("test@web.de")).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
        onView(withText("www.test.at")).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
        onView(withText("0043")).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
        onView(withText("4000")).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
        onView(withText("0660")).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
        onView(withText("400")).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
        onView(withText("1111")).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
        onView(withText("43")).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
    }

    //Testet ob bei leeren Inhalten, "Fehler"-Toast geworfen wird.
    @Test
    fun testEmptyCallListener() {
        val scenario = rule.launchActivity(null)
        val emptyKontakt = Kontakt()
        scenario.runOnUiThread(object : Runnable {
            override fun run() {
                scenario.initListener(emptyKontakt)
            }
        })
        onView(withId(id.buttonCall)).perform(click())
        onView(withText("Nicht genug Daten vorhanden!")).inRoot(withDecorView(not(`is`(scenario.window.decorView))))
            .check(matches(isDisplayed()))
    }

    //Prüft ob Fehler geworfen wird, wenn keine Daten vorhanden sind
    @Test
    fun testEmptyURLListener() {
        val scenario = rule.launchActivity(null)
        val emptyKontakt = Kontakt()
        scenario.runOnUiThread(object : Runnable {
            override fun run() {
                scenario.initListener(emptyKontakt)
            }
        })
        onView(withId(id.buttonURL)).perform(click())
        onView(withText("Nicht genug Daten vorhanden!")).inRoot(withDecorView(not(`is`(scenario.window.decorView))))
            .check(matches(isDisplayed()))
    }

    //Testet ob Anruf ausgeführt wird
    @Test
    fun testCallListener() {
        val scenario = testRuleWithIntents.launchActivity(null)
        scenario.runOnUiThread(object : Runnable {
            override fun run() {
                scenario.initListener(kontakt)
            }
        })
        onView(withId(id.buttonCall)).perform(click())
        intended(
            allOf(
                hasAction(Intent.ACTION_DIAL),
                hasData("tel:431111400")
            )
        )
        Intents.release()
    }

    //Testet ob Anruf mit fehlenden Informationen
    @Test
    fun testCallMissingListener() {
        kontakt.kTelCountry = null
        val scenario = testRuleWithIntents.launchActivity(null)
        scenario.runOnUiThread(object : Runnable {
            override fun run() {
                scenario.initListener(kontakt)
            }
        })
        onView(withId(id.buttonCall)).perform(click())
        intended(
            allOf(
                hasAction(Intent.ACTION_DIAL),
                hasData("tel:1111400")
            )
        )
        Intents.release()
    }

    //Prüft ob URL-Intent funktioniert
    @Test
    fun testURLListener() {
        val scenario = testRuleWithIntents.launchActivity(null)
        val webpage = Uri.parse("https://" + kontakt.kURL)
        scenario.runOnUiThread(object : Runnable {
            override fun run() {
                scenario.initListener(kontakt)

            }
        })
        onView(withId(id.buttonURL)).perform(click())
        intended(
            allOf(
                hasAction(Intent.ACTION_VIEW),
                hasData("https://www.test.at")
            )
        )
        Intents.release()
    }

    //Ob Fehlermeldung angegeben wurde, wenn kein Detail über Intent geliefert wurde.
    @Test
    fun testIntentFalse() {
        val scenario = rule.launchActivity(null)
        onView(withText(FailureCode.NO_DETAIL_NUMBER))
            .check(
                matches(
                    withEffectiveVisibility(
                        Visibility.VISIBLE
                    )
                )
            )
    }
}
