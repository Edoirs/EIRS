//package ng.gov.eirs.mas.erasmpoa.ui.auth
//
//import android.app.ProgressDialog
//import android.os.Bundle
//import android.support.v7.app.AlertDialog
//import android.text.TextUtils
//import android.view.Menu
//import android.view.MenuItem
//import io.reactivex.android.schedulers.AndroidSchedulers
//import io.reactivex.schedulers.Schedulers
//import ng.gov.eirs.mas.erasmpoa.R
//import ng.gov.eirs.mas.erasmpoa.ui.base.BaseAppCompatActivity
//import ng.gov.eirs.mas.erasmpoa.util.logout
//import java.net.SocketTimeoutException
//
//class ResetPasswordActivity : BaseAppCompatActivity() {
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_reset_password)
//        setUpToolbar(toolbar)
//        setHomeAsUp(false)
//
//        btnReset.setOnClickListener { onClickReset() }
//    }
//
//    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
//        val logoutItem = menu?.add(R.string.logout)
//        logoutItem?.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_NEVER)
//        logoutItem?.setOnMenuItemClickListener {
//            confirmLogout()
//            true
//        }
//        return super.onCreateOptionsMenu(menu)
//    }
//
//    private fun confirmLogout() {
//        val builder = AlertDialog.Builder(activity)
//        builder.setTitle(R.string.logout)
//        builder.setMessage(R.string.msg_logout_confirm)
//        builder.setPositiveButton(R.string.logout) { _, _ -> logout() }
//        builder.setNegativeButton(R.string.cancel, null)
//        builder.show()
//    }
//
//    private fun onClickReset() {
//        tilPassword.error = null
//        tilPasswordConfirm.error = null
//
//        tilPassword.isErrorEnabled = false
//        tilPasswordConfirm.isErrorEnabled = false
//
//        val password = etPassword.text.toString()
//        val passwordConfirm = etPasswordConfirm.text.toString()
//
//        var isOK = true
//
//        if (TextUtils.isEmpty(password)) {
//            isOK = false
//            tilPassword.error = getString(R.string.required_field)
//            etPassword.requestFocus()
//        } else if (password.trim { it <= ' ' } != password) {
//            isOK = false
//            tilPassword.error = getString(R.string.space_error)
//            etPassword.requestFocus()
//        } else if (password.length < 6) {
//            isOK = false
//            tilPassword.error = getString(R.string.password_minimum_length)
//            etPassword.requestFocus()
//        }
//
//        if (password != passwordConfirm) {
//            isOK = false
//            tilPasswordConfirm.error = getString(R.string.password_do_not_match)
//            etPasswordConfirm.requestFocus()
//        }
//
//
//        if (isOK) {
//            resetPassword(password)
//        }
//    }
//
//    private fun resetPassword(password: String) {
//        ApiClient.getClient(activity).resetPassword(getUser()?.employeeId, getUser()?.organizationId, password)
//                .compose<ApiResponse>(bindToLifecycle<ApiResponse>())
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe({ response ->
//                    mProgressDialog?.dismiss()
//                    if (response.success == 1) {
//                        val user: User = getUser()
//                        user.passwordStatus = PasswordStatus.NORMAL
//                        saveUser(user)
//                        navigateSession()
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
//
//}
