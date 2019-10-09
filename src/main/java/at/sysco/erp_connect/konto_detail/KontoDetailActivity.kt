package at.sysco.erp_connect.konto_detail

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import at.sysco.erp_connect.R
import at.sysco.erp_connect.pojo.Konto
import at.sysco.erp_connect.model.KontoDetailModel
import kotlinx.android.synthetic.main.activity_konto_detail.*

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
        tKontoCity.text = "City: " + konto.kCity
        tKontoCityNumber.text = "PLZ: " + konto.kPlz
        tKontoCountry.text = "Country: " + konto.kCountry
        tKontoMail.text = "Mail: " + konto.kMail
        tKontoName.text = "Name: " + konto.kName
        tKontoNote.text = "Notiz: " + konto.kNote
        tKontoNumber.text = "Nummer: " + konto.kNumber
        tKontoPhone.text = "Phone: " + konto.kTelMain
        tKontoWww.text = "WWW: " + konto.kUrl
    }
}
