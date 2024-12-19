package ng.gov.eirs.mas.erasmpoa.ui.haulage

import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.TaskStackBuilder
import android.support.v7.app.AlertDialog
import com.google.gson.reflect.TypeToken
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_haulage.*
import ng.gov.eirs.mas.erasmpoa.R
import ng.gov.eirs.mas.erasmpoa.data.FlavoredDevices
import ng.gov.eirs.mas.erasmpoa.data.GsonProvider
import ng.gov.eirs.mas.erasmpoa.data.dao.*
import ng.gov.eirs.mas.erasmpoa.data.model.ApiResponse
import ng.gov.eirs.mas.erasmpoa.helper.SelectionDialog
import ng.gov.eirs.mas.erasmpoa.net.ApiClient
import ng.gov.eirs.mas.erasmpoa.net.exception.NoInternetConnectionException
import ng.gov.eirs.mas.erasmpoa.sync.SyncTracker
import ng.gov.eirs.mas.erasmpoa.ui.base.BaseAppCompatActivity
import ng.gov.eirs.mas.erasmpoa.ui.home.HomeActivity
import ng.gov.eirs.mas.erasmpoa.util.*
import java.net.SocketTimeoutException
import java.util.*

@Suppress("DEPRECATION")
class HaulageActivity : BaseAppCompatActivity() {

