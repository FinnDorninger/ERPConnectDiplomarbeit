package at.sysco.erp_connect.konto_detail

import at.sysco.erp_connect.constants.FinishCode
import at.sysco.erp_connect.pojo.Konto
import at.sysco.erp_connect.model.KontoDetailModel

//Presenter für die Kommunikation zwischen Model und View.
class KontoDetailPresenter(
    kontoListView: KontoDetailContract.View,
    val kontoDetailModel: KontoDetailModel
) : KontoDetailContract.Presenter, KontoDetailContract.Model.OnFinishedListener {
    var kontoDetailView: KontoDetailContract.View? = kontoListView

    //Wird aufgerufen wenn Model erfolgreich ist. Stellt Daten im View dar. Versteckt Ladebalken.
    override fun onfinished(konto: Konto, finishCode: String) {
        kontoDetailView?.setTextData(konto)
        if (finishCode != FinishCode.finishedOnWeb) {
            kontoDetailView?.onSucess(finishCode)
        }
        kontoDetailView?.hideProgress()
    }

    //Wird aufgerufen, wenn Model keine Daten laden kann. Ruft Methoden des Views auf welche den Fehler anzeigen
    override fun onFailure(failureCode: String) {
        kontoDetailView?.hideProgress()
        kontoDetailView?.onError(failureCode)
    }

    //Methode zum Laden von Daten aus dem Model
    override fun requestFromWS(kontoNummer: String) {
        kontoDetailView?.showProgress()
        kontoDetailModel.getKontoDetail(this, kontoNummer)
    }

    //Setzt View null, damit keine Referenz mehr zur Activity besteht
    override fun onDestroy() {
        this.kontoDetailView = null
    }
}