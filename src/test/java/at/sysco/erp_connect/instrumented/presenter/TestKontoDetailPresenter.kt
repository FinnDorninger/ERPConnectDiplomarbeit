package at.sysco.erp_connect.instrumented.presenter

import at.sysco.erp_connect.constants.FailureCode
import at.sysco.erp_connect.constants.FinishCode
import at.sysco.erp_connect.konto_detail.KontoDetailContract
import at.sysco.erp_connect.konto_detail.KontoDetailPresenter
import at.sysco.erp_connect.model.KontoDetailModel
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
class TestKontoDetailPresenter {
    lateinit var mView: KontoDetailContract.View
    lateinit var mModelKonto: KontoDetailModel
    lateinit var mPresenter: KontoDetailPresenter

    @Before
    fun setUpPresenter() {
        mModelKonto = mock()
        mView = mock()
        mPresenter = KontoDetailPresenter(mView, mModelKonto)
    }

    @Test
    fun displayLoading() {
        mPresenter.requestFromWS("1")
        verify(mView).showProgress()
    }

    @Test
    fun loadDataSucessFromWeb() {
        val konto = Konto("1", "Test")
        val finishedCode = FinishCode.finishedOnWeb

        doAnswer {
            val callback: KontoDetailContract.Model.OnFinishedListener = it.getArgument(0)
            callback.onfinished(konto, finishedCode)
        }.whenever(mModelKonto).getKontoDetail(any(), eq("1"))

        mPresenter.requestFromWS("1")
        verify(mView, never()).onError(any())
        verify(mView, never()).onSucess(any())
        verify(mView).initListener(konto)
        verify(mView).hideProgress()
        verify(mView).setTextData(konto)
    }

    @Test
    fun loadDataSucessFromFile() {
        val konto = Konto("1", "Test")
        val finishedCode = FinishCode.finishedOnFile

        doAnswer {
            val callback: KontoDetailContract.Model.OnFinishedListener = it.getArgument(0)
            callback.onfinished(konto, finishedCode)
        }.whenever(mModelKonto).getKontoDetail(any(), eq("1"))

        mPresenter.requestFromWS("1")
        verify(mView, never()).onError(finishedCode)
        verify(mView).onSucess(any())
        verify(mView).initListener(konto)
        verify(mView).hideProgress()
        verify(mView).setTextData(konto)
    }

    @Test
    fun loadDataFailedFromWeb() {
        val konto = Konto("1", "Test")
        val failureCode = FailureCode.NO_DATA

        doAnswer {
            val callback: KontoDetailContract.Model.OnFinishedListener = it.getArgument(0)
            callback.onFailure(failureCode)
        }.whenever(mModelKonto).getKontoDetail(any(), eq("1"))

        mPresenter.requestFromWS("1")
        verify(mView).onError(failureCode)
        verify(mView, never()).onSucess(any())
        verify(mView).hideProgress()
        verify(mView, never()).setTextData(konto)
    }

    @Test
    fun loadDataFailedNoConnection() {
        val konto = Konto("1", "Test")
        val failureCode = FailureCode.NO_CONNECTION

        doAnswer {
            val callback: KontoDetailContract.Model.OnFinishedListener = it.getArgument(0)
            callback.onFailure(failureCode)
        }.whenever(mModelKonto).getKontoDetail(any(), eq("1"))

        mPresenter.requestFromWS("1")
        verify(mView).onError(failureCode)
        verify(mView, never()).onSucess(any())
        verify(mView).hideProgress()
        verify(mView, never()).setTextData(konto)
    }

    @Test
    fun loadDataFailedCorruptFile() {
        val konto = Konto("1", "Test")
        val failureCode = FailureCode.ERROR_LOADING_FILE

        doAnswer {
            val callback: KontoDetailContract.Model.OnFinishedListener = it.getArgument(0)
            callback.onFailure(failureCode)
        }.whenever(mModelKonto).getKontoDetail(any(), eq("1"))

        mPresenter.requestFromWS("1")
        verify(mView).onError(failureCode)
        verify(mView, never()).onSucess(any())
        verify(mView).hideProgress()
        verify(mView, never()).setTextData(konto)
    }

    @Test
    fun decoupleView() {
        mPresenter.onDestroy()
        Assert.assertNull(mPresenter.kontoDetailView)
    }
}

