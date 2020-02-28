package at.sysco.erp_connect.konto_list

import at.sysco.erp_connect.pojo.Konto

interface KontoListContract {
    //Datenbeschaffung
    interface Model {
        interface OnFinishedListener {
            fun onfinished(kontoArrayList: List<Konto>, finishCode: String)
            fun onFailure(failureCode: String)
        }

        fun getKontoList(onFinishedListener: OnFinishedListener)
    }

    //Darstellung
    interface View {
        fun showProgress()
        fun hideProgress()

        fun onSucess(finishCode: String)
        fun onError(failureCode: String)
        fun displayKontoListInRecyclerView(kontoList: List<Konto>)
    }

    //Präsentierlogik
    interface Presenter {
        fun requestFromWS()
        fun onDestroy()
    }
}