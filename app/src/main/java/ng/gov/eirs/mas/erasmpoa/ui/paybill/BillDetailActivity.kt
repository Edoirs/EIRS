package ng.gov.eirs.mas.erasmpoa.ui.paybill

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_bill_details.*
import kotlinx.android.synthetic.main.row_bill_item.view.*
import ng.gov.eirs.mas.erasmpoa.R
import ng.gov.eirs.mas.erasmpoa.data.GsonProvider
import ng.gov.eirs.mas.erasmpoa.data.constant.RequestCode
import ng.gov.eirs.mas.erasmpoa.data.model.*
import ng.gov.eirs.mas.erasmpoa.helper.BaseRecyclerViewAdapter
import ng.gov.eirs.mas.erasmpoa.net.ApiClient
import ng.gov.eirs.mas.erasmpoa.net.exception.NoInternetConnectionException
import ng.gov.eirs.mas.erasmpoa.sync.SyncTracker
import ng.gov.eirs.mas.erasmpoa.ui.base.BaseAppCompatActivity
import ng.gov.eirs.mas.erasmpoa.ui.common.ScanScratchCardDialogFragment
import ng.gov.eirs.mas.erasmpoa.ui.home.HomeActivity
import ng.gov.eirs.mas.erasmpoa.util.*
import java.net.SocketTimeoutException
import java.util.*
import kotlin.collections.ArrayList

class BillDetailActivity : BaseAppCompatActivity() {

    private var mScratchCard: String? = null
    private var mBill: Bill? = null
    private var mAmountToPay: Double = 0.toDouble()
    private var mAssessmentRuleItems: MutableList<AssessmentRuleItem> = ArrayList()
        set(value) {
            field = value
            var amountToPay = 0.toDouble()
            value.forEach { amountToPay += it.PendingAmount }
            updateAmountToPay(amountToPay)
        }

    private var mMdaServiceItems: MutableList<MdaServiceItem> = ArrayList()
        set(value) {
            field = value
            var amountToPay = 0.toDouble()
            value.forEach { amountToPay += it.PendingAmount }
            updateAmountToPay(amountToPay)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bill_details)

        setUpToolbar(toolbar)
        setHomeAsUp(true)

        mBill = intent.getSerializableExtra("bill") as Bill?

        recycleView.setHasFixedSize(false)
        recycleView.layoutManager = android.support.v7.widget.LinearLayoutManager(activity)
        ContextCompat.getDrawable(activity, R.drawable.divider_1dp_grey300)?.let {
            val decoration = android.support.v7.widget.DividerItemDecoration(
                activity,
                android.support.v7.widget.DividerItemDecoration.VERTICAL
            )
            decoration.setDrawable(it)
            recycleView.addItemDecoration(decoration)
        }

        setValues()

