package at.sysco.erp_connect.presenter

import at.sysco.erp_connect.constants.FailureCode
import at.sysco.erp_connect.konto_detail.KontoDetailContract
import at.sysco.erp_connect.konto_detail.KontoDetailPresenter
import at.sysco.erp_connect.model.KontoDetailModel
import at.sysco.erp_connect.pojo.Konto
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class TestKontoDetailPresenter {
    @Mock
    lateinit var mView: KontoDetailContract.View
    @Mock
    lateinit var mModelKonto: KontoDetailModel

    lateinit var mPresenter: KontoDetailPresenter

    @Before
    fun setUpPresenter() {
        MockitoAnnotations.initMocks(this)
        mPresenter = KontoDetailPresenter(mView, mModelKonto)
    }

    @Test
    fun getData() {
        mPresenter.requestFromWS("1")

    }
}

