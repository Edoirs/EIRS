package ng.gov.eirs.mas.erasmpoa.ui.home

import android.app.ProgressDialog
import android.os.Bundle
import com.raizlabs.android.dbflow.annotation.Collate
import com.raizlabs.android.dbflow.sql.language.SQLite
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_target.*
import ng.gov.eirs.mas.erasmpoa.R
import ng.gov.eirs.mas.erasmpoa.data.dao.Submission
import ng.gov.eirs.mas.erasmpoa.data.dao.Submission_Table
import ng.gov.eirs.mas.erasmpoa.data.model.ApiResponse
import ng.gov.eirs.mas.erasmpoa.data.model.CollectionTarget
import ng.gov.eirs.mas.erasmpoa.data.model.getRemainingAmount
import ng.gov.eirs.mas.erasmpoa.data.model.getTargetAchievePercentage
import ng.gov.eirs.mas.erasmpoa.net.ApiClient
import ng.gov.eirs.mas.erasmpoa.net.exception.NoInternetConnectionException
import ng.gov.eirs.mas.erasmpoa.sync.SyncTracker
import ng.gov.eirs.mas.erasmpoa.ui.base.BaseAppCompatActivity
import ng.gov.eirs.mas.erasmpoa.util.*
import java.net.SocketTimeoutException

class TargetActivity : BaseAppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_target)
        setUpToolbar(toolbar)

        loadTarget()
    }

    private fun loadTarget() {
        if (isConnectedToNetwork()) {
            SyncTracker.addSync(
                ApiClient.getClient(activity, true)
                    .getTargetAmount(
                        getToken(),
                        getUser()?.employeeId ?: 0L,
                        getUser()?.organizationId ?: 0L
                    ).compose<ApiResponse>(bindToLifecycle<ApiResponse>())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ response ->
                        mProgressDialog?.dismiss()
                        if (response.success == 1) {
                            val data = response.response?.data?.asJsonObject

                            val targetAmount = data?.get("targetAmount")?.asDouble ?: 0.0
                            val achievedAmount = data?.get("consumedAmount")?.asDouble ?: 0.0
                            val target = CollectionTarget(targetAmount, achievedAmount)
                            saveTarget(target)
                            updateView()
                        } else {
                            response.response?.message?.let {
                                when {
                                    it.isJsonObject -> coordinatorLayout.snackBar(R.string.please_try_again)
                                    it.isJsonPrimitive -> coordinatorLayout.snackBar(it.asString)
                                    else -> coordinatorLayout.snackBar(R.string.please_try_again)
                                }
                            }
                                ?: kotlin.run { coordinatorLayout.snackBar(R.string.please_try_again) }
                        }
                    }, {
                        mProgressDialog?.dismiss()
                        if (it is SocketTimeoutException || it is NoInternetConnectionException) {
                            coordinatorLayout.snackBar(R.string.connection_error)
                        } else {
                            coordinatorLayout.snackBar(R.string.please_try_again)
                        }
                        it.printStackTrace()
                    })
            )
            mProgressDialog = ProgressDialog.show(activity, null, getString(R.string.loading))
        } else {
            coordinatorLayout.snackBar(R.string.connection_error)
            // updateView()
        }
    }

    private fun updateView() {
        val target = getTarget() ?: return

        val unsyncedAmount = SQLite.select()
            .from(Submission::class.java)
            .where(Submission_Table.synced.eq(false).collate(Collate.NOCASE))
            .queryList()
            .sumByDouble {
                it.amountPaid
            }

        tvTargetAmount.text = "${getString(R.string.niara)} ${target.targetAmount.format(0)}"
        tvAchieved.text =
            "${getString(R.string.niara)} ${target.achievedAmount.plus(unsyncedAmount).format(0)}"
        tvRemaining.text =
            "${getString(R.string.niara)} ${target.getRemainingAmount(unsyncedAmount).format(0)}"

        tvTargetStatus.text = "${target.getTargetAchievePercentage(unsyncedAmount)}% Achieved"

    }

}
