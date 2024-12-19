package ng.gov.eirs.mas.erasmpoa.ui.sync

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import android.widget.CompoundButton
import com.google.gson.reflect.TypeToken
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_sync_scratch_card.*
import kotlinx.android.synthetic.main.row_scratch_card_sync.view.*
import ng.gov.eirs.mas.erasmpoa.R
import ng.gov.eirs.mas.erasmpoa.data.GsonProvider
import ng.gov.eirs.mas.erasmpoa.data.model.ScratchCardDenomination
import ng.gov.eirs.mas.erasmpoa.helper.MultiChoiceRecyclerViewAdapter
import ng.gov.eirs.mas.erasmpoa.net.ApiClient
import ng.gov.eirs.mas.erasmpoa.net.exception.NoInternetConnectionException
import ng.gov.eirs.mas.erasmpoa.sync.SyncTracker
import ng.gov.eirs.mas.erasmpoa.ui.base.BaseAppCompatActivity
import ng.gov.eirs.mas.erasmpoa.util.*
import java.net.SocketTimeoutException
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by himanshusoni on 11/04/17.
 */

class ScratchCardSyncActivity : BaseAppCompatActivity() {

    private var mAdapter: ListAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sync_scratch_card)

        setUpToolbar(toolbar)
        setHomeAsUp(true)

        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(activity)

        // val syncTime = SyncTracker.getSyncTime(activity, ScratchCard::class.java)
        val syncTime = 0L
        getDenominationList(syncTime)

        // val syncables: ArrayList<Syncable> = intent.getSerializableExtra("syncable") as ArrayList<Syncable>

        mAdapter = ListAdapter(ArrayList())
        recyclerView.adapter = mAdapter
    }

    private fun getDenominationList(time: Long) {
        val disposable = ApiClient.getClient(this, true)
                .denominations(
                        getToken(),
                        time.toDate().format(SERVER_DATE_FORMAT)
                ).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ response ->
                    mProgressDialog?.dismiss()
                    if (response.success == 1) {
                        val list: ArrayList<ScratchCardDenomination> = GsonProvider.getGson().fromJson(response.response?.data,
                                object : TypeToken<ArrayList<ScratchCardDenomination>>() {}.type)
                        mAdapter?.add(list)
                    } else {
                        val messageU = response.response?.message
                        if (messageU?.isJsonPrimitive == true) {
                            coordinatorLayout.snackBar(messageU.asString)
                        } else {
                            coordinatorLayout.snackBar(R.string.please_try_again)
                        }
                    }
                }, { error ->
                    mProgressDialog?.dismiss()
                    error.printStackTrace()

                    if (error is SocketTimeoutException || error is NoInternetConnectionException) {
                        coordinatorLayout.snackBar(R.string.connection_error)
                    } else {
                        coordinatorLayout.snackBar(R.string.please_try_again)
                    }
                })
        mProgressDialog = ProgressDialog.show(activity, null, getString(R.string.loading))
        SyncTracker.addSync(disposable)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val addItem = menu.add(getString(R.string.save).toUpperCase(Locale.getDefault()))
        addItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_WITH_TEXT or MenuItem.SHOW_AS_ACTION_IF_ROOM)
        addItem.setOnMenuItemClickListener {
            performSync()
            true
        }
        return super.onCreateOptionsMenu(menu)
    }

    private fun performSync() {
        mAdapter?.selectedItems?.let {
            if (it.size == 0) {
                toast("Select at least one item to sync")
                return
            }

            val intent = Intent()
            intent.putExtra("denominations", ArrayList(it))
            setResult(Activity.RESULT_OK, intent)
            finish()
        }
    }

    private inner class ListAdapter(list: ArrayList<ScratchCardDenomination>)
        : MultiChoiceRecyclerViewAdapter<ScratchCardDenomination, ListAdapter.ViewHolder>(list) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.row_scratch_card_sync, parent, false)
            return ViewHolder(v)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = getItem(position)

            holder.itemView.cbDenomination.text = getString(R.string.niara) + item.amount.format(0)
            holder.itemView.tvCount.text = "Total Activated Cards: " + item.totCard.toString()

            holder.itemView.cbDenomination.isChecked = isSelected(position)
        }

        internal inner class ViewHolder(root: View) : RecyclerView.ViewHolder(root), View.OnClickListener, CompoundButton.OnCheckedChangeListener {

            init {
                root.setOnClickListener(this)
            }

            override fun onClick(v: View) {
                setSelected(adapterPosition, !isSelected(adapterPosition))
                notifyDataSetChanged()
            }

            override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
                setSelected(adapterPosition, isChecked)
                notifyDataSetChanged()
            }
        }
    }
}
