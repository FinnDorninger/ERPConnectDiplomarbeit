package at.sysco.erp_connect.konto_detail

import at.sysco.erp_connect.pojo.Konto

interface KontoDetailContract {
    //Datenbeschaffung
    interface Model {
        interface OnFinishedListener {
            fun onfinished(konto: Konto, finishCode: String)
            fun onFailure(failureCode: String)
        }

        fun getKontoDetail(onFinishedListener: OnFinishedListener, kontoNummer: String)
    }

    //Darstellung
    interface View {
        fun setTextData(konto: Konto)
        fun onSucess(finishCode: String)
        fun onError(failureCode: String)
        fun hideProgress()
        fun showProgress()
        fun initListener(konto: Konto)
        fun startPresenterRequest(kontoNummer: String)
    }

    //Presentierlogik
    interface Presenter {
        fun requestFromWS(kontoNummer: String)
        fun onDestroy()
    }
}