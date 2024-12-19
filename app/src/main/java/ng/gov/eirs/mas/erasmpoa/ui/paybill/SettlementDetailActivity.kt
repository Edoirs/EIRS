package ng.gov.eirs.mas.erasmpoa.ui.paybill

import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.gson.reflect.TypeToken
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_settlement_details.*
import kotlinx.android.synthetic.main.row_bill_settlement.view.*
import ng.gov.eirs.mas.erasmpoa.R
import ng.gov.eirs.mas.erasmpoa.data.GsonProvider
import ng.gov.eirs.mas.erasmpoa.data.model.ApiResponse
import ng.gov.eirs.mas.erasmpoa.data.model.AssessmentSettlementItem
import ng.gov.eirs.mas.erasmpoa.data.model.Bill
import ng.gov.eirs.mas.erasmpoa.data.model.MdaServiceSettlementItem
import ng.gov.eirs.mas.erasmpoa.helper.BaseRecyclerViewAdapter
import ng.gov.eirs.mas.erasmpoa.net.ApiClient
import ng.gov.eirs.mas.erasmpoa.net.exception.NoInternetConnectionException
import ng.gov.eirs.mas.erasmpoa.sync.SyncTracker
import ng.gov.eirs.mas.erasmpoa.ui.base.BaseAppCompatActivity
import ng.gov.eirs.mas.erasmpoa.util.*
import java.net.SocketTimeoutException

class SettlementDetailActivity : BaseAppCompatActivity() {

    private var mBill: Bill? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settlement_details)

        setUpToolbar(toolbar)
        setHomeAsUp(true)

        mBill = intent.getSerializableExtra("bill") as Bill?

        recycleView.setHasFixedSize(false)
        recycleView.layoutManager = android.support.v7.widget.LinearLayoutManager(activity)
        ContextCompat.getDrawable(activity, R.drawable.divider_1dp_grey300)?.let {
            val decoration = android.support.v7.widget.DividerItemDecoration(activity, android.support.v7.widget.DividerItemDecoration.VERTICAL)
            decoration.setDrawable(it)
            recycleView.addItemDecoration(decoration)
        }

        loadItems()
    }

    private fun loadItems() {
        SyncTracker.addSync(if (mBill?.isAssessmentBill() == true) {
            ApiClient.getClient(activity, true).assessmentSettlementDetail(getToken(), mBill?.AssessmentID)
        } else {
            ApiClient.getClient(activity, true).serviceBillSettlementDetail(getToken(), mBill?.ServiceBillID)
        }.compose<ApiResponse>(bindToLifecycle<ApiResponse>())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ response ->
                    progressBar.gone()
                    if (response.success == 1) {
                        val data = response.response?.data?.asJsonObject
                        if (data?.get("Success")?.asBoolean==true) {
                            if (mBill?.isAssessmentBill() == true) {
                                val list = GsonProvider.getGson().fromJson<Array<AssessmentSettlementItem>>(data.get("Result"), object : TypeToken<Array<AssessmentSettlementItem>>() {}.type)
                                recycleView.adapter = AssessmentSettlementAdapter(list.toMutableList())
                            } else {
                                val list = GsonProvider.getGson().fromJson<Array<MdaServiceSettlementItem>>(data.get("Result"), object : TypeToken<Array<MdaServiceSettlementItem>>() {}.type)
                                recycleView.adapter = ServiceBillSettlementAdapter(list.toMutableList())
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

    private inner class AssessmentSettlementAdapter(list: MutableList<AssessmentSettlementItem>) : BaseRecyclerViewAdapter<AssessmentSettlementItem, AssessmentSettlementAdapter.ItemHolder>(list) {

        override fun onCreateViewHolder(parent: ViewGroup, position: Int): ItemHolder {
            return ItemHolder(LayoutInflater.from(parent.context).inflate(R.layout.row_bill_settlement, parent, false))
        }

        override fun onBindViewHolder(holder: ItemHolder, position: Int) {
            val item = getItem(position)

            holder.itemView.tvDate.text = item.SettlementDate.toDate(EIRS_DATE_FORMAT).format("MMM dd, yyyy")
            holder.itemView.tvAmount.text = "${getString(R.string.niara)} ${item.SettlementAmount.format(0)}"
        }

        internal inner class ItemHolder(root: View) : android.support.v7.widget.RecyclerView.ViewHolder(root)
    }

    private inner class ServiceBillSettlementAdapter(list: MutableList<MdaServiceSettlementItem>) : BaseRecyclerViewAdapter<MdaServiceSettlementItem, ServiceBillSettlementAdapter.ItemHolder>(list) {

        override fun onCreateViewHolder(parent: ViewGroup, position: Int): ItemHolder {
            return ItemHolder(LayoutInflater.from(parent.context).inflate(R.layout.row_bill_settlement, parent, false))
        }

        override fun onBindViewHolder(holder: ItemHolder, position: Int) {
            val item = getItem(position)

            holder.itemView.tvDate.text = item.SettlementDate.toDate(EIRS_DATE_FORMAT).format("MMM dd, yyyy")
            holder.itemView.tvAmount.text = "${getString(R.string.niara)} ${item.SettlementAmount.format(0)}"
        }

        internal inner class ItemHolder(root: View) : android.support.v7.widget.RecyclerView.ViewHolder(root)
    }
}