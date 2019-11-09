package at.sysco.erp_connect.konto_list

import at.sysco.erp_connect.constants.FinishCode
import at.sysco.erp_connect.pojo.Konto
import at.sysco.erp_connect.model.KontoListModel

class KontoListPresenter(
    kontoListView: KontoListContract.View,
    private val kontoListModel: KontoListModel
) : KontoListContract.Presenter, KontoListContract.Model.OnFinishedListener {
    var kontoListView: KontoListContract.View? = kontoListView

    override fun onfinished(kontoArrayList: List<Konto>, finishCode: String) {
        kontoListView?.displayKontoListInRecyclerView(kontoArrayList)
        kontoListView?.hideProgress()
        if (finishCode != FinishCode.finishedOnWeb) {
            kontoListView?.onSucess(finishCode)
        } else {
            kontoListModel.saveKonto(kontoArrayList, this)
        }
    }

    override fun onFailure(failureCode: String) {
        kontoListView?.hideProgress()
        kontoListView?.onError(failureCode)
    }

    override fun requestFromWS() {
        kontoListView?.showProgress()
        kontoListModel.getKontoList(this)
    }

    override fun onDestroy() {
        this.kontoListView = null
    }
}