        viewBillRef.setOnClickListener { onClickBillFullDetail() }
        viewTaxPayerRin.setOnClickListener { onClickTaxPayerDetail() }
        viewOutstanding.setOnClickListener { onClickOutstanding() }
        viewPin.setOnClickListener { onClickScratchCard() }
        btnPay.setOnClickListener { onClickPay() }
    }

    private fun setValues() {
        tvBillRef.text = mBill?.getBillRef()
        tvTaxPayerRin.text = mBill?.TaxPayerRIN
        tvOutstanding.text =
            "${getString(R.string.niara)} ${mBill?.getOutstandingAmount()?.format(2)}"

        updateAmountToPay(mBill?.getOutstandingAmount() ?: 0.toDouble())

        tvItemTitle.text =
            if (mBill?.isAssessmentBill() == true) getString(R.string.assessment_rules) else getString(
                R.string.mda_services
            )

        loadRuleOrService()
        loadItems()
    }

    private fun loadRuleOrService() {
        SyncTracker.addSync(
            if (mBill?.isAssessmentBill() == true) {
                ApiClient.getClient(activity, true)
                    .assessmentRuleDetail(getToken(), mBill?.AssessmentID)
            } else {
                ApiClient.getClient(activity, true)
                    .serviceBillMdaServiceDetail(getToken(), mBill?.ServiceBillID)
            }.compose<ApiResponse>(bindToLifecycle<ApiResponse>())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ response ->
                    progressBar.gone()
                    if (response.success == 1) {
                        val data = response.response?.data?.asJsonObject
                        if (data?.get("Success")?.asBoolean == true) {
                            if (mBill?.isAssessmentBill() == true) {
                                val list = GsonProvider.getGson().fromJson<Array<AssessmentRule>>(
                                    data.get("Result"),
                                    object : TypeToken<Array<AssessmentRule>>() {}.type
                                )
                                recycleView.adapter = AssessmentRuleAdapter(list.toMutableList())
                            } else {
                                val list = GsonProvider.getGson().fromJson<Array<MdaService>>(
                                    data.get("Result"),
                                    object : TypeToken<Array<MdaService>>() {}.type
                                )
                                recycleView.adapter = MdaServiceAdapter(list.toMutableList())
                            }
                        } else {
                            emptyView.visible()
                            data?.get("Result")?.let {
                                if (it.isJsonPrimitive) {
                                    emptyView.text = it.asString
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
                    progressBar.gone()
                    if (it is SocketTimeoutException || it is NoInternetConnectionException) {
                        coordinatorLayout.snackBar(R.string.connection_error)
                    } else {
                        coordinatorLayout.snackBar(R.string.please_try_again)
                    }
                    it.printStackTrace()
                })
        )
        emptyView.gone()
        progressBar.visible()
    }

    private fun loadItems() {
        SyncTracker.addSync(
            if (mBill?.isAssessmentBill() == true) {
                ApiClient.getClient(activity, true)
                    .assessmentItemDetail(getToken(), mBill?.AssessmentID)
            } else {
                ApiClient.getClient(activity, true)
                    .serviceBillItemDetail(getToken(), mBill?.ServiceBillID)
            }.compose<ApiResponse>(bindToLifecycle<ApiResponse>())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ response ->
                    progressBar.gone()
                    if (response.success == 1) {
                        val data = response.response?.data?.asJsonObject
                        if (data?.get("Success")?.asBoolean == true) {
                            if (mBill?.isAssessmentBill() == true) {
                                val list =
                                    GsonProvider.getGson().fromJson<Array<AssessmentRuleItem>>(
                                        data.get("Result"),
                                        object : TypeToken<Array<AssessmentRuleItem>>() {}.type
                                    )
                                mAssessmentRuleItems = list.toMutableList()
                            } else {
                                val list = GsonProvider.getGson().fromJson<Array<MdaServiceItem>>(
                                    data.get("Result"),
                                    object : TypeToken<Array<MdaServiceItem>>() {}.type
                                )
                                mMdaServiceItems = list.toMutableList()
                            }
                        } else {
                            emptyView.visible()
                            data?.get("Result")?.let {
                                if (it.isJsonPrimitive) {
                                    emptyView.text = it.asString
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
                    progressBar.gone()
                    if (it is SocketTimeoutException || it is NoInternetConnectionException) {
                        coordinatorLayout.snackBar(R.string.connection_error)
                    } else {
                        coordinatorLayout.snackBar(R.string.please_try_again)
                    }
                    it.printStackTrace()
                })
        )
        emptyView.gone()
        progressBar.visible()
    }

    private fun updateAmountToPay(amountToPay: Double) {
        if (amountToPay != mAmountToPay) {
            updateScratchCard(null)
        }
        mAmountToPay = amountToPay
        tvAmountToPay.text = "${getString(R.string.niara)} ${mAmountToPay.format(2)}"
    }

    private fun onClickBillFullDetail() {
        val intent = Intent(activity, BillDetailFullActivity::class.java)
        intent.putExtra("bill", mBill)
        startActivity(intent)
    }

    private fun onClickTaxPayerDetail() {
        val intent = Intent(activity, TaxPayerDetailActivity::class.java)
        intent.putExtra("bill", mBill)
        startActivity(intent)
    }

    private fun onClickOutstanding() {
        val intent = Intent(activity, SettlementDetailActivity::class.java)
        intent.putExtra("bill", mBill)
        startActivity(intent)
    }

    private fun onClickScratchCard() {
        val fm = supportFragmentManager
        val ft = fm.beginTransaction()

        ScanScratchCardDialogFragment.newInstance(remainingAmount = mAmountToPay).apply {
            setOnSelectedListener { card -> verifyScratchCard(card) }
            show(ft, "ScanBarcodeDialogFragment")
        }
    }

    private fun verifyScratchCard(scratchCard: String) {
        SyncTracker.addSync(
            ApiClient.getClient(activity, true).scratchCardVerify(getToken(), scratchCard)
                .compose<ApiResponse>(bindToLifecycle<ApiResponse>())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ response ->
                    mProgressDialog?.dismiss()
                    if (response.success == 1) {
                        val amount = try {
                            response.response?.data?.asJsonObject?.get("amount")?.asDouble
                        } catch (e: Exception) {
                            0.toDouble()
                        }

                        if (amount == mAmountToPay) {
                            updateScratchCard(scratchCard)
                        } else {
                            coordinatorLayout.snackBar("This PIN is not for ${getString(R.string.niara)} $mAmountToPay")
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
                })
        )
        mProgressDialog = ProgressDialog.show(activity, null, getString(R.string.loading))
    }

    private fun updateScratchCard(scratchCard: String?) {
        scratchCard?.let {
            mScratchCard = it
            tvPin.text = mScratchCard?.toUpperCase(Locale.getDefault())
            tvPinLabel.setTextColor(Color.GRAY)
        } ?: kotlin.run {
            mScratchCard = null
            tvPin.text = ""
            tvPinLabel.setTextColor(Color.GRAY)
        }
    }

    private fun onClickPay() {
        tilNotes.errorNull()
        tvPinLabel.setTextColor(Color.GRAY)

        val notes = etNotes.text.toString()
        val itemsJsonArray = JsonArray()
        if (mBill?.isAssessmentBill() == true) {
            mAssessmentRuleItems.forEach {
                val item = JsonObject()
                item.addProperty("TBPKID", it.AAIID)
                item.addProperty("ToSettleAmount", it.PendingAmount.format(0, false))
                item.addProperty("TaxAmount", mAmountToPay.format(0, false))
                itemsJsonArray.add(item)
            }
        } else {
            mMdaServiceItems.forEach {
                val item = JsonObject()
                item.addProperty("TBPKID", it.SBSIID)
                item.addProperty("ToSettleAmount", it.PendingAmount.format(0, false))
                item.addProperty("TaxAmount", mAmountToPay.format(0, false))
                itemsJsonArray.add(item)
            }
        }

        var isOkay = true

        if (notes.isEmpty()) {
            tilNotes.error = getString(R.string.required_field)
            etNotes.requestFocus()
            isOkay = false
        }

        if (mScratchCard.isNullOrBlank()) {
            tvPinLabel.setTextColor(Color.RED)
            tvPinLabel.requestFocus()
            isOkay = false
        }

        if (isOkay) {
            hideKeyboard()
            savePayment(notes, itemsJsonArray.toString())
        }
    }

    private fun savePayment(notes: String, items: String) {
        SyncTracker.addSync(
            ApiClient.getClient(activity, true)
                .saveCollection(
                    getToken(),
                    if (mBill?.isAssessmentBill() == true) "AssessmentID" else "ServiceBillID",
                    mBill?.getBillRef(),
                    mBill?.getBillId() ?: 0,
                    mScratchCard,
                    mAmountToPay.format(0, false),
                    notes,
                    items
                ).compose<ApiResponse>(bindToLifecycle<ApiResponse>())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ response ->
                    mProgressDialog?.dismiss()
                    if (response.success == 1) {
                        showTransactionSuccessfulDialog()
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
                })
        )
        mProgressDialog = ProgressDialog.show(activity, null, getString(R.string.loading))
    }

    private fun showTransactionSuccessfulDialog() {
        AlertDialog.Builder(activity).apply {
            setMessage("Transaction successful")
            setPositiveButton(R.string.okay) { _, _ ->
                val intent = Intent(activity, HomeActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
            show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RequestCode.EDIT) {
            if (resultCode == Activity.RESULT_OK) {
                val items = data?.getSerializableExtra("items")
                if (mBill?.isAssessmentBill() == true) {
                    (items as Array<AssessmentRuleItem>?)?.toMutableList()?.let {
                        mAssessmentRuleItems = it
                    }
                } else {
                    (items as Array<MdaServiceItem>?)?.toMutableList()?.let {
                        mMdaServiceItems = it
                    }
                }

//                val amountToPay = data?.getDoubleExtra("amountToPay", 0.toDouble()) ?: 0.toDouble()
//                updateAmountToPay(amountToPay) 6279404432875567

//                if (data?.hasExtra("assessmentItems") == true) {
//                    val list = data.getSerializableExtra("assessmentItems") as Array<AssessmentItem>
//                    recycleView.adapter = AssessmentRuleAdapter(list.toMutableList())
//
//                    var amountToPay = 0.toDouble()
//                    list.forEach { amountToPay += it.PendingAmount }
//                    updateAmountToPay(amountToPay)
//                } else if (data?.hasExtra("mdaServices") == true) {
//                    val list = data.getSerializableExtra("mdaServices") as Array<MdaServiceItem>
//                    recycleView.adapter = MdaServiceAdapter(list.toMutableList())
//
//                    var amountToPay = 0.toDouble()
//                    list.forEach { amountToPay += it.PendingAmount }
//                    updateAmountToPay(amountToPay)
//                }
            }
        }
    }

    private inner class AssessmentRuleAdapter(list: MutableList<AssessmentRule>) :
        BaseRecyclerViewAdapter<AssessmentRule, AssessmentRuleAdapter.ItemHolder>(list) {

        override fun onCreateViewHolder(parent: ViewGroup, position: Int): ItemHolder {
            return ItemHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.row_bill_item, parent, false)
            )
        }

        override fun onBindViewHolder(holder: ItemHolder, position: Int) {
            val item = getItem(position)

            holder.itemView.tvItemName.text = item.AssessmentRuleName
            holder.itemView.tvItemAmount.text =
                "${getString(R.string.niara)} ${item.AssessmentRuleAmount.format(0)}"

            holder.itemView.setOnClickListener {
                val intent = Intent(activity, RuleItemActivity::class.java)
                intent.putExtra("bill", mBill)
                startActivityForResult(intent, RequestCode.EDIT)
            }
        }

        internal inner class ItemHolder(root: View) :
            android.support.v7.widget.RecyclerView.ViewHolder(root)
    }

    private inner class MdaServiceAdapter(list: MutableList<MdaService>) :
        BaseRecyclerViewAdapter<MdaService, MdaServiceAdapter.ItemHolder>(list) {

        override fun onCreateViewHolder(parent: ViewGroup, position: Int): ItemHolder {
            return ItemHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.row_bill_item, parent, false)
            )
        }

        override fun onBindViewHolder(holder: ItemHolder, position: Int) {
            val item = getItem(position)

            holder.itemView.tvItemName.text = item.MDAServiceName
            holder.itemView.tvItemAmount.text =
                "${getString(R.string.niara)} ${item.ServiceAmount.format(0)}"

            holder.itemView.setOnClickListener {
                val intent = Intent(activity, RuleItemActivity::class.java)
                intent.putExtra("bill", mBill)
                startActivityForResult(intent, RequestCode.EDIT)
            }
        }

        internal inner class ItemHolder(root: View) :
            android.support.v7.widget.RecyclerView.ViewHolder(root)
    }
}