package at.sysco.erp_connect.konto_detail

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import at.sysco.erp_connect.R
import at.sysco.erp_connect.pojo.Konto
import at.sysco.erp_connect.model.KontoDetailModel
import kotlinx.android.synthetic.main.activity_konto_detail.*

//TO-DO: Error anzeigen, wenn Daten nicht genug Daten für Funktionen vorhanden!
class KontoDetailActivity : AppCompatActivity(), KontoDetailContract.View {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_konto_detail)

        val kontoNummer = intent.getStringExtra("id")
        val kontoDetailPresenter = KontoDetailPresenter(this, KontoDetailModel(this))
        kontoDetailPresenter.requestFromWS(kontoNummer)
    }

    override fun showProgress() {
        Log.w("Finn", "Loading")
    }

    override fun hideProgress() {
        Log.w("Finn", "Hiding")
    }

    override fun setTextData(konto: Konto) {
        textName.text = konto.kName
        textKontoNumber.text = "(" + konto.kNumber + ")"
        textPhone.text = konto.kTelMain

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
        var adressList = listOf(konto.kPlz, konto.kCity, konto.kStreet, konto.kCountry)
        var adress = "https://www.google.com/maps/search/?api=1"

        for (part in adressList) {
            if (part != null) {
                var newPart = part.replace(" ", "+")
                adress += "$newPart%2C"
            } else {
                if (adress.endsWith("%2C")) {
                    adress.removeSuffix("%2C")
                }
            }
        }

        val webpage: Uri = Uri.parse(adress)
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

        if (telNumber != null) {
            if (telNumberCity != null) {
                telNumber = "$telNumberCity$telNumber"
                if (telNumberCountry != null) {
                    telNumber = "$telNumberCountry$telNumber"
                    Log.w("Finn", "Du Depp")
                }
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
