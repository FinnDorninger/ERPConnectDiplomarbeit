package at.sysco.erp_connect.kontakte_detail

import at.sysco.erp_connect.constants.FinishCode
import at.sysco.erp_connect.model.KontakteDetailModel
import at.sysco.erp_connect.pojo.Kontakt

class KontakteDetailPresenter(
    kontaktListView: KontakteDetailContract.View,
    val kontakteDetailModel: KontakteDetailModel
) : KontakteDetailContract.Presenter, KontakteDetailContract.Model.OnFinishedListener {
    var kontaktDetailView: KontakteDetailContract.View? = kontaktListView

    override fun onfinished(kontakt: Kontakt, finishCode: String) {
        kontaktDetailView?.setTextData(kontakt)
        if (finishCode != FinishCode.finishedOnWeb) {
            kontaktDetailView?.onSucess(finishCode)
        }
        kontaktDetailView?.hideProgress()
    }

    override fun onFailure(failureCode: String) {
        kontaktDetailView?.hideProgress()
        kontaktDetailView?.onError(failureCode)
    }

    override fun requestFromWS(kontaktNummer: String) {
        kontaktDetailView?.showProgress()
        kontakteDetailModel.getKontaktDetail(this, kontaktNummer)
    }

    override fun onDestroy() {
        this.kontaktDetailView = null
    }
}