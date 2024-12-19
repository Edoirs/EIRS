package ng.gov.eirs.mas.erasmpoa.ui.sync

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import android.widget.CompoundButton
import com.raizlabs.android.dbflow.sql.language.SQLite
import kotlinx.android.synthetic.main.activity_sync.*
import kotlinx.android.synthetic.main.row_sync.view.*
import ng.gov.eirs.mas.erasmpoa.R
import ng.gov.eirs.mas.erasmpoa.data.constant.RequestCode
import ng.gov.eirs.mas.erasmpoa.data.dao.*
import ng.gov.eirs.mas.erasmpoa.data.model.ScratchCardDenomination
import ng.gov.eirs.mas.erasmpoa.data.model.Syncable
import ng.gov.eirs.mas.erasmpoa.helper.MultiChoiceRecyclerViewAdapter
import ng.gov.eirs.mas.erasmpoa.ui.base.BaseAppCompatActivity
import ng.gov.eirs.mas.erasmpoa.util.format
import ng.gov.eirs.mas.erasmpoa.util.saveSyncStatus
import ng.gov.eirs.mas.erasmpoa.util.toast
import java.util.*

class SyncActivity : BaseAppCompatActivity() {

    private var mAdapter: ListAdapter? = null
    private var denominations: ArrayList<ScratchCardDenomination> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sync)

        setUpToolbar(toolbar)
        setHomeAsUp(true)

        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(activity)

        val syncables = getSyncables(true)

        mAdapter = ListAdapter(syncables)
        recyclerView.adapter = mAdapter

        cbSelectAll.setOnClickListener {
            if (cbSelectAll.isChecked) {
                mAdapter?.selectAll()
                if (denominations.isEmpty()) {
                    val scSyncIntent = Intent(activity, ScratchCardSyncActivity::class.java)
                    startActivityForResult(scSyncIntent, RequestCode.EDIT)
                }
            } else {
                mAdapter?.deselectAll()
                denominations.clear()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val addItem = menu.add(getString(R.string.sync).toUpperCase(Locale.getDefault()))
        addItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_WITH_TEXT or MenuItem.SHOW_AS_ACTION_IF_ROOM)
        addItem.setOnMenuItemClickListener {
            performSync()
            true
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == RequestCode.EDIT) {
            if (resultCode == Activity.RESULT_OK) {
                denominations = data?.getSerializableExtra("denominations") as ArrayList<ScratchCardDenomination>
                mAdapter?.notifyDataSetChanged()
            } else if (resultCode == Activity.RESULT_CANCELED) {
                mAdapter?.dataSet?.forEachIndexed { index, syncable ->
                    if (syncable.clazz == ScratchCard::class.java.simpleName) {
                        if (mAdapter?.isSelected(index) == true) {
                            mAdapter?.toggleSelection(index)
                        }
                    }
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun performSync() {
        mAdapter?.selectedItems?.let {
            if (it.size == 0) {
                toast("Select at least one item to sync")
                return
            }

            it.forEach { syncable ->
                if (syncable.clazz == ScratchCard::class.java.simpleName) {
                    denominations.forEach { denomination ->
                        if (syncable.denomination == null) {
                            syncable.denomination = denomination
                        } else {
                            val copy = Syncable(syncable.clazz, syncable.title, syncable.isUpload)
                            copy.denomination = denomination
                            it.add(copy)
                        }
                    }
                }
            }
            saveSyncStatus(it)

            val serviceIntent = Intent(activity, SyncService::class.java)
            startService(serviceIntent)

            val intent = Intent(activity, SyncStatusActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private inner class ListAdapter(list: ArrayList<Syncable>) : MultiChoiceRecyclerViewAdapter<Syncable, ListAdapter.ViewHolder>(list) {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.row_sync, parent, false)
            return ViewHolder(v)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val syncable = getItem(position)

            holder.itemView.cbSyncItem.text = syncable.title

            if (syncable.isUpload) {
                if (syncable.clazz == Submission::class.java.simpleName) {
                    val count = SQLite.selectCountOf().from(Submission::class.java)
                            .where(Submission_Table.synced.eq(false))
                            .longValue()
                    syncable.totalData = count
                } else if (syncable.clazz == Haulage::class.java.simpleName) {
                    val count = SQLite.selectCountOf().from(Haulage::class.java)
                            .where(Haulage_Table.synced.eq(false))
                            .longValue()
                    syncable.totalData = count
                }
                holder.itemView.tvSyncMode.text = getString(R.string.upload_x, syncable.totalData.toString())
                if (syncable.totalData > 0) {
                    setSelected(position, true)
                }
            } else {
                if (syncable.clazz == ScratchCard::class.java.simpleName) {
                    var denominationDesc = ""
                    if (denominations.isNotEmpty()) {
                        denominationDesc += "("
                        for ((index, it) in denominations.withIndex()) {
                            denominationDesc += getString(R.string.niara) + it.amount.format(0)
                            if (index != denominations.size - 1) {
                                denominationDesc += ", "
                            }
                        }
                        denominationDesc += ")"
                    }
                    holder.itemView.tvSyncMode.text = getString(R.string.download_x, denominationDesc)
                } else {
                    holder.itemView.tvSyncMode.text = getString(R.string.download)
                }
            }

            holder.itemView.cbSyncItem.isChecked = isSelected(position)
        }

        internal inner class ViewHolder(root: View) : RecyclerView.ViewHolder(root), View.OnClickListener, CompoundButton.OnCheckedChangeListener {

            init {
                root.setOnClickListener(this)
            }

            override fun onClick(v: View) {
                if (getItem(adapterPosition).clazz == ScratchCard::class.java.simpleName) {
                    if (!isSelected(adapterPosition)) {
                        val scSyncIntent = Intent(activity, ScratchCardSyncActivity::class.java)
                        startActivityForResult(scSyncIntent, RequestCode.EDIT)
                    } else {
                        denominations.clear()
                    }
                }
                setSelected(adapterPosition, !isSelected(adapterPosition))
                notifyDataSetChanged()
            }

            override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
                setSelected(adapterPosition, isChecked)
                notifyDataSetChanged()
            }
        }
    }

    companion object {

        fun startSyncSilently(context: Context) {
            context.saveSyncStatus(getSyncables(false))

            val serviceIntent = Intent(context, SyncService::class.java)
            context.startService(serviceIntent)
        }

        fun getSyncables(withScratchCard: Boolean): ArrayList<Syncable> {
            val syncables = ArrayList<Syncable>()
//            syncables.add(Syncable(Submission::class.java.simpleName, "Captured MPoA", true))
//            syncables.add(Syncable(Haulage::class.java.simpleName, "Captured Haulage", true))

            syncables.add(Syncable(PriceSheet::class.java.simpleName, "Price Sheet", false))
            syncables.add(Syncable(TaxPayerType::class.java.simpleName, "Tax Payer Type", false))
            syncables.add(Syncable(Lga::class.java.simpleName, "Lga", false))
            syncables.add(Syncable(Beat::class.java.simpleName, "Beats", false))
//            if (withScratchCard) {
//                syncables.add(Syncable(ScratchCard::class.java.simpleName, "Scratch Card", false))
//            }
            return syncables
        }
    }
}
