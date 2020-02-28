package at.sysco.erp_connect.konto_detail

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import at.sysco.erp_connect.R
import at.sysco.erp_connect.constants.FailureCode
import at.sysco.erp_connect.kontakte_list.KontakteListActivity
import at.sysco.erp_connect.pojo.Konto
import at.sysco.erp_connect.model.KontoDetailModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_konto_detail.*
import java.net.URLEncoder

//Activity welche Konto/Kunden-Details anzeigt
class KontoDetailActivity : AppCompatActivity(), KontoDetailContract.View {
    lateinit var kontoDetailPresenter: KontoDetailPresenter
    lateinit var kontoNummer: String

    //Methode des Lifecycles. Setzt Layout und beauftragt Presenter für Beschaffung der Daten.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_konto_detail)
        val extra = intent.getStringExtra("id")
        if (extra != null) {
            kontoNummer = extra
        }
        kontoDetailPresenter = KontoDetailPresenter(this, KontoDetailModel(this))
        kontoDetailPresenter.requestFromWS(kontoNummer)

        buttonAnsprechpartner.setOnClickListener {
            startKontakte(kontoNummer)
        }
    }

    //Versteckt Ladebalken
    override fun hideProgress() {
        progressBar.visibility = View.GONE
    }

    //Darstellung des Ladebalkens
    override fun showProgress() {
        progressBar.visibility = View.VISIBLE
    }

    //Stellt erfolgreiches Laden dar ("Daten geladen aber vlt nicht aktuell etc")
    override fun onSucess(finishCode: String) {
        showSnackbar(finishCode, false)
    }

    //Prüft welcher Fehler vorherrscht, und ruft dann showSnackbar auf.
    override fun onError(failureCode: String) {
        when (failureCode) {
            FailureCode.ERROR_LOADING_FILE -> showSnackbar(failureCode, true)
            FailureCode.NO_DATA -> showSnackbar(failureCode, true)
            FailureCode.NO_CONNECTION -> showSnackbar(failureCode, true)
            else -> showSnackbar(failureCode, false)
        }
    }

    //Darstellung von Fehlermeldungen oder Erfolsmeldungen in Snackbar
    private fun showSnackbar(title: String, withAction: Boolean) {
        if (withAction) {
            val snackbar: Snackbar =
                Snackbar.make(this.layout_kontoDetail, title, Snackbar.LENGTH_INDEFINITE)
            snackbar.setAction(
                "Retry!"
            ) { kontoDetailPresenter.requestFromWS(kontoNummer) }
            snackbar.show()
        } else {
            val snackbar: Snackbar =
                Snackbar.make(findViewById(android.R.id.content), title, Snackbar.LENGTH_LONG)
            snackbar.show()
        }
    }

    //Methode welche die Daten aus dem Presenter bzw. Model darstellt. Setzt auch Listener für die Aktions-Buttons
    override fun setTextData(konto: Konto) {
        tableDetails.visibility = View.VISIBLE
        textInputName.text = konto.kName
        textInputKontoNumber.text = "(".plus(konto.kNumber).plus(")")
        textInputStaat.text = konto.kCountry
        textInputPLZ.text = konto.kPlz
        textInputCity.text = konto.kCity
        textInputStreet.text = konto.kStreet
        textInputMail.text = konto.kMail
        textInputWWW.text = konto.kUrl
        textInputPrefixCountry.text = konto.kTelCountry
        textInputPrefixCity.text = konto.kTelCity
        textInputPhoneNumber.text = konto.kTelMain
        textInputNote.text = konto.kNote

        buttonCall.setOnClickListener {
            dialNumber(konto)
        }
        buttonURL.setOnClickListener {
            openURL(konto)
        }
        buttonMap.setOnClickListener {
            openAddress(konto)
        }
        buttonSMS.setOnClickListener {
            messageNumber(konto)
        }
    }

    //Wird ausgeführt nach ausführen des Buttons "Ihre Ansprechpartner". Intent auf Kontakte/Ansprechpartner-Activity
    //mit passende Suchdetails.
    private fun startKontakte(kontoNumber: String) {
        val intent = Intent(this, KontakteListActivity::class.java)
        intent.putExtra("searchdetail", kontoNumber)
        startActivity(intent)
    }

    //Startet Intent welche hinterlegte Adresse mit Google Maps öffnet
    private fun openAddress(konto: Konto) {
        val adressList = listOf(konto.kPlz, konto.kCity, konto.kStreet)
        val adressIterator = adressList.iterator()
        var url = "https://www.google.com/maps/search/?api=1&query="
        var adress = ""

        while (adressIterator.hasNext()) {
            adress += adressIterator.next() + ", "
        }
        url += URLEncoder.encode(adress.removeSuffix(", "), "utf-8")

        val webpage: Uri = Uri.parse(url)
        val intent = Intent(Intent.ACTION_VIEW, webpage)
        intent.setPackage("com.google.android.apps.maps")
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        } else {
            Toast.makeText(this, "Kein Google Maps installiert!", Toast.LENGTH_SHORT).show()
        }
    }

    //Funktion für den URL-Button. Startet Intent für das öffnen einer Webseite
    private fun openURL(konto: Konto) {
        var url = konto.kUrl
        if (url != null) {
            if (url.startsWith("http:") or url.startsWith("https:")) {
                val webpage: Uri = Uri.parse(url)
                val intent = Intent(Intent.ACTION_VIEW, webpage)
                if (intent.resolveActivity(packageManager) != null) {
                    startActivity(intent)
                }
            } else {
                url = "https:$url"
                val webpage: Uri = Uri.parse(url)
                val intent = Intent(Intent.ACTION_VIEW, webpage)
                if (intent.resolveActivity(packageManager) != null) {
                    startActivity(intent)
                } else {
                    Toast.makeText(this, "Google Maps benötigt!", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(this, "Keine URL vorhanden!", Toast.LENGTH_SHORT).show()
        }

    }

    //Startet Intent welches eine Telefonnummer anruft.
    private fun dialNumber(konto: Konto) {
        var telNumber = konto.kTelMain
        val telNumberCity = konto.kTelCity
        val telNumberCountry = konto.kTelCountry

        if (telNumber != null && telNumberCity != null) {
            telNumber = "$telNumberCity$telNumber"
            if (telNumberCountry != null) {
                telNumber = "$telNumberCountry$telNumber"
            }
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$telNumber")
            }
            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
                Toast.makeText(this, "Keine Telefon-App installiert!", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Zu wenige Informationen vorhanden!", Toast.LENGTH_SHORT).show()
        }
    }

    //Intent welcher eine hinterlegte Nummer in einem Telefonfenster öffnet
    private fun messageNumber(konto: Konto) {
        var telMobilNumber = konto.kMobilTel
        val telMobilProvider = konto.kMobilOperatorTel
        val telMobilCountry = konto.kMobilCountry

        if (telMobilNumber != null && telMobilProvider != null) {
            telMobilNumber = "$telMobilProvider$telMobilNumber"
            if (telMobilCountry != null) {
                telMobilNumber = "$telMobilCountry$telMobilNumber"
            }
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("smsto:$telMobilNumber")
            }
            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
            } else {
                Toast.makeText(this, "Keine SMS-App vorhanden!", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Zu wenige Informationen vorhanden!", Toast.LENGTH_SHORT).show()
        }
    }
}