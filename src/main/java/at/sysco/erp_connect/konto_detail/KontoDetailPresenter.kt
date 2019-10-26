package at.sysco.erp_connect.konto_detail

import at.sysco.erp_connect.constants.FinishCode
import at.sysco.erp_connect.pojo.Konto
import at.sysco.erp_connect.model.KontoDetailModel

class KontoDetailPresenter(
    kontoListView: KontoDetailContract.View,
    val kontoDetailModel: KontoDetailModel
) : KontoDetailContract.Presenter, KontoDetailContract.Model.OnFinishedListener {
    var kontoDetailView: KontoDetailContract.View? = kontoListView

    override fun onfinished(konto: Konto, finishCode: String) {
        kontoDetailView?.setTextData(konto)
        if (finishCode != FinishCode.finishedOnWeb) {
            kontoDetailView?.onSucess(finishCode)
        }
    }

    override fun onFailure(failureCode: String) {
        kontoDetailView?.onError(failureCode)
    }
    override fun requestFromWS(kontoNummer: String) {
        kontoDetailModel.getKontoDetail(this, kontoNummer)
    }

    override fun onDestroy() {
        this.kontoDetailView = null
    }
}