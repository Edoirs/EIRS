package ng.gov.eirs.mas.erasmpoa.sync

import android.content.Context
import com.google.gson.reflect.TypeToken
import com.raizlabs.android.dbflow.sql.language.SQLite
import io.reactivex.schedulers.Schedulers
import ng.gov.eirs.mas.erasmpoa.data.GsonProvider
import ng.gov.eirs.mas.erasmpoa.data.constant.Action
import ng.gov.eirs.mas.erasmpoa.data.dao.TaxPayerType
import ng.gov.eirs.mas.erasmpoa.data.dao.TaxPayerType_Table
import ng.gov.eirs.mas.erasmpoa.net.ApiClient
import ng.gov.eirs.mas.erasmpoa.util.getToken

class TaxPayerTypeSyncronizer private constructor(private val mContext: Context) {

    private val mTransactionList: ArrayList<TaxPayerType> = ArrayList()

    private fun sync() {
        val syncTime = SyncTracker.getSyncTime(mContext, TaxPayerType::class.java)
        load(syncTime)
    }

    private fun beginTransaction(list: ArrayList<TaxPayerType>) {
        mTransactionList.addAll(list)
    }

    private fun commit(time: Long, total: Int) {
        val iterator = ArrayList(mTransactionList).iterator()
        mTransactionList.clear()
        Thread {
            while (iterator.hasNext()) {
                val item = iterator.next()
                SQLite.delete(TaxPayerType::class.java)
                        .where(TaxPayerType_Table.taxPayerTypeId.eq(item.taxPayerTypeId))
                        .execute()
                item.save()
                iterator.remove()
            }
            SyncTracker.downloadSyncUpdate(mContext, Action.TAX_PAYER_TYPE_DOWNLOAD, total,
                    SyncTracker.getSyncTime(mContext, TaxPayerType::class.java) != time)
            SyncTracker.saveSyncTime(mContext, TaxPayerType::class.java, time)
        }.start()

        // LogUtil.d(TAG, "commit: Capture profile sync completed");
    }

    private fun load(time: Long) {
        val disposable = ApiClient.getClient(mContext, true)
                .taxPayerTypeList(mContext.getToken())
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe({ response ->
                    if (response.success == 1) {
                        val data = response.response?.data
                        val list = GsonProvider.getGson().fromJson<ArrayList<TaxPayerType>>(
                                data,
                                object : TypeToken<ArrayList<TaxPayerType>>() {}.type
                        )
                        beginTransaction(list)

                        val more = false
                        if (more) {
                            // there are more
                            commit(response.time * 1000L, 0)
                        } else {
                            // start another sync
                            commit(response.time * 1000L, 0)
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
        private val TAG = "TaxPayerTypeSyncronizer"

        fun sync(context: Context) {
            TaxPayerTypeSyncronizer(context).sync()
        }
    }
}
