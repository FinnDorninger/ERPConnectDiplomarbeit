package at.sysco.erp_connect.kontakte_detail

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import at.sysco.erp_connect.R
import at.sysco.erp_connect.constants.FailureCode
import at.sysco.erp_connect.model.KontakteDetailModel
import at.sysco.erp_connect.pojo.Kontakt
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_kontakte_detail.*

//TO-DO: Error anzeigen, wenn Daten nicht genug Daten für Funktionen vorhanden!
class KontakteDetailActivity : AppCompatActivity(), KontakteDetailContract.View {
    lateinit var kontaktDetailPresenter: KontakteDetailPresenter
    lateinit var kontaktNummer: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_kontakte_detail)

        val extra = intent.getStringExtra("id")
        if (extra != null) {
            kontaktNummer = extra
        }
        kontaktDetailPresenter = KontakteDetailPresenter(this, KontakteDetailModel(this))
        kontaktDetailPresenter.requestFromWS(kontaktNummer)
    }

    override fun showProgress() {
        tableDetails.visibility = View.VISIBLE
    }

    override fun hideProgress() {
        progressBar.visibility = View.GONE
    }

    override fun onSucess(finishCode: String) {
        showSnackbar(finishCode, false)
    }

    override fun onError(failureCode: String) {
        when (failureCode) {
            FailureCode.ERROR_LOADING_FILE -> showSnackbar(failureCode, true)
            FailureCode.NO_DATA -> showSnackbar(failureCode, true)
            FailureCode.ERROR_SAVING_FILE -> showSnackbar(failureCode, false)
            FailureCode.NOT_ENOUGH_SPACE -> showSnackbar(failureCode, false)
        }
    }

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

    override fun setTextData(kontakt: Kontakt) {
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

    private fun openURL(kontakt: Kontakt) {
        val url = kontakt.kURL
        if (url != null && android.util.Patterns.WEB_URL.matcher(url).matches()) {
            val webpage: Uri = Uri.parse(url)
            val intent = Intent(Intent.ACTION_VIEW, webpage)
            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
            }
        }
    }

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
            }
        }
    }

    private fun mailNumber(kontakt: Kontakt) {
        val mail = kontakt.kMail
        if (mail != null && android.util.Patterns.EMAIL_ADDRESS.matcher(mail).matches()) {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:$mail")
            }
            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
            }
        }
    }
}