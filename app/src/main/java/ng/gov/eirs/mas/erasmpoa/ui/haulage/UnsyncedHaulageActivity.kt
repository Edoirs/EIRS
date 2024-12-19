package ng.gov.eirs.mas.erasmpoa.ui.haulage

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.util.DiffUtil
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import com.raizlabs.android.dbflow.annotation.Collate
import com.raizlabs.android.dbflow.rx2.language.RXSQLite
import com.raizlabs.android.dbflow.sql.language.SQLite
import kotlinx.android.synthetic.main.activity_unsynced_haulage.*
import kotlinx.android.synthetic.main.row_unsynced_haulage.view.*
import ng.gov.eirs.mas.erasmpoa.R
import ng.gov.eirs.mas.erasmpoa.data.constant.Action
import ng.gov.eirs.mas.erasmpoa.data.dao.*
import ng.gov.eirs.mas.erasmpoa.data.model.Syncable
import ng.gov.eirs.mas.erasmpoa.helper.BaseRecyclerViewAdapter
import ng.gov.eirs.mas.erasmpoa.sync.SyncTracker
import ng.gov.eirs.mas.erasmpoa.ui.base.BaseAppCompatActivity
import ng.gov.eirs.mas.erasmpoa.ui.sync.SyncService
import ng.gov.eirs.mas.erasmpoa.ui.sync.SyncStatusActivity
import ng.gov.eirs.mas.erasmpoa.util.format
import ng.gov.eirs.mas.erasmpoa.util.gone
import ng.gov.eirs.mas.erasmpoa.util.saveSyncStatus
import java.util.*

class UnsyncedHaulageActivity : BaseAppCompatActivity() {

    private var mAdapter: ListAdapter? = null

    private val mSyncUpdateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (Action.SYNC_UPDATE == intent.action) {
                // LogUtil.d(TAG, "onReceive: Action.SYNC_UPDATE");
                loadData()
//            } else if (Action.SYNC_UPDATE_ERROR == intent.action) {
//                toast("Some error occurred while syncing, please re-sync")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_unsynced_haulage)

        setUpToolbar(toolbar)
        setHomeAsUp(true)

        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(activity)

        ContextCompat.getDrawable(activity, R.drawable.divider_8dp)?.let {
            val dividerItemDecoration = DividerItemDecoration(activity, DividerItemDecoration.VERTICAL)
            dividerItemDecoration.setDrawable(it)
            recyclerView.addItemDecoration(dividerItemDecoration)
        }

        mAdapter = ListAdapter(ArrayList())
        recyclerView.adapter = mAdapter

        loadData()
    }

    private fun loadData() {
        SyncTracker.addSync(
                RXSQLite.rx(SQLite.select()
                        .from(Haulage::class.java)
                        .where(Haulage_Table.synced.eq(false).collate(Collate.NOCASE))
                ).queryList()
                        .doFinally {
                            progressBar.gone()
                            showEmptyView()
                        }
                        .subscribe { newList, t ->
                            mAdapter?.dataSet?.let { oldList ->
                                val diffResult = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
                                    override fun areItemsTheSame(oldPosition: Int, newPosition: Int): Boolean {
                                        return oldList[oldPosition].id == newList[newPosition].id
                                    }

                                    override fun getOldListSize(): Int = oldList.size

                                    override fun getNewListSize(): Int = newList.size

                                    override fun areContentsTheSame(oldPosition: Int, newPosition: Int): Boolean {
                                        return oldList[oldPosition] == newList[newPosition]
                                    }
                                })

                                mAdapter?.clear(notify = false)
                                mAdapter?.add(newList, notify = false)
                                mAdapter?.let { diffResult.dispatchUpdatesTo(it) }
                            }
                        })
    }

    private fun showEmptyView() {
        if (mAdapter?.itemCount == 0) {
            emptyView.visibility = View.VISIBLE
        } else {
            emptyView.visibility = View.GONE
        }
    }

    override fun onStart() {
        super.onStart()
        val intentFilter = IntentFilter()
        intentFilter.addAction(Action.SYNC_UPDATE)
        // intentFilter.addAction(Action.SYNC_UPDATE_ERROR)
        LocalBroadcastManager.getInstance(activity).registerReceiver(mSyncUpdateReceiver, intentFilter)
    }

    override fun onStop() {
        super.onStop()
        LocalBroadcastManager.getInstance(activity).unregisterReceiver(mSyncUpdateReceiver)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
//        val addItem = menu.add(getString(R.string.sync).toUpperCase())
//        addItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_WITH_TEXT or MenuItem.SHOW_AS_ACTION_IF_ROOM)
//        addItem.setOnMenuItemClickListener {
//            performSync()
//            true
//        }
        return super.onCreateOptionsMenu(menu)
    }

    private fun performSync() {
        val poaSyncList = ArrayList<Syncable>()
        poaSyncList.add(Syncable(Haulage::class.java.simpleName, "Haulage Collections", true))
        saveSyncStatus(poaSyncList)

        val serviceIntent = Intent(activity, SyncService::class.java)
        startService(serviceIntent)

        val intent = Intent(activity, SyncStatusActivity::class.java)
        startActivity(intent)
        finish()
    }

    private inner class ListAdapter(list: ArrayList<Haulage>) : BaseRecyclerViewAdapter<Haulage, ListAdapter.ViewHolder>(list) {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.row_unsynced_haulage, parent, false)
            return ViewHolder(v)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = getItem(position)

//            val amount = SpannableString("${getString(R.string.niara)}${item.amountPaid.format(2, true)}")
//            amount.setSpan(StyleSpan(Typeface.BOLD), 0, amount.length, 0)
//
//            val date = SpannableString(Date(item.submissionTime).format("dd MMM, yyyy hh:mm:ss aa"))
//            date.setSpan(StyleSpan(Typeface.ITALIC), 0, date.length, 0)
//
//            val menuSection = item.menuSection
//            val group = item.group
//            val subGroup = item.subGroup
//            val category = item.category
//            val taxPayer = SpannableString(item.name)
//            taxPayer.setSpan(StyleSpan(Typeface.BOLD), 0, taxPayer.length, 0)
//
//            holder.itemView.tvCaptureDetail.text = TextUtils.concat(
//                    amount,
//                    " collected at ",
//                    date,
//                    " for $menuSection, $group, $subGroup, $category from ",
//                    taxPayer
//            )

            val lga = Lga.byAreaId(item.areaId)
            holder.itemView.tvLga.text = lga?.areaName.orEmpty()

            val beat = Beat.byBeatId(item.beatId)
            holder.itemView.tvCaptureDetail.text = beat?.beatName.orEmpty()

            holder.itemView.tvTaxPayerName.text = item.name
            holder.itemView.tvDate.text = Date(item.submissionTime).format("dd MMM, yyyy hh:mm:ss aa")
        }

        internal inner class ViewHolder(root: View) : RecyclerView.ViewHolder(root), View.OnClickListener {

            init {
                root.setOnClickListener(this)
            }

            override fun onClick(v: View) {
                notifyDataSetChanged()
            }

        }
    }
}
