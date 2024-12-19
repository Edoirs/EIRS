package ng.gov.eirs.mas.erasmpoa.ui.payonaccount

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.support.v4.app.TaskStackBuilder
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.widget.RecyclerView
import android.text.SpannableString
import android.text.TextUtils
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import com.raizlabs.android.dbflow.annotation.Collate
import com.raizlabs.android.dbflow.rx2.language.RXSQLite
import com.raizlabs.android.dbflow.sql.language.SQLite
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_submission.*
import kotlinx.android.synthetic.main.row_pin.view.*
import ng.gov.eirs.mas.erasmpoa.R
import ng.gov.eirs.mas.erasmpoa.data.FlavoredDevices
import ng.gov.eirs.mas.erasmpoa.data.GsonProvider
import ng.gov.eirs.mas.erasmpoa.data.constant.SettlementMethod
import ng.gov.eirs.mas.erasmpoa.data.constant.SubmissionType
import ng.gov.eirs.mas.erasmpoa.data.dao.*
import ng.gov.eirs.mas.erasmpoa.data.model.getErrorMessage
import ng.gov.eirs.mas.erasmpoa.data.model.isSuccess
import ng.gov.eirs.mas.erasmpoa.helper.BaseRecyclerViewAdapter
import ng.gov.eirs.mas.erasmpoa.helper.SelectionDialog
import ng.gov.eirs.mas.erasmpoa.net.ApiClient
import ng.gov.eirs.mas.erasmpoa.net.SpacePointeApiClient
import ng.gov.eirs.mas.erasmpoa.net.exception.NoInternetConnectionException
import ng.gov.eirs.mas.erasmpoa.sync.SyncTracker
import ng.gov.eirs.mas.erasmpoa.ui.base.BaseAppCompatActivity
import ng.gov.eirs.mas.erasmpoa.ui.common.ScanScratchCardDialogFragment
import ng.gov.eirs.mas.erasmpoa.ui.home.HomeActivity
import ng.gov.eirs.mas.erasmpoa.util.*
import java.net.SocketTimeoutException
import java.util.*

@Suppress("DEPRECATION")
class SubmissionActivity : BaseAppCompatActivity() {

    private var mMenuSection: String? = null
    private var mGroup: String? = null
    private var mSubGroup: String? = null
    private var mCategory: String? = null
    private var mPriceSheetAmount: Double = 0.toDouble()
    private var mPriceSheetId: Int = 0

    private var mTaxPayerType: TaxPayerType? = null
    private var mLga: Lga? = null

