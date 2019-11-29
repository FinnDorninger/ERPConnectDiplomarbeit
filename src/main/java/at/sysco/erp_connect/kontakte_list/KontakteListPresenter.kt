package at.sysco.erp_connect.kontakte_list

import at.sysco.erp_connect.constants.FinishCode
import at.sysco.erp_connect.model.KontakteListModel
import at.sysco.erp_connect.pojo.Kontakt

class KontakteListPresenter(
    kontakteListView: KontakteListContract.View,
    private val kontakteListModel: KontakteListModel
) : KontakteListContract.Presenter, KontakteListContract.Model.OnFinishedListener {
    var kontakteListView: KontakteListContract.View? = kontakteListView

    override fun onfinished(kontaktArrayList: List<Kontakt>, finishCode: String) {
        kontakteListView?.displayKontakteListInRecyclerView(kontaktArrayList)
        kontakteListView?.hideProgress()
        if (finishCode != FinishCode.finishedOnWeb) {
            kontakteListView?.onSucess(finishCode)
        }
    }

    override fun onFailure(failureCode: String) {
        kontakteListView?.hideProgress()
        kontakteListView?.onError(failureCode)
    }

    override fun requestFromWS() {
        kontakteListView?.showProgress()
        kontakteListModel.getKontakteList(this)
    }

    override fun onDestroy() {
        this.kontakteListView = null
    }
}