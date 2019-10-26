package at.sysco.erp_connect.konto_detail

import at.sysco.erp_connect.pojo.Konto

interface KontoDetailContract {
    interface Model {
        interface OnFinishedListener {
            fun onfinished(konto: Konto, finishCode: String)
            fun onFailure(failureCode: String)
        }

        fun getKontoDetail(onFinishedListener: OnFinishedListener, kontoNummer: String)
    }

    interface View {
        fun setTextData(konto: Konto)
        fun onSucess(finishCode: String)
        fun onError(failureCode: String)
    }

    interface Presenter {
        fun requestFromWS(kontoNummer: String)
        fun onDestroy()
    }
}