    private var mAdapter: ListAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_submission)
        setUpToolbar(toolbar)
        setHomeAsUp(true)

        mMenuSection = intent.getStringExtra("menuSection")
        mGroup = intent.getStringExtra("group")
        mSubGroup = intent.getStringExtra("subGroup")
        mCategory = intent.getStringExtra("category")

        tvMenuSection.text = mMenuSection
        tvGroup.text = mGroup
        tvSubGroup.text = mSubGroup
        tvCategory.text = mCategory

        recyclerView.setHasFixedSize(false)
        val layoutManager = android.support.v7.widget.LinearLayoutManager(activity)
        recyclerView.layoutManager = layoutManager

        ContextCompat.getDrawable(activity, R.drawable.divider_1dp_grey300)?.let {
            val decoration = android.support.v7.widget.DividerItemDecoration(
                activity,
                android.support.v7.widget.DividerItemDecoration.VERTICAL
            )
            decoration.setDrawable(it)
            recyclerView.addItemDecoration(decoration)
        }

        mAdapter = ListAdapter(ArrayList())
        recyclerView.adapter = mAdapter

        loadPriceSheetAmount()

        viewTaxPayerType.setOnClickListener { onClickTaxPayerType() }
        viewLga.setOnClickListener { onClickLga() }
        viewPin.setOnClickListener { onClickScratchCard() }
        btnSave.setOnClickListener { onClickSave() }
    }

    private fun onClickSave() {
        tvTaxPayerTypeLabel.setTextColor(Color.GRAY)
        tilTaxPayerName.errorNull()
        tilTaxPayerMobile.errorNull()
        tilTaxAssessed.errorNull()
        tvLgaLabel.setTextColor(Color.GRAY)
        tilAddress.errorNull()

        tilAmountToPay.errorNull()
        tvPinLabel.setTextColor(Color.GRAY)
        tilNotes.errorNull()

        val name = etTaxPayerName.text.toString().trim()
        val mobile = etTaxPayerMobile.text.toString().trim()
        val assessableIncome = etAssessableIncome.text.toString().toDoubleOrZero()
        val taxAssessed = etTaxAssessed.text.toString().toDoubleOrZero()
        val address = etAddress.text.toString().trim()

        val amountToPay = getAmountToPay()
        val notes = etNotes.text.toString().trim()

        var isOkay = true

        if (mTaxPayerType == null) {
            tvTaxPayerTypeLabel.setTextColor(Color.RED)
            tvTaxPayerTypeLabel.requestFocus()
            isOkay = false
        }

        if (name.isEmpty()) {
            tilTaxPayerName.error = getString(R.string.required_field)
            tilTaxPayerName.requestFocus()
            isOkay = false
        }

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

        if (taxAssessed == 0.toDouble()) {
            tilTaxAssessed.error = getString(R.string.required_field)
            tilTaxAssessed.requestFocus()
            isOkay = false
        }

        if (address.isEmpty()) {
            tilAddress.error = getString(R.string.required_field)
            tilAddress.requestFocus()
            isOkay = false
        }

        if (amountToPay == 0.toDouble()) {
            tilAmountToPay.error = getString(R.string.required_field)
            tilAmountToPay.requestFocus()
            isOkay = false
        }

        if (notes.isEmpty()) {
            tilNotes.error = getString(R.string.required_field)
            tilNotes.requestFocus()
            isOkay = false
        }

        if (mLga == null) {
            tvLgaLabel.setTextColor(Color.RED)
            tvLgaLabel.requestFocus()
            isOkay = false
        }

        if (mAdapter?.isEmpty() == true) {
            tvPinLabel.setTextColor(Color.RED)
            tvPinLabel.requestFocus()
            isOkay = false
        }

        val cards = JsonArray()
        mAdapter?.dataSet?.forEach { card ->
            cards.add(JsonObject().apply {
                addProperty("card", card.key)
                addProperty("amount", card.value)
            })
        }

        if (isOkay) {
            hideKeyboard()

            val submission = Submission()
            submission.synced = false

            submission.name = name
            submission.mobile = mobile
            submission.address = address
            submission.assessableIncome = assessableIncome
            submission.taxAssessed = taxAssessed
            submission.amountPaid = getAmountToPay()
            submission.notes = notes

            submission.menuSection = mMenuSection.toString()
            submission.group = mGroup.toString()
            submission.subGroup = mSubGroup.toString()
            submission.category = mCategory.toString()
            submission.priceSheetId = mPriceSheetId
            submission.priceSheetAmount = mPriceSheetAmount
            submission.taxPayerTypeId = mTaxPayerType?.taxPayerTypeId ?: 0
            submission.areaId = mLga?.areaId ?: 0
            submission.paymentType = SettlementMethod.SCRATCH_CARD
            submission.scratchCard = cards.toString()

            submission.submissionTime = Date().time
            submission.submittedBy = getUser()?.employeeId ?: 0

            submission.offlineId = "${System.nanoTime()}_${getUser()?.employeeId}"

            saveSubmission(submission)
        }
    }

    private fun saveSubmission(submission: Submission) {
        if (isConnectedToNetwork()) {
            SyncTracker.addSync(
                ApiClient.getClient(activity, false).saveSubmission(
                    getToken(),
                    submission.menuSection,
                    submission.group,
                    submission.subGroup,
                    submission.category,
                    submission.priceSheetId,
                    submission.priceSheetAmount.format(2, false),
                    submission.taxPayerTypeId,
                    submission.name,
                    submission.mobile,
                    submission.areaId,
                    submission.address,
                    submission.assessableIncome.format(2, false),
                    submission.taxAssessed.format(2, false),
                    submission.amountPaid.format(2, false),
                    submission.notes,
                    submission.paymentType,
                    submission.scratchCard,
                    SubmissionType.ONLINE,
                    null
                ).compose(bindToLifecycle())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ response ->
                        mProgressDialog?.dismiss()
                        if (response.isSuccess()) {
                            // val data = response.response.data

                            showTransactionSuccessfulDialog(submission)
                        } else {
                            
                            alertWithOkay(getString(R.string.error), response.getErrorMessage())
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
//            submission.save()
//            showTransactionSuccessfulDialog(submission)
        }
    }

    private fun showTransactionSuccessfulDialog(submission: Submission) {
        val scratchCard = submission.scratchCard
        val scratchCardJson = JsonParser().parse(scratchCard)
        if (scratchCardJson.isJsonArray) {
            (scratchCardJson as JsonArray).forEach {
                val card = it.asJsonObject["card"].asString
                ScratchCard.deleteScratchCard(card)
            }
        }

        AlertDialog.Builder(activity).apply {
            setMessage("Transaction successful")
            setCancelable(false)
            setPositiveButton(R.string.okay) { _, _ ->

                val printerIntent = FlavoredDevices.startPrinter(activity, submission)

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
        if (isConnectedToNetwork()) {
            SyncTracker.addSync(
                ApiClient.getClient(activity, true).taxPayerTypeList(getToken())
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
        } else {
            coordinatorLayout.snackBar(R.string.connection_error)

//            val query = SQLite.select()
//                    .from(TaxPayerType::class.java)
//
//            mProgressDialog = ProgressDialog.show(activity, null, getString(R.string.loading))
//
//            SyncTracker.addSync(RXSQLite.rx(query)
//                    .queryList()
//                    .doFinally {
//                        mProgressDialog?.dismiss()
//                    }
//                    .subscribe { list, _ ->
//                        SelectionDialog.showDialog(activity,
//                                getString(R.string.tax_payer_type),
//                                list.toMutableList()
//                        ) { selected ->
//                            mTaxPayerType = selected
//                            tvTaxPayerType.text = selected.taxPayerType
//                            tvTaxPayerTypeLabel.setTextColor(Color.GRAY)
//                        }
//                    })
        }
    }

    private fun onClickLga() {
        if (isConnectedToNetwork()) {
            SyncTracker.addSync(
                ApiClient.getClient(activity, true).lgaList(getToken())
                    .compose(bindToLifecycle())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ response ->
                        mProgressDialog?.dismiss()
                        if (response.success == 1) {
                            val data = response.response?.data
                            val list = GsonProvider.getGson().fromJson<Array<Lga>>(
                                data,
                                object : TypeToken<Array<Lga>>() {}.type
                            )

                            SelectionDialog.showDialog(
                                activity,
                                getString(R.string.lga),
                                list.toMutableList()
                            ) { selected ->
                                mLga = selected
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
        } else {
            coordinatorLayout.snackBar(R.string.connection_error)
//            val query = SQLite.select()
//                    .from(Lga::class.java)
//
//            mProgressDialog = ProgressDialog.show(activity, null, getString(R.string.loading))
//
//            SyncTracker.addSync(RXSQLite.rx(query)
//                    .queryList()
//                    .doFinally {
//                        mProgressDialog?.dismiss()
//                    }
//                    .subscribe { list, _ ->
//                        SelectionDialog.showDialog(activity,
//                                getString(R.string.lga),
//                                list
//                        ) { selected ->
//                            mLga = selected
//                            tvLga.text = mLga?.areaName
//                            tvLgaLabel.setTextColor(Color.GRAY)
//                        }
//                    })
        }
    }

    private fun onClickScratchCard() {
        val fm = supportFragmentManager
        val ft = fm.beginTransaction()

        ScanScratchCardDialogFragment.newInstance(getRemainingAmount()).apply {
            setOnSelectedListener { code ->
                if (mAdapter?.isCardAlreadyAdded(code) == true) {
                    alertWithOkay(getString(R.string.error), "This card is already scanned")
                } else {
                    verifyScratchCard(code)
                }
            }
            show(ft, "ScanBarcodeDialogFragment")
        }
    }

    private fun verifyScratchCard(scratchCard: String) {
        if (isConnectedToNetwork()) {
            SyncTracker.addSync(
                ApiClient.getClient(activity, true)
                    .scratchCardVerifyMpoa(getToken(), scratchCard)
                    .compose(bindToLifecycle())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ response ->
                        mProgressDialog?.dismiss()
                        if (response.success == 1) {
                            val scratchCardAmount: Double = try {
                                response.response?.data?.asJsonObject?.get("amount")?.asDouble
                                    ?: 0.toDouble()
                            } catch (e: Exception) {
                                0.toDouble()
                            }

                            if (scratchCardAmount <= getAmountToPay()) {
                                addScratchCard(scratchCard, scratchCardAmount)
                            } else {
                                alertWithOkay(
                                    getString(R.string.error),
                                    "This PIN is not for ${getString(R.string.niara)}${getAmountToPay()}"
                                )
                            }
                        } else {
                            response.response?.message?.let {
                                when {
                                    it.isJsonObject -> alertWithOkay(
                                        R.string.error,
                                        R.string.please_try_again
                                    )
                                    it.isJsonPrimitive -> alertWithOkay(
                                        getString(R.string.error),
                                        it.asString
                                    )
                                    else -> alertWithOkay(R.string.error, R.string.please_try_again)
                                }
                            }
                                ?: kotlin.run { coordinatorLayout.snackBar(R.string.please_try_again) }
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
//
//            val dbScratchCard = SQLite.select().from(ScratchCard::class.java)
//                    .where(ScratchCard_Table.qrCode.eq(scratchCard.md5()))
//                    .querySingle()
//
//            if (dbScratchCard == null) {
//                alertWithOkay(getString(R.string.error), "This scratch card is not valid or not synced")
//            } else {
//                val scratchCardAmount = dbScratchCard.amount
//
//                if (scratchCardAmount <= getAmountToPay()) {
//                    addScratchCard(scratchCard, scratchCardAmount)
//                } else {
//                    alertWithOkay(getString(R.string.error), "This PIN is not for ${getString(R.string.niara)}${getAmountToPay()}")
//                }
//            }
        }
    }

    private fun loadPriceSheetAmount() {
        if (isConnectedToNetwork()) {
            SyncTracker.addSync(
                ApiClient.getClient(activity, true)
                    .psAmount(getToken(), mMenuSection, mGroup, mSubGroup, mCategory)
                    .compose(bindToLifecycle())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ response ->
                        mProgressDialog?.dismiss()
                        if (response.success == 1) {
                            val data = response.response?.data?.asJsonArray?.get(0)?.asJsonObject
                            mPriceSheetId = data?.get("priceSheetAggregateId")?.asInt ?: 0
                            mPriceSheetAmount = data?.get("Price")?.asDouble ?: 0.0
                            updatePrice(mPriceSheetAmount)
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

//            val query = SQLite.select()
//                    .from(PriceSheet::class.java)
//                    .where(PriceSheet_Table.MenuSection.eq(mMenuSection).collate(Collate.NOCASE))
//                    .and(PriceSheet_Table.GroupName.eq(mGroup).collate(Collate.NOCASE))
//                    .and(PriceSheet_Table.SubGroupName.eq(mSubGroup).collate(Collate.NOCASE))
//                    .and(PriceSheet_Table.CategoryName.eq(mCategory).collate(Collate.NOCASE))
//
//            mProgressDialog = ProgressDialog.show(activity, null, getString(R.string.loading))
//
//            SyncTracker.addSync(RXSQLite.rx(query)
//                    .querySingle()
//                    .doFinally {
//                        mProgressDialog?.dismiss()
//                    }
//                    .subscribe {
//                        mPriceSheetId = it.priceSheetAggregateId
//                        mPriceSheetAmount = it.Price
//
//                        updatePrice(mPriceSheetAmount)
//                    })
        }
    }

    private fun getAmountToPay(): Double = etAmountToPay.text.toString().toDoubleOrZero()
    private fun getRemainingAmount(): Double = getAmountToPay() - (mAdapter?.totalAmount()
        ?: 0.toDouble())

    private fun addScratchCard(scratchCard: String, amount: Double) {
        mAdapter?.add(AbstractMap.SimpleEntry(scratchCard, amount))

        updateScratchCardView()
    }

    private fun updateScratchCardView() {
        val amountSpannable =
            SpannableString("${getString(R.string.niara)}${getRemainingAmount().format(0, false)}")
        amountSpannable.setSpan(StyleSpan(Typeface.BOLD), 0, amountSpannable.length, 0)
        tvPinAmount.text = TextUtils.concat("Attach PIN worth ", amountSpannable)
        tvPinLabel.setTextColor(Color.GRAY)

        if (getRemainingAmount() == 0.toDouble()) {
            viewPin.gone()
        } else {
            viewPin.visible()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updatePrice(amount: Double) {
        tvExpectedAmount.text = "${getString(R.string.niara)}${amount.format(0)}"
        etTaxAssessed.setText(amount.format(0, false))
        etAmountToPay.setText(amount.format(0, false))

        val amountSpannable =
            SpannableString("${getString(R.string.niara)}${amount.format(0, false)}")
        amountSpannable.setSpan(StyleSpan(Typeface.BOLD), 0, amountSpannable.length, 0)
        tvPinAmount.text = TextUtils.concat("Attach PIN worth ", amountSpannable)
    }

    private inner class ListAdapter(list: MutableList<Map.Entry<String, Double>>) :
        BaseRecyclerViewAdapter<Map.Entry<String, Double>, ListAdapter.ViewHolder>(list) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.row_pin, parent, false)
            return ViewHolder(v)
        }

        fun isCardAlreadyAdded(card: String): Boolean {
            dataSet.forEach { if (card == it.key) return true }
            return false
        }

        fun totalAmount(): Double {
            var total = 0.toDouble()
            dataSet.forEach { total += it.value }
            return total
        }

        @SuppressLint("SetTextI18n")
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = getItem(position)

            holder.itemView.tvPin.text = item.key
            holder.itemView.tvAmount.text =
                "${getString(R.string.niara)}${item.value.format(0, false)}"
        }

        internal inner class ViewHolder(root: View) : RecyclerView.ViewHolder(root),
            View.OnClickListener {
            init {
                root.setOnClickListener(this)
            }

            override fun onClick(v: View) {
                val item = getItem(adapterPosition)
                AlertDialog.Builder(v.context)
                    .setTitle(item.key)
                    .setItems(
                        arrayOf(
                            getString(R.string.detach),
                            getString(R.string.cancel)
                        )
                    ) { _, which ->
                        when (which) {
                            0 -> {
                                // detach
                                remove(item)
                                updateScratchCardView()
                            }
                            1 -> {
                                // cancel
                            }
                        }
                    }.show()
            }
        }
    }
}