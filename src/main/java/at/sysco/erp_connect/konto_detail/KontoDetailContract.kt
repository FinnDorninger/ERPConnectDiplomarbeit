package at.sysco.erp_connect.konto_detail

import at.sysco.erp_connect.pojo.Konto

interface KontoDetailContract {
    interface Model {
        interface OnFinishedListener {
            fun onfinished(konto: Konto)
            fun onFailureFileLoad(failureCode: String)
        }

        fun getKontoDetail(onFinishedListener: OnFinishedListener, kontoNummer: String)
    }

    interface View {
        fun showProgress()
        fun hideProgress()
        fun setTextData(konto: Konto)
    }

    interface Presenter {
        fun requestFromWS(kontoNummer: String)
        fun onDestroy()
    }
}