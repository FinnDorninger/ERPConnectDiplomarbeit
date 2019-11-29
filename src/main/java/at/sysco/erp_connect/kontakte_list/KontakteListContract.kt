package at.sysco.erp_connect.kontakte_list

import at.sysco.erp_connect.pojo.Kontakt

interface KontakteListContract {
    interface Model {
        interface OnFinishedListener {
            fun onfinished(kontaktArrayList: List<Kontakt>, finishCode: String)
            fun onFailure(failureCode: String)
        }

        fun getKontakteList(onFinishedListener: OnFinishedListener)
    }

    interface View {
        fun showProgress()
        fun hideProgress()

        fun onSucess(finishCode: String)
        fun onError(failureCode: String)
        fun displayKontakteListInRecyclerView(kontakteList: List<Kontakt>)
    }

    interface Presenter {
        fun requestFromWS()
        fun onDestroy()
    }
}