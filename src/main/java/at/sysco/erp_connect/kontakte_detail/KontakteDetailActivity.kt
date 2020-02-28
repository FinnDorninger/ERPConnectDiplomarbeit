package at.sysco.erp_connect.kontakte_detail

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import at.sysco.erp_connect.R
import at.sysco.erp_connect.constants.FailureCode
import at.sysco.erp_connect.model.KontakteDetailModel
import at.sysco.erp_connect.pojo.Kontakt
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_kontakte_detail.*

//Activity welche Kontakt-Details anzeigt
class KontakteDetailActivity : AppCompatActivity(), KontakteDetailContract.View {
    lateinit var kontaktDetailPresenter: KontakteDetailPresenter
    lateinit var kontaktNummer: String

    //Methode des Lifecycles. Setzt Layout und beauftragt Presenter für Beschaffung der Daten.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_kontakte_detail)

        //Ruft Extras auf welche angehängt wurden. Extra ist die ID des zu ladenden Kontakt/Ansprechpartner
        val extra = intent.getStringExtra("id")
        if (extra != null) {
            kontaktNummer = extra
        }
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
            val snackbar: Snackbar =
                Snackbar.make(this.layout_kontoDetail, title, Snackbar.LENGTH_INDEFINITE)
            snackbar.setAction(
                "Retry!"
            ) { kontaktDetailPresenter.requestFromWS(kontaktNummer) }
            snackbar.show()
        } else {
            val snackbar: Snackbar =
                Snackbar.make(findViewById(android.R.id.content), title, Snackbar.LENGTH_LONG)
            snackbar.show()
        }
    }

    //Methode welche die Daten aus dem Presenter bzw. Model darstellt. Setzt auch Listener für die Aktions-Buttons
    override fun setTextData(kontakt: Kontakt) {
        tableDetails.visibility = View.VISIBLE
        textInputName.text = if (kontakt.kLastName != null) {
            if (kontakt.kFirstName != null) {
                kontakt.kLastName.plus(" ").plus(kontakt.kFirstName)
            } else {
                kontakt.kLastName
            }
        } else {
            if (kontakt.kFirstName != null) {
                kontakt.kFirstName
            } else {
                ""
            }
        }
        textInputVorname.text = kontakt.kFirstName
        textInputNachname.text = kontakt.kLastName
        textInputSex.text = kontakt.kSex
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

        if (kontakt.kSex == "1") {
            textInputSex.text = getString(R.string.sexWeiblich)
        } else if (kontakt.kSex == "0") {
            textInputSex.text = getString(R.string.sexMännlich)
        } else {
            textInputSex.text = "/"
        }

        buttonCall.setOnClickListener {
            dialNumber(kontakt)
        }

        buttonMail.setOnClickListener {
            mailNumber(kontakt)
        }

        buttonURL.setOnClickListener {
            openURL(kontakt)
        }
    }

    //Funktion für den URL-Button. Startet Intent für das öffnen einer Webseite
    private fun openURL(kontakt: Kontakt) {
        var url = kontakt.kURL
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
                    Toast.makeText(this, "Kein passender Browser vorhanden!", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        } else {
            Toast.makeText(this, "Keine URL vorhanden!", Toast.LENGTH_SHORT).show()
        }

    }

    //Startet Intent welches eine Telefonnummer anruft.
    private fun dialNumber(kontakt: Kontakt) {
        var telNumber = kontakt.kTelNumber
        val telNumberCity = kontakt.kTelCity
        val telNumberCountry = kontakt.kTelCountry

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
            } else {
                Toast.makeText(this, "Keine Telefon-App vorhanden!", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Keine vollständige Telefonnummer vorhanden!", Toast.LENGTH_SHORT)
                .show()
        }
    }

    //Startet Intent welcher eine E-Mail-Konversation an hinterlegte Adresse startet.
    private fun mailNumber(kontakt: Kontakt) {
        val mail = kontakt.kMail
        if (mail != null && android.util.Patterns.EMAIL_ADDRESS.matcher(mail).matches()) {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:$mail")
            }
            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
            } else {
                Toast.makeText(this, "Keine Mail-App vorhanden!", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Keine E-Mail-Adresse vorhanden!", Toast.LENGTH_SHORT).show()
        }
    }
}