package at.sysco.erp_connect.konto_list

import at.sysco.erp_connect.pojo.Konto

interface KontoListContract {
    interface Model {
        interface OnFinishedListener {
            fun onfinished(kontoArrayList: List<Konto>)
            fun onFailure(failureCode: String)
        }
        fun getKontoList(onFinishedListener: OnFinishedListener)
    }

    interface View {
        fun showLoadingError()
        fun showProgress()
        fun hideProgress()
        fun displayKontoListInRecyclerView(kontoList: List<Konto>)
    }

    interface Presenter {
        fun requestFromWS()
        fun onDestroy()
    }
}