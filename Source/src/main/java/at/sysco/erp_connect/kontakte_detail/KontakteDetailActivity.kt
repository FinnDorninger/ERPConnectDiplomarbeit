package at.sysco.erp_connect.kontakte_detail

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import at.sysco.erp_connect.pojo.KontaktUtility
import at.sysco.erp_connect.R
import at.sysco.erp_connect.constants.FailureCode
import at.sysco.erp_connect.model.KontakteDetailModel
import at.sysco.erp_connect.pojo.Kontakt
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_kontakte_detail.*
import kotlinx.android.synthetic.main.activity_kontakte_detail.buttonCall
import kotlinx.android.synthetic.main.activity_kontakte_detail.buttonURL
import kotlinx.android.synthetic.main.activity_kontakte_detail.layout_kontoDetail
import kotlinx.android.synthetic.main.activity_kontakte_detail.progressBar
import kotlinx.android.synthetic.main.activity_kontakte_detail.tableDetails
import kotlinx.android.synthetic.main.activity_kontakte_detail.textInputMail
import kotlinx.android.synthetic.main.activity_kontakte_detail.textInputName
import kotlinx.android.synthetic.main.activity_kontakte_detail.textInputPhoneNumber
import kotlinx.android.synthetic.main.activity_kontakte_detail.textInputPrefixCity
import kotlinx.android.synthetic.main.activity_kontakte_detail.textInputPrefixCountry
import kotlinx.android.synthetic.main.activity_kontakte_detail.textInputWWW

//Activity welche Kontakt-Details anzeigt
class KontakteDetailActivity : AppCompatActivity(), KontakteDetailContract.View {
    lateinit var kontaktDetailPresenter: KontakteDetailPresenter
    lateinit var kontaktNummer: String
    var toast: Toast? = null

    //Methode des Lifecycles. Setzt Layout und beauftragt Presenter für Beschaffung der Daten.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_kontakte_detail)

