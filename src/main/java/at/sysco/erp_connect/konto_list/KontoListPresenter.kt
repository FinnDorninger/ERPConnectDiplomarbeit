package at.sysco.erp_connect.konto_list

import at.sysco.erp_connect.constants.FailureCode
import at.sysco.erp_connect.pojo.Konto
import at.sysco.erp_connect.model.KontoListModel

class KontoListPresenter(
    kontoListView: KontoListContract.View,
    val kontoListModel: KontoListModel
) : KontoListContract.Presenter, KontoListContract.Model.OnFinishedListener {
    var kontoListView: KontoListContract.View? = kontoListView

    override fun onfinished(kontoArrayList: List<Konto>) {
        kontoListView?.displayKontoListInRecyclerView(kontoArrayList)
        kontoListView?.hideProgress()
    }

    override fun onFailure(failureCode: String) {
        kontoListView?.hideProgress()
        when (failureCode) {
            FailureCode.FAILED_CONNECTION -> kontoListView?.showLoadingError()
        }
    }

    override fun requestFromWS() {
        kontoListView?.showProgress()
        kontoListModel.getKontoList(this)
    }

    override fun onDestroy() {
        this.kontoListView = null
    }
}