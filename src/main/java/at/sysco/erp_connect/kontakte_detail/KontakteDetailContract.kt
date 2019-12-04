package at.sysco.erp_connect.kontakte_detail

import at.sysco.erp_connect.pojo.Kontakt

interface KontakteDetailContract {
    interface Model {
        interface OnFinishedListener {
            fun onfinished(kontakt: Kontakt, finishCode: String)
            fun onFailure(failureCode: String)
        }

        fun getKontaktDetail(onFinishedListener: OnFinishedListener, kontaktNummer: String)
    }

    interface View {
        fun setTextData(kontakt: Kontakt)
        fun onSucess(finishCode: String)
        fun onError(failureCode: String)
        fun hideProgress()
        fun showProgress()
    }

    interface Presenter {
        fun requestFromWS(kontaktNummer: String)
        fun onDestroy()
    }
}