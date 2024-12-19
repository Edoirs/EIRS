package ng.gov.eirs.mas.erasmpoa.ui.paybill

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.gson.reflect.TypeToken
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_bill_item_amount.*
import kotlinx.android.synthetic.main.row_bill_item_amount.view.*
import ng.gov.eirs.mas.erasmpoa.R
import ng.gov.eirs.mas.erasmpoa.data.GsonProvider
import ng.gov.eirs.mas.erasmpoa.data.model.ApiResponse
import ng.gov.eirs.mas.erasmpoa.data.model.AssessmentRuleItem
import ng.gov.eirs.mas.erasmpoa.data.model.Bill
import ng.gov.eirs.mas.erasmpoa.data.model.MdaServiceItem
import ng.gov.eirs.mas.erasmpoa.helper.BaseRecyclerViewAdapter
import ng.gov.eirs.mas.erasmpoa.net.ApiClient
import ng.gov.eirs.mas.erasmpoa.net.exception.NoInternetConnectionException
import ng.gov.eirs.mas.erasmpoa.sync.SyncTracker
import ng.gov.eirs.mas.erasmpoa.ui.base.BaseAppCompatActivity
import ng.gov.eirs.mas.erasmpoa.util.*
import java.net.SocketTimeoutException

class RuleItemActivity : BaseAppCompatActivity() {

