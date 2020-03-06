package at.sysco.erp_connect.test.presenter

import at.sysco.erp_connect.constants.FailureCode
import at.sysco.erp_connect.constants.FinishCode
import at.sysco.erp_connect.kontakte_list.KontakteListContract
import at.sysco.erp_connect.konto_list.KontoListContract
import at.sysco.erp_connect.konto_list.KontoListPresenter
import at.sysco.erp_connect.model.KontakteListModel
import at.sysco.erp_connect.model.KontoListModel
import at.sysco.erp_connect.pojo.Kontakt
import at.sysco.erp_connect.pojo.Konto
import com.nhaarman.mockito_kotlin.*
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class TestKontoListPresenter {
    var mView: KontoListContract.View = mock()
    var mModelKonto: KontoListModel = mock()
    var mModelKontakt: KontakteListModel = mock()
    var presenter: KontoListPresenter = KontoListPresenter(mView, mModelKonto, mModelKontakt)

    val konto = Konto("1", "Test")
    val listKonto = listOf(konto)

    val kontakt = Kontakt("1", "Test")
    val listKontakt = listOf(kontakt)

    @Test
    fun showDisplayLoading() {
        presenter.requestFromWS()
        verify(mView).showProgress()
    }

    @Test
    fun loadAndSaveKonten() {
        val finishedCode = FinishCode.finishedSavingKonto
        doAnswer {
            val callback: KontoListContract.Model.OnFinishedListener = it.getArgument(0)
            callback.onfinished(listKonto, FinishCode.finishedOnWeb)
        }.whenever(mModelKonto).getKontoList(any())

        doAnswer {
            false
        }.whenever(mModelKontakt).isAutoSyncActivated()

        doAnswer {
            FinishCode.finishedSavingKonto
        }.whenever(mModelKonto).saveKonto(any())

        presenter.requestFromWS()
        verify(mView, never()).onError(any())
        verify(mView).onSucess(finishedCode)
        verify(mView).hideProgress()
        verify(mView).displayKontoListInRecyclerView(listKonto)
    }

    @Test
    fun loadOnlyOffline() {
        val finishedCode = FinishCode.finishedOnFile
        doAnswer {
            val callback: KontoListContract.Model.OnFinishedListener = it.getArgument(0)
            callback.onfinished(listKonto, FinishCode.finishedOnFile)
        }.whenever(mModelKonto).getKontoList(any())

        doAnswer {
            false
        }.whenever(mModelKontakt).isAutoSyncActivated()

        doAnswer {
            FinishCode.finishedSavingKonto
        }.whenever(mModelKonto).saveKonto(any())

        presenter.requestFromWS()
        verify(mView, never()).onError(any())
        verify(mView).onSucess(finishedCode)
        verify(mView).hideProgress()
        verify(mView).displayKontoListInRecyclerView(listKonto)
    }

    @Test
    fun failedSaving() {
        val failureCode = FailureCode.ERROR_SAVING_FILE
        doAnswer {
            val callback: KontoListContract.Model.OnFinishedListener = it.getArgument(0)
            callback.onfinished(listKonto, FinishCode.finishedOnWeb)
        }.whenever(mModelKonto).getKontoList(any())

        doAnswer {
            false
        }.whenever(mModelKontakt).isAutoSyncActivated()

        doAnswer {
            FailureCode.ERROR_SAVING_FILE
        }.whenever(mModelKonto).saveKonto(any())

        presenter.requestFromWS()
        verify(mView).onError(failureCode)
        verify(mView, never()).onSucess(any())
        verify(mView).hideProgress()
        verify(mView).displayKontoListInRecyclerView(listKonto)
    }

    @Test
    fun failedLoading() {
        val failureCode = FailureCode.NOT_SAVED
        doAnswer {
            val callback: KontoListContract.Model.OnFinishedListener = it.getArgument(0)
            callback.onFailure(FailureCode.NOT_SAVED)
        }.whenever(mModelKonto).getKontoList(any())

        doAnswer {
            false
        }.whenever(mModelKontakt).isAutoSyncActivated()

        presenter.requestFromWS()
        verify(mView).onError(failureCode)
        verify(mView, never()).onSucess(any())
        verify(mView).hideProgress()
        verify(mView, never()).displayKontoListInRecyclerView(any())
    }

    @Test
    fun failedNoConnectionNoFiles() {
        val failureCode = FailureCode.NO_CONNECTION
        doAnswer {
            val callback: KontoListContract.Model.OnFinishedListener = it.getArgument(0)
            callback.onFailure(FailureCode.NO_CONNECTION)
        }.whenever(mModelKonto).getKontoList(any())

        doAnswer {
            false
        }.whenever(mModelKontakt).isAutoSyncActivated()

        presenter.requestFromWS()
        verify(mView).onError(failureCode)
        verify(mView, never()).onSucess(any())
        verify(mView).hideProgress()
        verify(mView, never()).displayKontoListInRecyclerView(listKonto)
    }


    @Test
    fun autoSyncSucess() {
        val finishedCode = FinishCode.finishedSavingKontakte
        doAnswer {
            val callback: KontoListContract.Model.OnFinishedListener = it.getArgument(0)
            callback.onfinished(listKonto, finishedCode)
        }.whenever(mModelKonto).getKontoList(any())

        doAnswer {
            FinishCode.finishedSavingKonto
        }.whenever(mModelKonto).saveKonto(any())

        doAnswer {
            true
        }.whenever(mModelKontakt).isAutoSyncActivated()

        doAnswer {
            val callback: KontakteListContract.Model.OnFinishedListener = it.getArgument(0)
            callback.onfinished(listKontakt, finishedCode)
        }.whenever(mModelKontakt).getKontakteList(any())

        presenter.requestFromWS()
        verify(mView, never()).onError(any())
        verify(mView).onSucess(finishedCode)
        verify(mView).hideProgress()
        verify(mView).displayKontoListInRecyclerView(listKonto)
    }

    @Test
    fun autoSyncKontakteFromFile() {
        val finishedCode = FinishCode.finishedOnFileAutosyncKontakt
        doAnswer {
            val callback: KontoListContract.Model.OnFinishedListener = it.getArgument(0)
            callback.onfinished(listKonto, FinishCode.finishedOnWeb)
        }.whenever(mModelKonto).getKontoList(any())

        doAnswer {
            FinishCode.finishedSavingKonto
        }.whenever(mModelKonto).saveKonto(any())

        doAnswer {
            true
        }.whenever(mModelKontakt).isAutoSyncActivated()

        doAnswer {
            val callback: KontakteListContract.Model.OnFinishedListener = it.getArgument(0)
            callback.onfinished(listKontakt, FinishCode.finishedOnFile)
        }.whenever(mModelKontakt).getKontakteList(any())

        presenter.requestFromWS()
        verify(mView, never()).onError(any())
        verify(mView).onSucess(finishedCode)
        verify(mView).hideProgress()
        verify(mView).displayKontoListInRecyclerView(listKonto)
    }


    @Test
    fun autoSyncKontakteNotLoadedFromFile() {
        val finishedCode = FinishCode.finishedOnFileAutosyncKontakt

        doAnswer {
            val callback: KontoListContract.Model.OnFinishedListener = it.getArgument(0)
            callback.onfinished(listKonto, FinishCode.finishedOnWeb)
        }.whenever(mModelKonto).getKontoList(any())

        doAnswer {
            FinishCode.finishedSavingKonto
        }.whenever(mModelKonto).saveKonto(any())

        doAnswer {
            true
        }.whenever(mModelKontakt).isAutoSyncActivated()

        doAnswer {
            val callback: KontakteListContract.Model.OnFinishedListener = it.getArgument(0)
            callback.onFailure(FailureCode.NO_DATA)
        }.whenever(mModelKontakt).getKontakteList(any())

        presenter.requestFromWS()
        verify(mView).onSucess(finishedCode)
        verify(mView, never()).onError(any())
        verify(mView).hideProgress()
        verify(mView).displayKontoListInRecyclerView(listKonto)
    }

    @Test
    fun autoSyncKontakteNoConnection() {
        val finishedCode = FinishCode.finishedOnFileAutosyncKontakt

        doAnswer {
            val callback: KontoListContract.Model.OnFinishedListener = it.getArgument(0)
            callback.onfinished(listKonto, FinishCode.finishedOnWeb)
        }.whenever(mModelKonto).getKontoList(any())

        doAnswer {
            FinishCode.finishedSavingKonto
        }.whenever(mModelKonto).saveKonto(any())

        doAnswer {
            true
        }.whenever(mModelKontakt).isAutoSyncActivated()

        doAnswer {
            val callback: KontakteListContract.Model.OnFinishedListener = it.getArgument(0)
            callback.onFailure(FailureCode.NO_CONNECTION)
        }.whenever(mModelKontakt).getKontakteList(any())

        presenter.requestFromWS()
        verify(mView).onSucess(finishedCode)
        verify(mView, never()).onError(any())
        verify(mView).hideProgress()
        verify(mView).displayKontoListInRecyclerView(listKonto)
    }

    @Test
    fun autoSyncKontakteErrorSaving() {
        val finishedCode = FinishCode.finishedOnFileAutosyncKontakt

        doAnswer {
            val callback: KontoListContract.Model.OnFinishedListener = it.getArgument(0)
            callback.onfinished(listKonto, FinishCode.finishedOnWeb)
        }.whenever(mModelKonto).getKontoList(any())

        doAnswer {
            true
        }.whenever(mModelKontakt).isAutoSyncActivated()

        doAnswer {
            val callback: KontakteListContract.Model.OnFinishedListener = it.getArgument(0)
            callback.onfinished(listKontakt, FinishCode.finishedOnWeb)
        }.whenever(mModelKontakt).getKontakteList(any())

        doAnswer {
            FinishCode.finishedSavingKonto
        }.whenever(mModelKonto).saveKonto(any())

        doAnswer {
            FailureCode.ERROR_SAVING_FILE
        }.whenever(mModelKontakt).saveKontakte(any())

        presenter.requestFromWS()
        verify(mView).onSucess(finishedCode)
        verify(mView, never()).onError(any())
        verify(mView).hideProgress()
        verify(mView).displayKontoListInRecyclerView(listKonto)
    }

//================

    @Test
    fun autoSyncKontoOffline() {
        val finishedCode = FinishCode.finishedOnFile
        doAnswer {
            val callback: KontoListContract.Model.OnFinishedListener = it.getArgument(0)
            callback.onfinished(listKonto, FinishCode.finishedOnFile)
        }.whenever(mModelKonto).getKontoList(any())

        doAnswer {
            false
        }.whenever(mModelKontakt).isAutoSyncActivated()

        doAnswer {
            FinishCode.finishedSavingKonto
        }.whenever(mModelKonto).saveKonto(any())

        presenter.requestFromWS()
        verify(mView, never()).onError(any())
        verify(mView).onSucess(finishedCode)
        verify(mView).hideProgress()
        verify(mView).displayKontoListInRecyclerView(listKonto)
    }

    @Test
    fun autoSyncFailedSaving() {
        val failureCode = FailureCode.ERROR_SAVING_FILE
        doAnswer {
            val callback: KontoListContract.Model.OnFinishedListener = it.getArgument(0)
            callback.onfinished(listKonto, FinishCode.finishedOnWeb)
        }.whenever(mModelKonto).getKontoList(any())

        doAnswer {
            true
        }.whenever(mModelKontakt).isAutoSyncActivated()

        doAnswer {
            FailureCode.ERROR_SAVING_FILE
        }.whenever(mModelKonto).saveKonto(any())

        doAnswer {
            val callback: KontakteListContract.Model.OnFinishedListener = it.getArgument(0)
            callback.onfinished(listKontakt, FinishCode.finishedOnWeb)
        }.whenever(mModelKontakt).getKontakteList(any())

        presenter.requestFromWS()
        verify(mView).onError(failureCode)
        verify(mView, never()).onSucess(any())
        verify(mView).hideProgress()
        verify(mView).displayKontoListInRecyclerView(listKonto)
    }

    @Test
    fun autoSyncFailedLoading() {
        val failureCode = FailureCode.NOT_SAVED
        doAnswer {
            val callback: KontoListContract.Model.OnFinishedListener = it.getArgument(0)
            callback.onFailure(FailureCode.NOT_SAVED)
        }.whenever(mModelKonto).getKontoList(any())

        doAnswer {
            true
        }.whenever(mModelKontakt).isAutoSyncActivated()

        doAnswer {
            val callback: KontakteListContract.Model.OnFinishedListener = it.getArgument(0)
            callback.onfinished(listKontakt, FinishCode.finishedOnWeb)
        }.whenever(mModelKontakt).getKontakteList(any())

        presenter.requestFromWS()
        verify(mView).onError(failureCode)
        verify(mView, never()).onSucess(any())
        verify(mView).hideProgress()
        verify(mView, never()).displayKontoListInRecyclerView(any())
    }

    @Test
    fun autoSyncKontoFailedNoConnectionNoFiles() {
        val failureCode = FailureCode.NO_CONNECTION
        doAnswer {
            val callback: KontoListContract.Model.OnFinishedListener = it.getArgument(0)
            callback.onFailure(FailureCode.NO_CONNECTION)
        }.whenever(mModelKonto).getKontoList(any())

        doAnswer {
            true
        }.whenever(mModelKontakt).isAutoSyncActivated()

        doAnswer {
            val callback: KontakteListContract.Model.OnFinishedListener = it.getArgument(0)
            callback.onfinished(listKontakt, FinishCode.finishedOnWeb)
        }.whenever(mModelKontakt).getKontakteList(any())

        presenter.requestFromWS()
        verify(mView).onError(failureCode)
        verify(mView, never()).onSucess(any())
        verify(mView).hideProgress()
        verify(mView, never()).displayKontoListInRecyclerView(listKonto)
    }
}