    private var mTaxPayerType: TaxPayerType? = null
    private var mLga: Lga? = null
    private var mRevenueType: RevenueType? = null
    private var mBeat: Beat? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_haulage)
        setUpToolbar(toolbar)
        setHomeAsUp(true)

        viewTaxPayerType.setOnClickListener { onClickTaxPayerType() }
        viewLga.setOnClickListener { onClickLga() }
        viewRevenueType.setOnClickListener { onClickRevenueType() }
        viewBeat.setOnClickListener { onClickBeat() }

        btnSave.setOnClickListener { onClickSave() }
    }

    private fun onClickSave() {
        var isOkay = true

        tvLgaLabel.setTextColor(Color.GRAY)
        if (mLga == null) {
            tvLgaLabel.setTextColor(Color.RED)
            tvLgaLabel.shake()
            isOkay = false
        }

        tvRevenueTypeLabel.setTextColor(Color.GRAY)
        if (mRevenueType == null) {
            tvRevenueTypeLabel.setTextColor(Color.RED)
            tvRevenueTypeLabel.shake()
            isOkay = false
        }

        tvBeatLabel.setTextColor(Color.GRAY)
        if (mLga == null) {
            tvBeatLabel.setTextColor(Color.RED)
            tvBeatLabel.shake()
            isOkay = false
        }

        tvTaxPayerTypeLabel.setTextColor(Color.GRAY)
        if (mTaxPayerType == null) {
            tvTaxPayerTypeLabel.setTextColor(Color.RED)
            tvTaxPayerTypeLabel.shake()
            isOkay = false
        }

        val name = etTaxPayerName.text.toString().trim()
        tilTaxPayerName.errorNull()
        if (name.isEmpty()) {
            tilTaxPayerName.error = getString(R.string.required_field)
            tilTaxPayerName.requestFocus()
            isOkay = false
        }

        val mobile = etTaxPayerMobile.text.toString().trim()
        tilTaxPayerMobile.errorNull()
        if (mobile.isEmpty()) {
            tilTaxPayerMobile.error = getString(R.string.required_field)
            tilTaxPayerMobile.requestFocus()
            isOkay = false
        } else if (mobile.length != 11) {
            tilTaxPayerMobile.error = "Enter valid 11 digit mobile number"
            tilTaxPayerMobile.requestFocus()
            isOkay = false
        } else if (!mobile.startsWith("09") && !mobile.startsWith("08") && !mobile.startsWith("07")) {
            tilTaxPayerMobile.error = "Enter valid mobile number (starting with 09, 08 or 07)"
            tilTaxPayerMobile.requestFocus()
            isOkay = false
        }

        val vehicleRegNo = etVehicleRegNo.text.toString().trim()
        tilVehicleRegNo.errorNull()
        if (vehicleRegNo.isEmpty()) {
            tilVehicleRegNo.error = getString(R.string.required_field)
            tilVehicleRegNo.requestFocus()
            isOkay = false
        }

        if (isOkay) {
            hideKeyboard()

            val haulage = Haulage()
            haulage.synced = false

            haulage.areaId = mBeat?.areaId ?: 0
            haulage.revenueTypeId = mBeat?.revenueTypeId ?: 0
            haulage.beatId = mBeat?.beatId ?: 0

            haulage.taxPayerTypeId = mTaxPayerType?.taxPayerTypeId ?: 0
            haulage.name = name
            haulage.mobile = mobile
            haulage.vehicleRegNo = vehicleRegNo

            haulage.submissionTime = Date().time
            haulage.submittedBy = getUser()?.employeeId ?: 0
            haulage.offlineId = "${System.nanoTime()}_${getUser()?.employeeId}"

            saveHaulage(haulage)
        }
    }

    private fun saveHaulage(haulage: Haulage) {
        if (isConnectedToNetwork()) {
            SyncTracker.addSync(
                ApiClient.getClient(activity, true)
                    .haulageSubmissionAdd(
                        getToken(),
                        haulage.areaId,
                        haulage.revenueTypeId,
                        haulage.beatId,
                        haulage.vehicleRegNo,
                        haulage.taxPayerTypeId,
                        haulage.name,
                        haulage.mobile,
                        haulage.offlineId
                    ).compose<ApiResponse>(bindToLifecycle<ApiResponse>())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ response ->
                        mProgressDialog?.dismiss()
                        if (response.success == 1) {
                            // val data = response.response.data
                            showTransactionSuccessfulDialog(haulage)
                        } else {
                            response.response?.message?.let { messageU ->
                                when {
                                    messageU.isJsonObject -> {
                                        var hasKnownError = false
                                        messageU.asJsonObject.get("areaId")?.let {
                                            tvLgaLabel.setTextColor(Color.RED)
                                            tvLgaLabel.shake()
                                            hasKnownError = true
                                        }
                                        messageU.asJsonObject.get("revenueTypeId")?.let {
                                            tvRevenueTypeLabel.setTextColor(Color.RED)
                                            tvRevenueTypeLabel.shake()
                                            hasKnownError = true
                                        }
                                        messageU.asJsonObject.get("beatId")?.let {
                                            tvBeatLabel.setTextColor(Color.RED)
                                            tvBeatLabel.shake()
                                            hasKnownError = true
                                        }
                                        messageU.asJsonObject.get("vehicleRegistrationNo")?.let {
                                            tilVehicleRegNo.error = it.asString
                                            tilVehicleRegNo.requestFocus()
                                            hasKnownError = true
                                        }
                                        messageU.asJsonObject.get("taxPayerTypeId")?.let {
                                            tvTaxPayerTypeLabel.setTextColor(Color.RED)
                                            tvTaxPayerTypeLabel.shake()
                                            hasKnownError = true
                                        }
                                        messageU.asJsonObject.get("name")?.let {
                                            tilTaxPayerName.error = it.asString
                                            etTaxPayerName.requestFocus()
                                            hasKnownError = true
                                        }
                                        messageU.asJsonObject.get("phoneNumber")?.let {
                                            tilTaxPayerMobile.error = it.asString
                                            etTaxPayerMobile.requestFocus()
                                            hasKnownError = true
                                        }
                                        if (!hasKnownError) {
                                            alertWithOkay(R.string.error, R.string.please_try_again)
                                        }
                                    }
                                    messageU.isJsonPrimitive -> alertWithOkay(
                                        getString(R.string.error),
                                        messageU.asString
                                    )
                                    else -> alertWithOkay(R.string.error, R.string.please_try_again)
                                }
                            }
                                ?: kotlin.run {
                                    alertWithOkay(
                                        R.string.error,
                                        R.string.please_try_again
                                    )
                                }
                        }
                    }, {
                        mProgressDialog?.dismiss()
                        if (it is SocketTimeoutException || it is NoInternetConnectionException) {
                            alertWithOkay(R.string.error, R.string.connection_error)
                        } else {
                            alertWithOkay(R.string.error, R.string.please_try_again)
                        }
                        it.printStackTrace()
                    })
            )
            mProgressDialog = ProgressDialog.show(activity, null, getString(R.string.loading))
        } else {
            alertWithOkay(R.string.error, R.string.connection_error)

//            haulage.save()
//            showTransactionSuccessfulDialog(haulage)
        }
    }

    private fun showTransactionSuccessfulDialog(haulage: Haulage) {
        AlertDialog.Builder(activity).apply {
            setMessage("Haulage collection recorded successfully")
            setPositiveButton(R.string.okay) { _, _ ->
                val printerIntent = FlavoredDevices.startPrinter(activity, haulage)

                val homeIntent = Intent(activity, HomeActivity::class.java)
                homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(homeIntent)

                val builder = TaskStackBuilder.create(activity)
                builder.addNextIntent(homeIntent)
                if (printerIntent != null) {
                    builder.addNextIntent(printerIntent)
                }

                builder.startActivities()
            }
            show()
        }
    }

    private fun onClickTaxPayerType() {
        SyncTracker.addSync(
            ApiClient.getClient(activity, true)
                .taxPayerTypeList(getToken())
                .compose(bindToLifecycle())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ response ->
                    mProgressDialog?.dismiss()
                    if (response.success == 1) {
                        val data = response.response?.data
                        val list = GsonProvider.getGson().fromJson<Array<TaxPayerType>>(
                            data,
                            object : TypeToken<Array<TaxPayerType>>() {}.type
                        )

                        SelectionDialog.showDialog(
                            activity,
                            getString(R.string.tax_payer_type),
                            list.toMutableList()
                        ) { selected ->
                            mTaxPayerType = selected
                            tvTaxPayerType.text = selected.taxPayerType
                            tvTaxPayerTypeLabel.setTextColor(Color.GRAY)
                        }
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
    }

    private fun onClickLga() {
        SyncTracker.addSync(
            ApiClient.getClient(activity, true)
                .haulageBeatList(getToken())
                .compose(bindToLifecycle())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ response ->
                    mProgressDialog?.dismiss()
                    if (response.success == 1) {
                        val data = response.response?.data
                        val list = GsonProvider.getGson()
                            .fromJson<Array<Beat>>(data, object : TypeToken<Array<Beat>>() {}.type)

                        val lgaPair = list
                            .distinctBy { it.areaId }
                            .map { Pair(it, it.areaName) }.toList()
                        SelectionDialog.showDialogPair(
                            activity,
                            getString(R.string.lga),
                            lgaPair
                        ) { selected ->
                            val lga = selected.first.toLga()

                            if (lga.areaId != mLga?.areaId) {
                                tvBeat.text = ""
                                tvBeatLabel.setTextColor(Color.GRAY)

                                tvRevenueType.text = ""
                                tvRevenueTypeLabel.setTextColor(Color.GRAY)
                            }
                            mLga = lga
                            tvLga.text = mLga?.areaName
                            tvLgaLabel.setTextColor(Color.GRAY)
                        }
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

//        val query = SQLite.select(Beat_Table.areaId.distinct(), Beat_Table.areaName)
//            .from(Beat::class.java)
//            .orderBy(Beat_Table.areaName.asc())
//
//        mProgressDialog = ProgressDialog.show(activity, null, getString(R.string.loading))
//
//        SyncTracker.addSync(RXSQLite.rx(query)
//            .queryList()
//            .doFinally {
//                mProgressDialog?.dismiss()
//            }
//            .subscribe { list, _ ->
//                val lgaPair = list.map { Pair(it, it.areaName) }.toList()
//                SelectionDialog.showDialogPair(
//                    activity,
//                    getString(R.string.lga),
//                    lgaPair
//                ) { selected ->
//                    val lga = selected.first.toLga()
//
//                    if (lga.areaId != mLga?.areaId) {
//                        tvBeat.text = ""
//                        tvBeatLabel.setTextColor(Color.GRAY)
//
//                        tvRevenueType.text = ""
//                        tvRevenueTypeLabel.setTextColor(Color.GRAY)
//                    }
//                    mLga = lga
//                    tvLga.text = mLga?.areaName
//                    tvLgaLabel.setTextColor(Color.GRAY)
//                }
//            })
    }

    private fun onClickRevenueType() {
        if (mLga == null) {
            tvLgaLabel.setTextColor(Color.RED)
            tvLgaLabel.shake()
            return
        }

        SyncTracker.addSync(
            ApiClient.getClient(activity, true)
                .haulageBeatList(getToken())
                .compose(bindToLifecycle())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ response ->
                    mProgressDialog?.dismiss()
                    if (response.success == 1) {
                        val data = response.response?.data
                        val list = GsonProvider.getGson()
                            .fromJson<Array<Beat>>(data, object : TypeToken<Array<Beat>>() {}.type)

                        val revenueTypePair = list
                            .distinctBy { it.revenueTypeId }
                            .filter { it.areaId == mLga?.areaId }
                            .map { Pair(it, it.revenueTypeName) }.toList()
                        SelectionDialog.showDialogPair(
                            activity,
                            getString(R.string.revenue_type),
                            revenueTypePair
                        ) { selected ->
                            val revenueType = selected.first.toRevenueType()
                            if (revenueType.revenueTypeId != mRevenueType?.revenueTypeId) {
                                tvBeat.text = ""
                                tvBeatLabel.setTextColor(Color.GRAY)
                            }
                            mRevenueType = revenueType
                            tvRevenueType.text = mRevenueType?.revenueTypeName
                            tvRevenueTypeLabel.setTextColor(Color.GRAY)
                        }
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


//        val query = SQLite.select(Beat_Table.revenueTypeId.distinct(), Beat_Table.revenueTypeName)
//            .from(Beat::class.java)
//            .where(Beat_Table.areaId.eq(mLga?.areaId))
//            .orderBy(Beat_Table.revenueTypeName.asc())
//
//        mProgressDialog = ProgressDialog.show(activity, null, getString(R.string.loading))
//
//        SyncTracker.addSync(RXSQLite.rx(query)
//            .queryList()
//            .doFinally {
//                mProgressDialog?.dismiss()
//            }
//            .subscribe { list, _ ->
//                val revenueTypePair = list.map { Pair(it, it.revenueTypeName) }.toList()
//                SelectionDialog.showDialogPair(
//                    activity,
//                    getString(R.string.revenue_type),
//                    revenueTypePair
//                ) { selected ->
//                    val revenueType = selected.first.toRevenueType()
//                    if (revenueType.revenueTypeId != mRevenueType?.revenueTypeId) {
//                        tvBeat.text = ""
//                        tvBeatLabel.setTextColor(Color.GRAY)
//                    }
//                    mRevenueType = revenueType
//                    tvRevenueType.text = mRevenueType?.revenueTypeName
//                    tvRevenueTypeLabel.setTextColor(Color.GRAY)
//                }
//            })
    }

    private fun onClickBeat() {
        if (mLga == null) {
            tvLgaLabel.setTextColor(Color.RED)
            tvLgaLabel.shake()
            return
        }

        if (mRevenueType == null) {
            tvRevenueTypeLabel.setTextColor(Color.RED)
            tvRevenueTypeLabel.shake()
            return
        }

        SyncTracker.addSync(
            ApiClient.getClient(activity, true)
                .haulageBeatList(getToken())
                .compose(bindToLifecycle())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ response ->
                    mProgressDialog?.dismiss()
                    if (response.success == 1) {
                        val data = response.response?.data
                        val list = GsonProvider.getGson()
                            .fromJson<Array<Beat>>(data, object : TypeToken<Array<Beat>>() {}.type)

                        val revenueTypePair = list
                            .filter { it.areaId == mLga?.areaId }
                            .filter { it.revenueTypeId == mRevenueType?.revenueTypeId }
                            .map { Pair(it, it.beatName) }.toList()
                        SelectionDialog.showDialogPair(
                            activity,
                            getString(R.string.beat),
                            revenueTypePair
                        ) { selected ->
                            mBeat = selected.first
                            tvBeat.text = mBeat?.beatName
                            tvBeatLabel.setTextColor(Color.GRAY)
                        }
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


//        val query = SQLite.select()
//            .from(Beat::class.java)
//            .where(Beat_Table.areaId.eq(mLga?.areaId))
//            .and(Beat_Table.revenueTypeId.eq(mRevenueType?.revenueTypeId))
//            .orderBy(Beat_Table.revenueTypeName.asc())
//
//        mProgressDialog = ProgressDialog.show(activity, null, getString(R.string.loading))
//
//        SyncTracker.addSync(RXSQLite.rx(query)
//            .queryList()
//            .doFinally {
//                mProgressDialog?.dismiss()
//            }
//            .subscribe { list, _ ->
//                val revenueTypePair = list.map { Pair(it, it.beatName) }.toList()
//                SelectionDialog.showDialogPair(
//                    activity,
//                    getString(R.string.beat),
//                    revenueTypePair
//                ) { selected ->
//                    mBeat = selected.first
//                    tvBeat.text = mBeat?.beatName
//                    tvBeatLabel.setTextColor(Color.GRAY)
//                }
//            })
    }

}