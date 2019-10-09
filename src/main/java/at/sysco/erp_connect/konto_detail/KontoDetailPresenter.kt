package at.sysco.erp_connect.konto_detail

import android.util.Log
import at.sysco.erp_connect.pojo.Konto
import at.sysco.erp_connect.model.KontoDetailModel

class KontoDetailPresenter(
    kontoListView: KontoDetailContract.View,
    val kontoDetailModel: KontoDetailModel
) : KontoDetailContract.Presenter, KontoDetailContract.Model.OnFinishedListener {
    var kontoDetailView: KontoDetailContract.View? = kontoListView

    override fun onfinished(konto: Konto) {
        kontoDetailView?.setTextData(konto)
        kontoDetailView?.hideProgress()
    }

    override fun onFailureFileLoad() {
        Log.w("Presenter", "Failed loading file")
    }

    override fun requestFromWS(kontoNummer: String) {
        kontoDetailView?.showProgress()
        kontoDetailModel.getKontoDetail(this, kontoNummer)
    }

    override fun onDestroy() {
        this.kontoDetailView = null
    }
}