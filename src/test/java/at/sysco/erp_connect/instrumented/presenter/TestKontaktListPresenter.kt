package at.sysco.erp_connect.instrumented.presenter

import at.sysco.erp_connect.constants.FailureCode
import at.sysco.erp_connect.constants.FinishCode
import at.sysco.erp_connect.kontakte_list.KontakteListContract
import at.sysco.erp_connect.kontakte_list.KontakteListPresenter
import at.sysco.erp_connect.model.KontakteListModel
import at.sysco.erp_connect.pojo.Kontakt
import com.nhaarman.mockito_kotlin.*
import org.junit.Assert
import org.junit.Before
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class TestKontaktListPresenter {
    lateinit var mView: KontakteListContract.View
    lateinit var mModelKontakt: KontakteListModel
    lateinit var presenter: KontakteListPresenter

    @Before
    fun setUpPresenter() {
        mModelKontakt = mock()
        mView = mock()
        presenter = KontakteListPresenter(mView, mModelKontakt)
    }

    @Test
    fun displayLoading() {
        presenter.requestFromWS()
        verify(mView).showProgress()
    }

    @Test
    fun loadDataSucessFromWebAndSave() {
        val kontakt = Kontakt("1", "Test")
        val listKontakt = listOf(kontakt)
        val finishedCode = FinishCode.finishedOnWeb

        doAnswer {
            val callback: KontakteListContract.Model.OnFinishedListener = it.getArgument(0)
            callback.onfinished(listKontakt, finishedCode)
        }.whenever(mModelKontakt).getKontakteList(any())

        presenter.requestFromWS()
        verify(mView, never()).onError(any())
        verify(mView, never()).onSucess(any())
        verify(mModelKontakt).saveKontakte(listKontakt)
        verify(mView).hideProgress()
        verify(mView).displayKontakteListInRecyclerView(listKontakt)
    }

    @Test
    fun loadDataSucessFromFile() {
        val kontakt = Kontakt("1", "Test")
        val listKontakt = listOf(kontakt)
        val finishedCode = FinishCode.finishedOnFile

        doAnswer {
            val callback: KontakteListContract.Model.OnFinishedListener = it.getArgument(0)
            callback.onfinished(listKontakt, finishedCode)
        }.whenever(mModelKontakt).getKontakteList(any())

        presenter.requestFromWS()
        verify(mView, never()).onError(finishedCode)
        verify(mView).onSucess(any())
        verify(mView).hideProgress()
        verify(mView).displayKontakteListInRecyclerView(listKontakt)
    }

    @Test
    fun loadDataFailedFromWeb() {
        val kontakt = Kontakt("1", "Test")
        val listKontakt = listOf(kontakt)
        val failureCode = FailureCode.NO_DATA

        doAnswer {
            val callback: KontakteListContract.Model.OnFinishedListener = it.getArgument(0)
            callback.onFailure(failureCode)
        }.whenever(mModelKontakt).getKontakteList(any())

        presenter.requestFromWS()
        verify(mView).onError(failureCode)
        verify(mView, never()).onSucess(any())
        verify(mView).hideProgress()
        verify(mView, never()).displayKontakteListInRecyclerView(listKontakt)
    }

    @Test
    fun loadDataFailedNoConnection() {
        val kontakt = Kontakt("1", "Test")
        val listKontakt = listOf(kontakt)
        val failureCode = FailureCode.NO_CONNECTION

        doAnswer {
            val callback: KontakteListContract.Model.OnFinishedListener = it.getArgument(0)
            callback.onFailure(failureCode)
        }.whenever(mModelKontakt).getKontakteList(any())

        presenter.requestFromWS()
        verify(mView).onError(failureCode)
        verify(mView, never()).onSucess(any())
        verify(mView).hideProgress()
        verify(mView, never()).displayKontakteListInRecyclerView(listKontakt)
    }

    @Test
    fun loadDataFailedCorruptFile() {
        val kontakt = Kontakt("1", "Test")
        val listKontakt = listOf(kontakt)
        val failureCode = FailureCode.ERROR_LOADING_FILE

        doAnswer {
            val callback: KontakteListContract.Model.OnFinishedListener = it.getArgument(0)
            callback.onFailure(failureCode)
        }.whenever(mModelKontakt).getKontakteList(any())

        presenter.requestFromWS()
        verify(mView).onError(failureCode)
        verify(mView, never()).onSucess(any())
        verify(mView).hideProgress()
        verify(mView, never()).displayKontakteListInRecyclerView(listKontakt)
    }

    @Test
    fun decoupleView() {
        presenter.onDestroy()
        Assert.assertNull(presenter.kontakteListView)
    }
}
