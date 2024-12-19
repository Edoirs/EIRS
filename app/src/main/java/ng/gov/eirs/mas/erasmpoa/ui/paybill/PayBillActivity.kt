package ng.gov.eirs.mas.erasmpoa.ui.paybill

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_pay_bill.*
import ng.gov.eirs.mas.erasmpoa.R
import ng.gov.eirs.mas.erasmpoa.data.GsonProvider
import ng.gov.eirs.mas.erasmpoa.data.model.ApiResponse
import ng.gov.eirs.mas.erasmpoa.data.model.Bill
import ng.gov.eirs.mas.erasmpoa.net.ApiClient
import ng.gov.eirs.mas.erasmpoa.net.exception.NoInternetConnectionException
import ng.gov.eirs.mas.erasmpoa.sync.SyncTracker
import ng.gov.eirs.mas.erasmpoa.ui.base.BaseAppCompatActivity
import ng.gov.eirs.mas.erasmpoa.util.errorNull
import ng.gov.eirs.mas.erasmpoa.util.getToken
import ng.gov.eirs.mas.erasmpoa.util.hideKeyboard
import ng.gov.eirs.mas.erasmpoa.util.snackBar
import java.net.SocketTimeoutException

class PayBillActivity : BaseAppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pay_bill)

        setUpToolbar(toolbar)
        setHomeAsUp(true)

        btnSubmit.setOnClickListener { onClickSubmit() }
    }

    private fun onClickSubmit() {
        tilBillRefNo.errorNull()

        val billRefNo = etBillRefNo.text.toString()

        var isOkay = true

        if (billRefNo.isEmpty()) {
            tilBillRefNo.error = getString(R.string.required_field)
            etBillRefNo.requestFocus()
            isOkay = false
        }

        if (isOkay) {
            hideKeyboard()
            searchBill(billRefNo)
        }
    }

    private fun searchBill(billRef: String) {
        SyncTracker.addSync(if (billRef.startsWith("SB", ignoreCase = true)) {
            ApiClient.getClient(activity, true).serviceBillDetailByRef(getToken(), billRef)
        } else {
            ApiClient.getClient(activity, true).assessmentDetailByRef(getToken(), billRef)
        }.compose<ApiResponse>(bindToLifecycle<ApiResponse>())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ response ->
                    mProgressDialog?.dismiss()
                    if (response.success == 1) {
                        val data = response.response?.data?.asJsonObject
                        if (data?.get("Success")?.asBoolean == true) {
                            val bill = GsonProvider.getGson().fromJson(data?.get("Result"), Bill::class.java)

                            val intent = Intent(activity, BillDetailActivity::class.java)
                            intent.putExtra("bill", bill)
                            startActivity(intent)
                        } else {
                            data?.get("Result")?.let {
                                if (it.isJsonPrimitive) {
                                    coordinatorLayout.snackBar(it.asString)
                                } else {
                                    coordinatorLayout.snackBar(R.string.please_try_again)
                                }
                            }
                        }
                    } else {
                        response.response?.message?.let {
                            when {
                                it.isJsonObject -> coordinatorLayout.snackBar(R.string.please_try_again)
                                it.isJsonPrimitive -> coordinatorLayout.snackBar(it.asString)
                                else -> coordinatorLayout.snackBar(R.string.please_try_again)
                            }
                        } ?: kotlin.run { coordinatorLayout.snackBar(R.string.please_try_again) }
                    }
                }, {
                    mProgressDialog?.dismiss()
                    if (it is SocketTimeoutException || it is NoInternetConnectionException) {
                        coordinatorLayout.snackBar(R.string.connection_error)
                    } else {
                        coordinatorLayout.snackBar(R.string.please_try_again)
                    }
                    it.printStackTrace()
                }))
        mProgressDialog = ProgressDialog.show(activity, null, getString(R.string.loading))
    }
}