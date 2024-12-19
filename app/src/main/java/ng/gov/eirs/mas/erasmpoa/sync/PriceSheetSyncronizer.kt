package ng.gov.eirs.mas.erasmpoa.sync

import android.content.Context
import com.google.gson.JsonArray
import com.google.gson.reflect.TypeToken
import com.raizlabs.android.dbflow.sql.language.SQLite
import io.reactivex.schedulers.Schedulers
import ng.gov.eirs.mas.erasmpoa.data.ConfigData
import ng.gov.eirs.mas.erasmpoa.data.GsonProvider
import ng.gov.eirs.mas.erasmpoa.data.constant.Action
import ng.gov.eirs.mas.erasmpoa.data.dao.PriceSheet
import ng.gov.eirs.mas.erasmpoa.data.dao.PriceSheet_Table
import ng.gov.eirs.mas.erasmpoa.net.ApiClient
import ng.gov.eirs.mas.erasmpoa.util.getMenuSection
import ng.gov.eirs.mas.erasmpoa.util.getToken
import java.util.*

class PriceSheetSyncronizer private constructor(private val mContext: Context) {

    private val mTransactionList: ArrayList<PriceSheet> = ArrayList()

    private fun sync() {
        val syncTime = SyncTracker.getSyncTime(mContext, PriceSheet::class.java)

        load(1, syncTime)
    }

    private fun beginTransaction(list: ArrayList<PriceSheet>) {
        mTransactionList.addAll(list)
    }

    private fun commit(time: Long, total: Int) {
        val iterator = ArrayList(mTransactionList).iterator()
        mTransactionList.clear()
        Thread {
            while (iterator.hasNext()) {
                val item = iterator.next()
                SQLite.delete(PriceSheet::class.java)
                        .where(PriceSheet_Table.priceSheetAggregateId.eq(item.priceSheetAggregateId))
                        .execute()
                item.save()
                iterator.remove()
            }
            SyncTracker.downloadSyncUpdate(mContext, Action.PRICE_SHEET_DOWNLOAD, total,
                    SyncTracker.getSyncTime(mContext, PriceSheet::class.java) != time)
            SyncTracker.saveSyncTime(mContext, PriceSheet::class.java, time)
        }.start()

        // LogUtil.d(TAG, "commit: Capture profile sync completed");
    }

    private fun load(page: Int, time: Long) {
        val menuSections = JsonArray()
        mContext.getMenuSection()?.forEach {
            menuSections.add(it.menuSection)
        }
        val disposable = ApiClient.getClient(mContext, true)
                .psList(
                        mContext.getToken(),
                        menuSections.toString(),
                        ConfigData.PER_SET,
                        page
                ).subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe({ response ->
                    if (response.success == 1) {
                        if (page == 1) {
                            // delete all data before sync so that old data can be deleted.
                            SQLite.delete(PriceSheet::class.java)
                                    .execute()
                        }
                        val data = response.response?.data?.asJsonObject
                        val list = GsonProvider.getGson().fromJson<ArrayList<PriceSheet>>(
                                data?.get("list"),
                                object : TypeToken<ArrayList<PriceSheet>>() {}.type
                        )
                        beginTransaction(list)

                        val total = data?.get("total")?.asInt ?: 0
                        val more = (page * ConfigData.PER_SET) < total
                        if (more) {
                            // there are more
                            commit(time, total)
                            load(page + 1, time)
                        } else {
                            // start another sync
                            commit(response.time * 1000L, total)
                        }
                    } else if (response.success == 0) {
                        commit(response.time * 1000L, 0)
                    } else {
                        // else we have faced any error
                        SyncTracker.sendBroadcastUpdateForError(mContext)
                    }
                }, { error ->
                    SyncTracker.sendBroadcastUpdateForError(mContext)
                    error.printStackTrace()
                })
        SyncTracker.addSync(disposable)
    }

    companion object {
        private val TAG = "PriceSheetSyncronizer"

        fun sync(context: Context) {
            PriceSheetSyncronizer(context).sync()
        }
    }
}
