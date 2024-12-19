package ng.gov.eirs.mas.erasmpoa.ui.auth

import android.app.ProgressDialog
import android.os.Bundle
import android.text.TextUtils
import com.google.gson.reflect.TypeToken
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_login.*
import ng.gov.eirs.mas.erasmpoa.R
import ng.gov.eirs.mas.erasmpoa.data.GsonProvider
import ng.gov.eirs.mas.erasmpoa.data.model.ApiResponse
import ng.gov.eirs.mas.erasmpoa.data.model.MenuSection
import ng.gov.eirs.mas.erasmpoa.data.model.User
import ng.gov.eirs.mas.erasmpoa.net.ApiClient
import ng.gov.eirs.mas.erasmpoa.net.exception.NoInternetConnectionException
import ng.gov.eirs.mas.erasmpoa.sync.SyncTracker
import ng.gov.eirs.mas.erasmpoa.ui.base.BaseAppCompatActivity
import ng.gov.eirs.mas.erasmpoa.util.*
import java.net.SocketTimeoutException

class LoginActivity : BaseAppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        btnLogin.setOnClickListener { onClickLogin() }
        tvForgotPassword.setOnClickListener { onClickForgotPassword() }

        checkSession()
    }

    private fun onClickForgotPassword() {
//        val intent = Intent(activity, ForgotPasswordActivity::class.java)
//        startActivity(intent)
    }

    private fun checkSession() {
        getUser()?.let { saveSessionAndGoToNextPage(it) }
    }

    private fun onClickLogin() {
        tilEmail.errorNull()
        tilPassword.errorNull()

        val username = etEmail.text.toString().superTrim()
        val password = etPassword.text.toString()

        var isOkay = true

        if (TextUtils.isEmpty(username)) {
            tilEmail.error = getString(R.string.required_field)
            etEmail.requestFocus()
            isOkay = false
        }

        if (TextUtils.isEmpty(password)) {
            tilPassword.error = getString(R.string.required_field)
            etPassword.requestFocus()
            isOkay = false
        } else if (password != password.trim()) {
            tilPassword.error = getString(R.string.space_error)
            etPassword.requestFocus()
            isOkay = false
        }

        if (isOkay) {
            login(username, password)
            hideKeyboard()
        }
    }

    private fun login(username: String, password: String) {
        SyncTracker.addSync(ApiClient.getClient(activity, true).login(username, password)
                .compose<ApiResponse>(bindToLifecycle<ApiResponse>())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ response ->
                    mProgressDialog?.dismiss()
                    if (response.success == 1) {
                        val data = response.response?.data?.asJsonObject

                        val token = data?.get("token")?.asString.orEmpty()
                        val user = GsonProvider.getGson().fromJson(data?.get("employeeDetail"), User::class.java)
                        val menuSection = GsonProvider.getGson().fromJson<Array<MenuSection>>(data?.get("menuSection"), object : TypeToken<Array<MenuSection>>() {}.type)

                        saveToken(token)
                        saveMenuSection(menuSection)
                        saveSessionAndGoToNextPage(user)
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

    private fun saveSessionAndGoToNextPage(user: User) {
        saveUser(user)
        navigateSession()
    }
}
