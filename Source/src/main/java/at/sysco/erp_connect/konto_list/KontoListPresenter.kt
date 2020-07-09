package at.sysco.erp_connect.konto_list

import at.sysco.erp_connect.constants.FinishCode
import at.sysco.erp_connect.kontakte_list.KontakteListContract
import at.sysco.erp_connect.model.KontakteListModel
import at.sysco.erp_connect.pojo.Konto
import at.sysco.erp_connect.model.KontoListModel
import at.sysco.erp_connect.pojo.Kontakt

//Beeinhaltet Präsentierlogik zwischen Model und View.
class KontoListPresenter(
    kontoListView: KontoListContract.View,
    private val kontoListModel: KontoListModel,
    private val kontaktListModel: KontakteListModel
) : KontoListContract.Presenter {
    var kontoListView: KontoListContract.View? = kontoListView

    //Versucht die Daten zu laden. Und gegebenenfalls auch noch, wenn im Einstellungsmneü "Synchronisieren von Kontakte"
    //ausgewählt wurde, werden diese ebenfalls noch geladen.
    fun trySaving(kontoArrayList: List<Konto>? = null, kontaktArrayListToSave: List<Kontakt>? = null) {
        val message: String
        if (kontoArrayList != null) {
            //Ruft Model zur Kontenspeicherung auf.
            message = kontoListModel.saveKonto(kontoArrayList)
            if (message == FinishCode.finishedSavingKonto && kontaktListModel.isAutoSyncActivated()) {
                //Startet bei bestehender Einstellung "Synchronisieren von Kontakte" auch das Kontakte-Model zum diese zu laden.
                kontaktListModel.getKontakteList(object : KontakteListContract.Model.OnFinishedListener {
                    override fun onfinished(kontaktArrayList: List<Kontakt>, finishCode: String) {
                        if (finishCode != FinishCode.finishedOnWeb) {
                            kontoListView?.onSucess(FinishCode.finishedOnFileAutosyncKontakt)
                            kontoListView?.hideProgress()
                        } else {
                            //Ruft die gleiche Methode wieder auf
                            trySaving(null, kontaktArrayList)
                        }
                    }

                    override fun onFailure(failureCode: String) {
                        kontoListView?.hideProgress()
                        kontoListView?.onSucess(FinishCode.finishedOnFileAutosyncKontakt)
                    }
                })
            } else if (message == FinishCode.finishedSavingKonto) {
                kontoListView?.onSucess(message)
                kontoListView?.hideProgress()
            } else {
                kontoListView?.hideProgress()
                kontoListView?.onError(message)
            }
        } else if (kontaktArrayListToSave != null) {
            //Speichern der Kontakte
            message = kontaktListModel.saveKontakte(kontaktArrayListToSave)
            if (message == FinishCode.finishedSavingKontakte) {
                kontoListView?.hideProgress()
                kontoListView?.onSucess(message)
            } else {
                kontoListView?.hideProgress()
                kontoListView?.onSucess(FinishCode.finishedOnFileAutosyncKontakt)
            }
        }
    }

    //Beauftragt Model mit der Datenbeschaffung
    override fun requestFromWS() {
        kontoListView?.showProgress()
        kontoListModel.getKontoList(object : KontoListContract.Model.OnFinishedListener {
            //Wird nach erfolgreicher Datenbeschaffung des Models geladen.
            override fun onfinished(kontoArrayList: List<Konto>, finishCode: String) {
                kontoListView?.displayKontoListInRecyclerView(kontoArrayList)
                if (finishCode != FinishCode.finishedOnWeb) {
                    kontoListView?.onSucess(finishCode)
                    kontoListView?.hideProgress()
                } else {
                    trySaving(kontoArrayList)
                }
            }

            //Methode welche bei Fehlern in der Datenbeschaffung aufgerufen wird.
            override fun onFailure(failureCode: String) {
                kontoListView?.hideProgress()
                kontoListView?.onError(failureCode)
            }
        })
    }

    //Setzt View null, damit keine Referenz mehr zur Activity besteht
    override fun onDestroy() {
        this.kontoListView = null
    }
}