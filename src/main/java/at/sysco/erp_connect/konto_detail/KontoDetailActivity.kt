package at.sysco.erp_connect.konto_detail

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import at.sysco.erp_connect.R
import at.sysco.erp_connect.constants.FailureCode
import at.sysco.erp_connect.pojo.Konto
import at.sysco.erp_connect.model.KontoDetailModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_konto_detail.*
import java.net.URLEncoder

//TO-DO: Error anzeigen, wenn Daten nicht genug Daten für Funktionen vorhanden!
class KontoDetailActivity : AppCompatActivity(), KontoDetailContract.View {
    override fun hideProgress() {
        progressBar.visibility = View.GONE
    }

    override fun showProgress() {
        tableDetails.visibility = View.VISIBLE
    }

    lateinit var kontoDetailPresenter: KontoDetailPresenter
    lateinit var kontoNummer: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_konto_detail)

        val extra = intent.getStringExtra("id")
        if (extra != null) {
            kontoNummer = extra
        }
        kontoDetailPresenter = KontoDetailPresenter(this, KontoDetailModel(this))
        kontoDetailPresenter.requestFromWS(kontoNummer)
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
            ) { kontoDetailPresenter.requestFromWS(kontoNummer) }
            snackbar.show()
        } else {
            val snackbar: Snackbar =
                Snackbar.make(findViewById(android.R.id.content), title, Snackbar.LENGTH_LONG)
            snackbar.show()
        }
    }

    override fun setTextData(konto: Konto) {
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
        }
    }

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
                }
            }
        }
    }

    private fun dialNumber(konto: Konto) {
        var telNumber = konto.kTelMain
        val telNumberCity = konto.kTelCity
        val telNumberCountry = konto.kTelCountry

        if (telNumber != null && telNumberCity != null) {
            telNumber = "$telNumberCity$telNumber"
            if (telNumberCountry != null) {
                telNumber = "$telNumberCountry$telNumber"
                Log.w("Finn", "Du Depp")
            }
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$telNumber")
            }
            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
            }
        }
    }

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
            }
        }
    }
}