    private var mBill: Bill? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bill_item_amount)

        setUpToolbar(toolbar)
        setHomeAsUp(true)

        mBill = intent.getSerializableExtra("bill") as Bill?

        if (mBill?.isAssessmentBill() == true) {
            tvItemTitle.setText(R.string.assessment_rules)
        } else {
            tvItemTitle.setText(R.string.mda_services)
        }

        recycleView.setHasFixedSize(false)
        recycleView.layoutManager = android.support.v7.widget.LinearLayoutManager(activity)
        ContextCompat.getDrawable(activity, R.drawable.divider_1dp_grey300)?.let {
            val decoration = android.support.v7.widget.DividerItemDecoration(activity, android.support.v7.widget.DividerItemDecoration.VERTICAL)
            decoration.setDrawable(it)
            recycleView.addItemDecoration(decoration)
        }

        loadItems()

        btnSave.setOnClickListener { onClickSave() }
    }

    private fun loadItems() {
        SyncTracker.addSync(if (mBill?.isAssessmentBill() == true) {
            ApiClient.getClient(activity, true).assessmentItemDetail(getToken(), mBill?.AssessmentID)
        } else {
            ApiClient.getClient(activity, true).serviceBillItemDetail(getToken(), mBill?.ServiceBillID)
        }.compose<ApiResponse>(bindToLifecycle<ApiResponse>())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ response ->
                    progressBar.gone()
                    if (response.success == 1) {
                        val data = response.response?.data?.asJsonObject
                        if (data?.get("Success")?.asBoolean == true) {
                            if (mBill?.isAssessmentBill() == true) {
                                val list = GsonProvider.getGson().fromJson<Array<AssessmentRuleItem>>(data.get("Result"), object : TypeToken<Array<AssessmentRuleItem>>() {}.type)
                                recycleView.adapter = AssessmentRuleItemAdapter(list.toMutableList())
                            } else {
                                val list = GsonProvider.getGson().fromJson<Array<MdaServiceItem>>(data.get("Result"), object : TypeToken<Array<MdaServiceItem>>() {}.type)
                                recycleView.adapter = MdaServiceItemAdapter(list.toMutableList())
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
                }))
        emptyView.gone()
        progressBar.visible()
    }

    private fun onClickSave() {
        var isOkay = true

        recycleView.adapter?.let { adapter ->
            if (adapter is AssessmentRuleItemAdapter) {
                val result = Intent()
                result.putExtra("items", adapter.dataSet.toTypedArray())
                setResult(Activity.RESULT_OK, result)
                finish()

//                var amountToPay = 0.toDouble()
//                adapter.dataSet.forEach { amountToPay += it.PendingAmount }
//                if (isOkay) {
//                    val result = Intent()
//                    result.putExtra("amountToPay", amountToPay)
//                    setResult(Activity.RESULT_OK, result)
//                    finish()
//                }
            } else if (adapter is MdaServiceItemAdapter) {
                val result = Intent()
                result.putExtra("items", adapter.dataSet.toTypedArray())
                setResult(Activity.RESULT_OK, result)
                finish()

//                var amountToPay = 0.toDouble()
//                adapter.dataSet.forEach { amountToPay += it.PendingAmount }
//                if (isOkay) {
//                    val result = Intent()
//                    result.putExtra("amountToPay", amountToPay)
//                    setResult(Activity.RESULT_OK, result)
//                    finish()
//                }
            }
        }

        if (!isOkay) {
            AlertDialog.Builder(activity).apply {
                setMessage("Please enter all valid amounts")
                setPositiveButton(R.string.okay, null)
                show()
            }
        }


        hideKeyboard()
    }

    private class AssessmentRuleItemAdapter(list: MutableList<AssessmentRuleItem>) : BaseRecyclerViewAdapter<AssessmentRuleItem, AssessmentRuleItemAdapter.ItemHolder>(list) {

        override fun onCreateViewHolder(parent: ViewGroup, position: Int): ItemHolder {
            return ItemHolder(
                    LayoutInflater.from(parent.context).inflate(R.layout.row_bill_item_amount, parent, false),
                    AmountWatcher()
            )
        }

        override fun onBindViewHolder(holder: ItemHolder, position: Int) {
            val item = getItem(position)

            holder.itemView.tvItemName.text = item.AssessmentItemName

            holder.watcher.updatePosition(position)
            holder.itemView.etItemAmount.setText(item.PendingAmount.format(0, false))
        }

        internal class ItemHolder(root: View, var watcher: AmountWatcher) : android.support.v7.widget.RecyclerView.ViewHolder(root) {
            init {
                itemView.etItemAmount.addTextChangedListener(watcher)
            }
        }

        private inner class AmountWatcher internal constructor() : TextWatcher {

            private var position: Int = 0

            internal fun updatePosition(position: Int) {
                this.position = position
            }

            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i2: Int, i3: Int) {
                // no op
            }

            override fun onTextChanged(charSequence: CharSequence, i: Int, i2: Int, i3: Int) {}

            override fun afterTextChanged(editable: Editable) {
                getItem(position).PendingAmount = editable.toString().toDoubleOrZero()
            }
        }
    }

    private class MdaServiceItemAdapter(list: MutableList<MdaServiceItem>) : BaseRecyclerViewAdapter<MdaServiceItem, MdaServiceItemAdapter.ItemHolder>(list) {

        override fun onCreateViewHolder(parent: ViewGroup, position: Int): ItemHolder {
            return ItemHolder(
                    LayoutInflater.from(parent.context).inflate(R.layout.row_bill_item_amount, parent, false),
                    AmountWatcher()
            )
        }

        override fun onBindViewHolder(holder: ItemHolder, position: Int) {
            val item = getItem(position)

            holder.itemView.tvItemName.text = item.MDAServiceItemName

            holder.watcher.updatePosition(position)
            holder.itemView.etItemAmount.setText(item.PendingAmount.format(0, false))
        }

        internal class ItemHolder(root: View, var watcher: AmountWatcher) : android.support.v7.widget.RecyclerView.ViewHolder(root) {
            init {
                itemView.etItemAmount.addTextChangedListener(watcher)
            }
        }

        private inner class AmountWatcher internal constructor() : TextWatcher {

            private var position: Int = 0

            internal fun updatePosition(position: Int) {
                this.position = position
            }

            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i2: Int, i3: Int) {
                // no op
            }

            override fun onTextChanged(charSequence: CharSequence, i: Int, i2: Int, i3: Int) {}

            override fun afterTextChanged(editable: Editable) {
                getItem(position).PendingAmount = editable.toString().toDoubleOrZero()
            }
        }
    }
}