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
import at.sysco.erp_connect.konto_detail.KontoDetailActivity
import at.sysco.erp_connect.pojo.Konto
import org.junit.Before

import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import at.sysco.erp_connect.constants.FailureCode
import at.sysco.erp_connect.constants.FinishCode
import at.sysco.erp_connect.pojo.KontoUtility
import org.hamcrest.CoreMatchers.*
import java.net.URLEncoder


/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */

//Klasse welche die KontakteListActivity testet
//Dafür wurde die Library Espresso verwendet

@RunWith(AndroidJUnit4ClassRunner::class)
class TestKontoDetailActivity {
    //Für normale Activity-Tests.
    var rule: ActivityTestRule<KontoDetailActivity> =
        ActivityTestRule(KontoDetailActivity::class.java)
    //Benötigt wenn bei einer Activity auch Intents geprüft werden sollen.
    var testRuleWithIntents: IntentsTestRule<KontoDetailActivity> =
        IntentsTestRule<KontoDetailActivity>(KontoDetailActivity::class.java)
    lateinit var activity: KontakteListActivity
    val konto = Konto("230A002")

    @Before
    fun init() {
        konto.kName = "Ammansberger"
        konto.kPlz = "8055"
        konto.kCity = "Graz"
        konto.kStreet = "Street"
        konto.kMail = "finn@test.at"
        konto.kUrl = "www.test.at"
        konto.kTelMain = "4563"
        konto.kTelCity = "07582"
        konto.kTelCountry = "0043"
        konto.kNote = "Hallo"
        konto.kMobilOperatorTel = "0660"
        konto.kMobilTel = "400"
        konto.kMobilCountry = "0043"
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
                scenario.setTextData(konto)
            }
        })
        onView(withText("(230A002)")).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
        onView(withText("Ammansberger")).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
        onView(withText("8055")).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
        onView(withText("Graz")).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
        onView(withText("Street")).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
        onView(withText("finn@test.at")).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
        onView(withText("4563")).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
        onView(withText("07582")).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
        onView(withText("0043")).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
        onView(withText("Hallo")).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
    }

    //Testet ob bei leeren Inhalten, "Fehler"-Toast geworfen wird.
    @Test
    fun testEmptyCallListener() {
        val scenario = rule.launchActivity(null)
        val emptyKonto = Konto()
        scenario.runOnUiThread(object : Runnable {
            override fun run() {
                scenario.initListener(emptyKonto)
            }
        })
        onView(withId(id.buttonCall)).perform(click())
        onView(withText("Nicht genug Daten vorhanden!")).inRoot(withDecorView(not(`is`(scenario.window.decorView))))
            .check(matches(isDisplayed()))
    }

    //Prüft ob Fehler geworfen wird, wenn keine Daten vorhanden sind
    @Test
    fun testEmptySMSListener() {
        val scenario = rule.launchActivity(null)
        val emptyKonto = Konto()
        scenario.runOnUiThread(object : Runnable {
            override fun run() {
                scenario.initListener(emptyKonto)
            }
        })
        onView(withId(id.buttonSMS)).perform(click())
        onView(withText("Nicht genug Daten vorhanden!")).inRoot(withDecorView(not(`is`(scenario.window.decorView))))
            .check(matches(isDisplayed()))
    }

    //Prüft ob Fehler geworfen wird, wenn keine Daten vorhanden sind
    @Test
    fun testEmptyURLListener() {
        val scenario = rule.launchActivity(null)
        val emptyKonto = Konto()
        scenario.runOnUiThread(object : Runnable {
            override fun run() {
                scenario.initListener(emptyKonto)
            }
        })
        onView(withId(id.buttonURL)).perform(click())
        onView(withText("Nicht genug Daten vorhanden!")).inRoot(withDecorView(not(`is`(scenario.window.decorView))))
            .check(matches(isDisplayed()))
    }

    //Prüft ob Fehler geworfen wird, wenn keine Daten vorhanden sind bei Maps Button
    @Test
    fun testEmptyMapsListener() {
        val scenario = rule.launchActivity(null)
        val emptyKonto = Konto()
        scenario.runOnUiThread(object : Runnable {
            override fun run() {
                scenario.initListener(emptyKonto)
            }
        })
        onView(withId(id.buttonMap)).perform(click())
        onView(withText("Nicht genug Daten vorhanden!")).inRoot(withDecorView(not(`is`(scenario.window.decorView))))
            .check(matches(isDisplayed()))
    }

    //Testet ob Anruf ausgeführt wird
    @Test
    fun testCallListener() {
        val scenario = testRuleWithIntents.launchActivity(null)
        scenario.runOnUiThread(object : Runnable {
            override fun run() {
                scenario.initListener(konto)
            }
        })
        onView(withId(id.buttonCall)).perform(click())
        intended(
            allOf(
                hasAction(Intent.ACTION_DIAL),
                hasData("tel:0043075824563")
            )
        )
        Intents.release()
    }

    //Testet ob Anruf mit fehlenden Informationen
    @Test
    fun testCallMissingListener() {
        konto.kTelCountry = null
        val scenario = testRuleWithIntents.launchActivity(null)
        scenario.runOnUiThread(object : Runnable {
            override fun run() {
                scenario.initListener(konto)
            }
        })
        onView(withId(id.buttonCall)).perform(click())
        intended(
            allOf(
                hasAction(Intent.ACTION_DIAL),
                hasData("tel:075824563")
            )
        )
        Intents.release()
    }

    //Testet ob Google Maps ausgeführt wird
    @Test
    fun testMapListener() {
        val scenario = testRuleWithIntents.launchActivity(null)
        scenario.runOnUiThread(object : Runnable {
            override fun run() {
                scenario.initListener(konto)
            }
        })
        onView(withId(id.buttonMap)).perform(click())
        var url = "https://www.google.com/maps/search/?api=1&query="
        val adress = "8055, Graz, Street, "
        url += URLEncoder.encode(adress.removeSuffix(", "), "utf-8")
        val webpage: Uri = Uri.parse(url)
        intended(
            allOf(
                hasPackage("com.google.android.apps.maps"),
                hasData(webpage)
            )
        )
        Intents.release()
    }

    //Testet ob Google Maps ausgeführt wird, wenn etwas fehlt!
    @Test
    fun testMapMissingSomethingListener() {
        val scenario = testRuleWithIntents.launchActivity(null)
        konto.kCity = null
        scenario.runOnUiThread(object : Runnable {
            override fun run() {
                scenario.initListener(konto)
            }
        })
        onView(withId(id.buttonMap)).perform(click())
        var url = "https://www.google.com/maps/search/?api=1&query="
        val adress = "8055, Street, "
        url += URLEncoder.encode(adress.removeSuffix(", "), "utf-8")
        val webpage: Uri = Uri.parse(url)
        intended(
            allOf(
                hasPackage("com.google.android.apps.maps"),
                hasData(webpage)
            )
        )
        Intents.release()
    }

    //Prüft ob URL-Intent funktioniert
    @Test
    fun testURLListener() {
        val scenario = testRuleWithIntents.launchActivity(null)
        var webpage = Uri.parse("https://" + konto.kUrl)
        scenario.runOnUiThread(object : Runnable {
            override fun run() {
                scenario.initListener(konto)

            }
        })
        onView(withId(id.buttonURL)).perform(click())
        intended(
            allOf(
                hasAction(Intent.ACTION_VIEW),
                hasData(webpage)
            )
        )
        Intents.release()
    }

    //Prüft ob URL-Intent funktioniert
    @Test
    fun testSMSListener() {
        val scenario = testRuleWithIntents.launchActivity(null)
        var mobilnr: String? = null
        scenario.runOnUiThread(object : Runnable {
            override fun run() {
                scenario.initListener(konto)
                mobilnr = KontoUtility.createMobilNumber(konto)
            }
        })
        onView(withId(id.buttonSMS)).perform(click())
        intended(
            allOf(
                hasAction(Intent.ACTION_SENDTO),
                hasData(Uri.parse("smsto:$mobilnr"))
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
