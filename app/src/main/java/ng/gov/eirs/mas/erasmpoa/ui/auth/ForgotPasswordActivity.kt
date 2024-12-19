//package ng.gov.eirs.mas.erasmpoa.ui.auth
//
//import android.app.AlertDialog
//import android.app.ProgressDialog
//import android.content.Intent
//import android.os.Bundle
//import android.text.TextUtils
//import io.reactivex.android.schedulers.AndroidSchedulers
//import io.reactivex.schedulers.Schedulers
//import ng.gov.eirs.mas.erasmpoa.R
//import ng.gov.eirs.mas.erasmpoa.ui.base.BaseAppCompatActivity
//import java.net.SocketTimeoutException
//
//class ForgotPasswordActivity : BaseAppCompatActivity() {
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_forgot_password)
//
//        setUpToolbar(toolbar)
//        setHomeAsUp(true)
//
//        btnRecover.setOnClickListener { onClickRecover() }
//    }
//
//    private fun onClickRecover() {
//        tilPhone.error = null
//        tilPhone.isErrorEnabled = false
//
//        val phone = etPhone.text.toString()
//
//        var isOK = true
//
//        if (TextUtils.isEmpty(phone) || phone.length != 10) {
//            tilPhone.error = getString(R.string.enter_valid_phone)
//            etPhone.requestFocus()
//            isOK = false
//        }
//
//        if (isOK) {
//            recover(phone)
//        }
//    }
//
//    private fun recover(phone: String) {
//        ApiClient.getClient(activity).recover(phone)
//                .compose<ApiResponse>(bindToLifecycle<ApiResponse>())
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe({ response ->
//                    mProgressDialog?.dismiss()
//                    if (response.success == 1) {
//
//                        val builder = AlertDialog.Builder(activity)
//                        builder.setTitle("SMS Sent")
//                        builder.setMessage("Message containing username and temporary password has been sent to your number $phone. please use temporary password to sign in.")
//                        builder.setPositiveButton(R.string.okay) { _, _ ->
//                            val intent = Intent(this@ForgotPasswordActivity, LoginActivity::class.java)
//                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
//                            startActivity(intent)
//                        }
//                        builder.show()
//                    } else {
//                        val messageU = response.response.message
//                        if (messageU.isJsonObject) {
//                            snackBar(coordinatorLayout, R.string.please_try_again)
//                        } else if (messageU.isJsonPrimitive) {
//                            val message = messageU.asString
//                            snackBar(coordinatorLayout, message)
//                        } else {
//                            snackBar(coordinatorLayout, R.string.please_try_again)
//                        }
//                    }
//                }, { error ->
//                    mProgressDialog?.dismiss()
//                    if (error is SocketTimeoutException || error is NoInternetConnectionException) {
//                        snackBar(coordinatorLayout, R.string.connection_error)
//                    } else {
//                        snackBar(coordinatorLayout, R.string.please_try_again)
//                    }
//                    error.printStackTrace()
//                })
//        mProgressDialog = ProgressDialog.show(activity, null, getString(R.string.loading))
//    }
//}
