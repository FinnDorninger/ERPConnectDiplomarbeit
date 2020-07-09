package at.sysco.erp_connect.test.presenter

import at.sysco.erp_connect.constants.FailureCode
import at.sysco.erp_connect.constants.FinishCode
import at.sysco.erp_connect.kontakte_detail.KontakteDetailContract
import at.sysco.erp_connect.kontakte_detail.KontakteDetailPresenter
import at.sysco.erp_connect.model.KontakteDetailModel
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
class TestKontaktDetailPresenter {
    lateinit var mView: KontakteDetailContract.View
    lateinit var mModelKontakt: KontakteDetailModel
    lateinit var presenter: KontakteDetailPresenter

    @Before
    fun setUpPresenter() {
        mModelKontakt = mock()
        mView = mock()
        presenter = KontakteDetailPresenter(mView, mModelKontakt)
    }

    @Test
    fun displayLoading() {
        presenter.requestFromWS("1")
        verify(mView).showProgress()
    }

    @Test
    fun loadDataSucessFromWeb() {
        val kontakt = Kontakt("1", "Test")
        val finishedCode = FinishCode.finishedOnWeb

        doAnswer {
            val callback: KontakteDetailContract.Model.OnFinishedListener = it.getArgument(0)
            callback.onfinished(kontakt, finishedCode)
        }.whenever(mModelKontakt).getKontaktDetail(any(), eq("1"))

        presenter.requestFromWS("1")
        verify(mView, never()).onError(any())
        verify(mView, never()).onSucess(any())
        verify(mView).hideProgress()
        verify(mView).setTextData(kontakt)
        verify(mView).initListener(kontakt)
    }

    @Test
    fun loadDataSucessFromFile() {
        val kontakt = Kontakt("1", "Test")
        val finishedCode = FinishCode.finishedOnFile

        doAnswer {
            val callback: KontakteDetailContract.Model.OnFinishedListener = it.getArgument(0)
            callback.onfinished(kontakt, finishedCode)
        }.whenever(mModelKontakt).getKontaktDetail(any(), eq("1"))

        presenter.requestFromWS("1")
        verify(mView, never()).onError(finishedCode)
        verify(mView).onSucess(any())
        verify(mView).hideProgress()
        verify(mView).setTextData(kontakt)
        verify(mView).initListener(kontakt)
    }

    @Test
    fun loadDataFailedFromWeb() {
        val kontakt = Kontakt("1", "Test")
        val failureCode = FailureCode.NO_DATA

        doAnswer {
            val callback: KontakteDetailContract.Model.OnFinishedListener = it.getArgument(0)
            callback.onFailure(failureCode)
        }.whenever(mModelKontakt).getKontaktDetail(any(), eq("1"))

        presenter.requestFromWS("1")
        verify(mView).onError(failureCode)
        verify(mView, never()).onSucess(any())
        verify(mView).hideProgress()
        verify(mView, never()).setTextData(kontakt)
    }

    @Test
    fun loadDataFailedNoConnection() {
        val kontakt = Kontakt("1", "Test")
        val failureCode = FailureCode.NO_CONNECTION

        doAnswer {
            val callback: KontakteDetailContract.Model.OnFinishedListener = it.getArgument(0)
            callback.onFailure(failureCode)
        }.whenever(mModelKontakt).getKontaktDetail(any(), eq("1"))

        presenter.requestFromWS("1")
        verify(mView).onError(failureCode)
        verify(mView, never()).onSucess(any())
        verify(mView).hideProgress()
        verify(mView, never()).setTextData(kontakt)
    }

    @Test
    fun loadDataFailedCorruptFile() {
        val kontakt = Kontakt("1", "Test")
        val failureCode = FailureCode.ERROR_LOADING_FILE

        doAnswer {
            val callback: KontakteDetailContract.Model.OnFinishedListener = it.getArgument(0)
            callback.onFailure(failureCode)
        }.whenever(mModelKontakt).getKontaktDetail(any(), eq("1"))

        presenter.requestFromWS("1")
        verify(mView).onError(failureCode)
        verify(mView, never()).onSucess(any())
        verify(mView).hideProgress()
        verify(mView, never()).setTextData(kontakt)
    }

    @Test
    fun decoupleView() {
        presenter.onDestroy()
        Assert.assertNull(presenter.kontaktDetailView)
    }
}

