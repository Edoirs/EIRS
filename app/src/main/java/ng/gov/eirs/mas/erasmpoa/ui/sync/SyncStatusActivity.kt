package ng.gov.eirs.mas.erasmpoa.ui.sync

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.util.DiffUtil
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.activity_sync_status.*
import kotlinx.android.synthetic.main.row_sync_status.view.*
import ng.gov.eirs.mas.erasmpoa.R
import ng.gov.eirs.mas.erasmpoa.data.constant.Action
import ng.gov.eirs.mas.erasmpoa.data.dao.ScratchCard
import ng.gov.eirs.mas.erasmpoa.data.model.Syncable
import ng.gov.eirs.mas.erasmpoa.helper.BaseRecyclerViewAdapter
import ng.gov.eirs.mas.erasmpoa.ui.base.BaseAppCompatActivity
import ng.gov.eirs.mas.erasmpoa.util.*
import java.util.*

/**
 * Created by himanshusoni on 11/04/17.
 */

class SyncStatusActivity : BaseAppCompatActivity() {

    private var mAdapter: ListAdapter? = null
    private val mSyncUpdateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (Action.SYNC_UPDATE == intent.action) {
                // LogUtil.d(TAG, "onReceive: Action.SYNC_UPDATE");
                reloadList()
            } else if (Action.SYNC_UPDATE_ERROR == intent.action) {
                toast("Some error occurred while syncing, please re-sync")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sync_status)

        setUpToolbar(toolbar)
        setHomeAsUp(true)

        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(activity)

        mAdapter = ListAdapter(ArrayList())
        recyclerView.adapter = mAdapter

        reloadList()
    }

    private fun reloadList() {
        val syncables = getSyncStatus()
        if (syncables.size == 0) {
            toast("Sync completed")
            finish()
        } else {
            mAdapter?.let {
                val oldSyncables = it.dataSet

                val diffResult = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
                    override fun areItemsTheSame(oldPosition: Int, newPosition: Int): Boolean {
                        return oldSyncables[oldPosition].clazz == syncables[newPosition].clazz &&
                                oldSyncables[oldPosition].isUpload == syncables[newPosition].isUpload
                    }

                    override fun getOldListSize(): Int = oldSyncables.size

                    override fun getNewListSize(): Int = syncables.size

                    override fun areContentsTheSame(oldPosition: Int, newPosition: Int): Boolean {
                        return oldSyncables[oldPosition] == syncables[newPosition]
                    }
                })

                it.clear(notify = false)
                it.add(syncables, notify = false)
                diffResult.dispatchUpdatesTo(it)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val intentFilter = IntentFilter()
        intentFilter.addAction(Action.SYNC_UPDATE)
        intentFilter.addAction(Action.SYNC_UPDATE_ERROR)
        LocalBroadcastManager.getInstance(activity).registerReceiver(mSyncUpdateReceiver, intentFilter)
        sIsForeground = true
    }

    override fun onStop() {
        super.onStop()
        LocalBroadcastManager.getInstance(activity).unregisterReceiver(mSyncUpdateReceiver)
        sIsForeground = false
    }

    private inner class ListAdapter(list: ArrayList<Syncable>) : BaseRecyclerViewAdapter<Syncable, ListAdapter.ViewHolder>(list) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.row_sync_status, parent, false)
            return ViewHolder(v)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val syncable = getItem(position)

            holder.itemView.tvSyncItem.text = syncable.title
            if (syncable.isUpload) {
                holder.itemView.tvSyncMode.text = getString(R.string.upload)
            } else {
                if (syncable.clazz == ScratchCard::class.java.simpleName) {
                    holder.itemView.tvSyncMode.text = getString(R.string.download_x, "(${getString(R.string.niara)}${syncable.denomination?.amount?.format(0)})")
                } else {
                    holder.itemView.tvSyncMode.text = getString(R.string.download)
                }
            }

            val total = syncable.totalData.toInt()
            val completed = syncable.currentDataStatus.toInt()
            val progress = total - completed

            if (syncable.clazz == ScratchCard::class.java.simpleName) {
                holder.itemView.tvProgress.visible()
                holder.itemView.tvProgress.text = "$completed / $total"
            } else {
                holder.itemView.tvProgress.gone()
            }
            if (syncable.isUpload) {
                holder.itemView.pbStatus.max = total
                holder.itemView.pbStatus.progress = progress
            } else {
                holder.itemView.pbStatus.max = total
                holder.itemView.pbStatus.progress = completed
                // holder.itemView.pbStatus.setIndeterminate(true);
            }
        }

        internal inner class ViewHolder(root: View) : RecyclerView.ViewHolder(root)
    }

    companion object {
        private var sIsForeground: Boolean = false
    }
}
