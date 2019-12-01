package at.sysco.erp_connect.konto_list

import android.util.Log
import androidx.preference.PreferenceManager
import at.sysco.erp_connect.constants.FinishCode
import at.sysco.erp_connect.kontakte_list.KontakteListContract
import at.sysco.erp_connect.model.KontakteListModel
import at.sysco.erp_connect.pojo.Konto
import at.sysco.erp_connect.model.KontoListModel
import at.sysco.erp_connect.pojo.Kontakt

class KontoListPresenter(
    kontoListView: KontoListContract.View,
    private val kontoListModel: KontoListModel,
    private val kontaktListModel: KontakteListModel
) : KontoListContract.Presenter, KontoListContract.Model.OnFinishedListener {
    var kontoListView: KontoListContract.View? = kontoListView

    override fun onfinished(kontoArrayList: List<Konto>, finishCode: String) {
        kontoListView?.displayKontoListInRecyclerView(kontoArrayList)
        if (finishCode != FinishCode.finishedOnWeb) {
            kontoListView?.onSucess(finishCode)
            kontoListView?.hideProgress()
        } else {
            trySaving(kontoArrayList)
        }
    }

    fun trySaving(kontoArrayList: List<Konto>? = null, kontaktArrayListToSave: List<Kontakt>? = null) {
        val message: String
        if (kontoArrayList != null) {
            message = kontoListModel.saveKonto(kontoArrayList)
            if (message == FinishCode.finishedSavingKonto) {
                kontaktListModel.getKontakteList(object : KontakteListContract.Model.OnFinishedListener {
                    override fun onfinished(kontaktArrayList: List<Kontakt>, finishCode: String) {
                        if (finishCode != FinishCode.finishedOnWeb) {
                            kontoListView?.onSucess(finishCode)
                            kontoListView?.hideProgress()
                        } else {
                            trySaving(null, kontaktArrayList)
                        }
                    }

                    override fun onFailure(failureCode: String) {
                        kontoListView?.hideProgress()
                        kontoListView?.onError(message)
                    }
                })
            } else {
                kontoListView?.hideProgress()
                kontoListView?.onError(message)
            }
        } else if (kontaktArrayListToSave != null) {
            message = kontaktListModel.saveKontakte(kontaktArrayListToSave)
            if (message == FinishCode.finishedSavingKontakte) {
                kontoListView?.hideProgress()
                kontoListView?.onSucess(message)
            } else {
                kontoListView?.hideProgress()
                kontoListView?.onError(message)
            }
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