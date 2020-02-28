package at.sysco.erp_connect.kontakte_list

import at.sysco.erp_connect.constants.FinishCode
import at.sysco.erp_connect.model.KontakteListModel
import at.sysco.erp_connect.pojo.Kontakt

//Verbindung zwischen View und Model
class KontakteListPresenter(
    kontakteListView: KontakteListContract.View,
    private val kontakteListModel: KontakteListModel
) : KontakteListContract.Presenter, KontakteListContract.Model.OnFinishedListener {
    var kontakteListView: KontakteListContract.View? = kontakteListView

    //Methode welche aufgerufen wird nach Erfolg des Models. Ruft Methoden zum Darstellen von Daten in der View auf.
    override fun onfinished(kontaktArrayList: List<Kontakt>, finishCode: String) {
        kontakteListView?.displayKontakteListInRecyclerView(kontaktArrayList)
        kontakteListView?.hideProgress()
        if (finishCode != FinishCode.finishedOnWeb) {
            kontakteListView?.onSucess(finishCode)
        } else {
            val string = kontakteListModel.saveKontakte(kontaktArrayList)
            kontakteListView?.onSucess(string)
        }
    }

    //Methode welche bei Fehlern in der Datenbeschaffung aufgerufen wird.
    override fun onFailure(failureCode: String) {
        kontakteListView?.hideProgress()
        kontakteListView?.onError(failureCode)
    }

    //Beauftragt Model mit der Datenbeschaffung
    override fun requestFromWS() {
        kontakteListView?.showProgress()
        kontakteListModel.getKontakteList(this)
    }

    //Setzt View null damit keine Referenz mehr zur Activity besteht.
    override fun onDestroy() {
        this.kontakteListView = null
    }
}