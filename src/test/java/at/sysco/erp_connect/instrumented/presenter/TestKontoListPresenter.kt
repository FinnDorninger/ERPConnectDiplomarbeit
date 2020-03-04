package at.sysco.erp_connect.instrumented.presenter

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
import org.junit.Assert
import org.junit.Before
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class TestKontoListPresenter {
    lateinit var mView: KontoListContract.View
    lateinit var mModelKonto: KontoListModel
    lateinit var mModelKontakt: KontakteListModel
    lateinit var presenter: KontoListPresenter

    val konto = Konto("1", "Test")
    val listKonto = listOf(konto)

    val kontakt = Kontakt("1", "Test")
    val listKontakt = listOf(kontakt)

    @Before
    fun setUpPresenter() {
        mModelKonto = mock()
        mView = mock()
        presenter = KontoListPresenter(mView, mModelKonto, mModelKontakt)
    }

    @Test
    fun displayLoading() {
        presenter.requestFromWS()
        verify(mView).showProgress()
    }

    @Test
    fun loadDataSucessAndSaveKontoAndKontakt() {
        val finishedCode = FinishCode.finishedSavingKontakte

        doAnswer {
            val callback: KontoListContract.Model.OnFinishedListener = it.getArgument(0)
            callback.onfinished(listKonto, finishedCode)
        }.whenever(mModelKonto).getKontoList(any())

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
    fun loadDataSaveKontakteAndKonto() {
        val finishedCode = FinishCode.finishedSavingKontakte

        doAnswer {
            val callback: KontoListContract.Model.OnFinishedListener = it.getArgument(0)
            callback.onfinished(listKonto, finishedCode)
        }.whenever(mModelKonto).getKontoList(any())

        doAnswer {
            val callback: KontakteListContract.Model.OnFinishedListener = it.getArgument(0)
            callback.onfinished(listKontakt, finishedCode)
        }.whenever(mModelKontakt).getKontakteList(any())

        presenter.requestFromWS()
        verify(mView, never()).onError(any())
        verify(mView).onSucess(finishedCode)
        verify(mView, never()).onSucess(any())
        verify(mView).hideProgress()
        verify(mView).displayKontoListInRecyclerView(listKonto)
    }

    @Test
    fun loadDataSucessFromFile() {
        val finishedCode = FinishCode.finishedOnFile

        doAnswer {
            val callback: KontoListContract.Model.OnFinishedListener = it.getArgument(0)
            callback.onfinished(listKonto, finishedCode)
        }.whenever(mModelKonto).getKontoList(any())

        presenter.requestFromWS()
        verify(mView, never()).onError(finishedCode)
        verify(mView).onSucess(any())
        verify(mView).hideProgress()
        verify(mView).displayKontoListInRecyclerView(listKonto)
    }

    @Test
    fun loadDataFailedFromWeb() {
        val failureCode = FailureCode.NO_DATA

        doAnswer {
            val callback: KontoListContract.Model.OnFinishedListener = it.getArgument(0)
            callback.onFailure(failureCode)
        }.whenever(mModelKonto).getKontoList(any())

        presenter.requestFromWS()
        verify(mView).onError(failureCode)
        verify(mView, never()).onSucess(any())
        verify(mView).hideProgress()
        verify(mView, never()).displayKontoListInRecyclerView(listKonto)
    }

    @Test
    fun loadDataFailedNoConnection() {
        val failureCode = FailureCode.NO_CONNECTION

        doAnswer {
            val callback: KontoListContract.Model.OnFinishedListener = it.getArgument(0)
            callback.onFailure(failureCode)
        }.whenever(mModelKonto).getKontoList(any())

        presenter.requestFromWS()
        verify(mView).onError(failureCode)
        verify(mView, never()).onSucess(any())
        verify(mView).hideProgress()
        verify(mView, never()).displayKontoListInRecyclerView(listKonto)
    }

    @Test
    fun loadDataFailedCorruptFile() {
        val failureCode = FailureCode.ERROR_LOADING_FILE

        doAnswer {
            val callback: KontoListContract.Model.OnFinishedListener = it.getArgument(0)
            callback.onFailure(failureCode)
        }.whenever(mModelKonto).getKontoList(any())

        presenter.requestFromWS()
        verify(mView).onError(failureCode)
        verify(mView, never()).onSucess(any())
        verify(mView).hideProgress()
        verify(mView, never()).displayKontoListInRecyclerView(listKonto)
    }

    @Test
    fun decoupleView() {
        presenter.onDestroy()
        Assert.assertNull(presenter.kontoListView)
    }
}

