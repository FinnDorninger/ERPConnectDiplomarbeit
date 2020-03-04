package at.sysco.erp_connect.kontakte_detail

import at.sysco.erp_connect.constants.FinishCode
import at.sysco.erp_connect.model.KontakteDetailModel
import at.sysco.erp_connect.pojo.Kontakt

//Presenter für die Kommunikation zwischen Model und View.
class KontakteDetailPresenter(
    kontaktListView: KontakteDetailContract.View,
    val kontakteDetailModel: KontakteDetailModel
) : KontakteDetailContract.Presenter {
    var kontaktDetailView: KontakteDetailContract.View? = kontaktListView

    //Methode zum Laden von Daten aus dem Model
    override fun requestFromWS(kontaktNummer: String) {
        kontaktDetailView?.showProgress()
        kontakteDetailModel.getKontaktDetail(object :
            KontakteDetailContract.Model.OnFinishedListener {
            //Wird aufgerufen wenn Model erfolgreich ist. Stellt Daten im View dar. Versteckt Ladebalken.
            override fun onfinished(kontakt: Kontakt, finishCode: String) {
                kontaktDetailView?.initListener(kontakt)
                kontaktDetailView?.setTextData(kontakt)
                if (finishCode != FinishCode.finishedOnWeb) {
                    kontaktDetailView?.onSucess(finishCode)
                }
                kontaktDetailView?.hideProgress()
            }

            //Wird aufgerufen, wenn Model keine Daten laden kann. Ruft Methoden des Views auf welche den Fehler anzeigen
            override fun onFailure(failureCode: String) {
                kontaktDetailView?.hideProgress()
                kontaktDetailView?.onError(failureCode)
            }
        }, kontaktNummer)
    }

    //Setzt View null, damit keine Referenz mehr zur Activity besteht
    override fun onDestroy() {
        this.kontaktDetailView = null
    }
}