        //Ruft Extras auf welche angehängt wurden. Extra ist die ID des zu ladenden Kontakt/Ansprechpartner
        val extra = intent.getStringExtra("id")
        if (extra != null) {
            kontaktNummer = extra
            startPresenterRequest(kontaktNummer)
        } else {
            showSnackbar(FailureCode.NO_DETAIL_NUMBER, true)
            kontaktNummer = "notvalidkontaktnumber"
        }
    }

    //Startet den Request an den Presenter
    override fun startPresenterRequest(kontaktNummer: String) {
        kontaktDetailPresenter = KontakteDetailPresenter(this, KontakteDetailModel(this))
        kontaktDetailPresenter.requestFromWS(kontaktNummer)
    }

    //Darstellung der Daten
    override fun showProgress() {
        progressBar.visibility = View.VISIBLE
    }

    //Versteckt den Ladebalken
    override fun hideProgress() {
        progressBar.visibility = View.GONE
    }

    //Bei Erfolg wird eine Snackbar aufgerufen
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
            if (title != FailureCode.NO_DETAIL_NUMBER) {
                val snackbar: Snackbar =
                    Snackbar.make(this.layout_kontoDetail, title, Snackbar.LENGTH_INDEFINITE)
                snackbar.setAction(
                    "Retry!"
                ) { kontaktDetailPresenter.requestFromWS(kontaktNummer) }
                snackbar.show()
            } else {
                val snackbar: Snackbar =
                    Snackbar.make(this.layout_kontoDetail, title, Snackbar.LENGTH_INDEFINITE)
                snackbar.setAction(
                    "Zurück!"
                ) { finish() }
                snackbar.show()
            }
        } else {
            val snackbar: Snackbar =
                Snackbar.make(findViewById(android.R.id.content), title, Snackbar.LENGTH_LONG)
            snackbar.show()
        }
    }

    //Methode welche die Daten aus dem Presenter bzw. Model darstellt. Setzt auch Listener für die Aktions-Buttons
    override fun setTextData(kontakt: Kontakt) {
        tableDetails.visibility = View.VISIBLE
        textInputName.text = KontaktUtility.calculateName(kontakt)
        textInputVorname.text = kontakt.kFirstName
        textInputNachname.text = kontakt.kLastName
        textInputAbteilung.text = kontakt.kAbteilung
        textInputFunktion.text = kontakt.kFunction
        textInputMail.text = kontakt.kMail
        textInputWWW.text = kontakt.kURL
        textInputMobilPrefixCountry.text = kontakt.kMobilCountry
        textInputPrefixMobilOperator.text = kontakt.kMobilOperator
        textInputMobilPhoneNumber.text = kontakt.kMobilNumber
        textInputPrefixCountry.text = kontakt.kTelCountry
        textInputPrefixCity.text = kontakt.kTelCity
        textInputPhoneNumber.text = kontakt.kTelNumber
        textInputSex.text = KontaktUtility.calculateSex(kontakt.kSex)
    }

    //Setzt Listener der Buttons
    override fun initListener(kontakt: Kontakt) {
        val mail = kontakt.kMail
        val number = KontaktUtility.calculateNumber(kontakt)
        val url = kontakt.kURL
        val color = Color.rgb(216, 27, 96)

        if (!number.isBlank()) {
            buttonCall.setOnClickListener {
                dialNumber(number)
            }
            buttonCall.setColorFilter(color)
        } else {
            buttonCall.setOnClickListener {
                notEnoughData()
            }
        }

        if (!mail.isNullOrBlank()) {
            buttonMail.setOnClickListener {
                startMail(mail)
            }
            buttonMail.setColorFilter(color)
        } else {
            buttonMail.setOnClickListener {
                notEnoughData()
            }
        }

        if (!url.isNullOrBlank()) {
            buttonURL.setOnClickListener {
                openURL(url)
            }
            buttonURL.setColorFilter(color)
        } else {
            buttonURL.setOnClickListener {
                notEnoughData()
            }
        }
    }

    //Fehler Meldung wenn nicht genug Daten für eine Button-Funktion vorhanden sind.
    private fun notEnoughData() {
        toast = Toast.makeText(this, "Nicht genug Daten vorhanden!", Toast.LENGTH_SHORT)
        toast?.show()
    }

    //Funktion für den URL-Button. Startet Intent für das öffnen einer Webseite
    private fun openURL(urlInput: String) {
        var url = urlInput
        if (url.startsWith("http://") or url.startsWith("https://")) {
            val webpage: Uri = Uri.parse(url)
            val intent = Intent(Intent.ACTION_VIEW, webpage)
            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
            }
        } else {
            url = "https://$url"
            val webpage: Uri = Uri.parse(url)
            val intent = Intent(Intent.ACTION_VIEW, webpage)
            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
            } else {
                toast =
                    Toast.makeText(this, "Kein passender Browser vorhanden!", Toast.LENGTH_SHORT)
                toast?.show()
            }
        }

    }

    //Startet Intent welches eine Telefonnummer anruft.
    private fun dialNumber(telNumber: String) {
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$telNumber")
        }
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        } else {
            toast = Toast.makeText(this, "Keine Telefon-App vorhanden!", Toast.LENGTH_SHORT)
            toast?.show()
        }
    }

    //Startet Intent welcher eine E-Mail-Konversation an hinterlegte Adresse startet.
    private fun startMail(mail: String) {
        if (android.util.Patterns.EMAIL_ADDRESS.matcher(mail).matches()) {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:$mail")
            }
            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
            } else {
                toast = Toast.makeText(this, "Keine Mail-App vorhanden!", Toast.LENGTH_SHORT)
                toast?.show()
            }
        } else {
            toast =
                Toast.makeText(this, "Keine gültige E-Mail-Adresse vorhanden!", Toast.LENGTH_SHORT)
            toast?.show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        toast?.cancel()
    }
}