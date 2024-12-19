package ng.gov.eirs.mas.erasmpoa.sync

import android.content.Context
import com.google.gson.reflect.TypeToken
import com.raizlabs.android.dbflow.sql.language.SQLite
import io.reactivex.schedulers.Schedulers
import ng.gov.eirs.mas.erasmpoa.data.GsonProvider
import ng.gov.eirs.mas.erasmpoa.data.constant.Action
import ng.gov.eirs.mas.erasmpoa.data.dao.Lga
import ng.gov.eirs.mas.erasmpoa.data.dao.Lga_Table
import ng.gov.eirs.mas.erasmpoa.net.ApiClient
import ng.gov.eirs.mas.erasmpoa.util.getToken

class LgaSyncronizer private constructor(private val mContext: Context) {

    private fun sync() {
        val syncTime = SyncTracker.getSyncTime(mContext, Lga::class.java)
        load(syncTime)
    }

    private fun beginTransaction(list: ArrayList<Lga>) {
        list.forEach {
            SQLite.delete(Lga::class.java)
                    .where(Lga_Table.areaId.eq(it.areaId))
                    .execute()
            it.save()
        }
    }

    private fun commit(time: Long, total: Int) {
        SyncTracker.downloadSyncUpdate(mContext, Action.LGA_DOWNLOAD, total,
                SyncTracker.getSyncTime(mContext, Lga::class.java) != time)
        SyncTracker.saveSyncTime(mContext, Lga::class.java, time)
    }

    private fun load(time: Long) {
        val disposable = ApiClient.getClient(mContext, true)
                .lgaList(mContext.getToken())
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe({ response ->
                    if (response.success == 1) {
                        val data = response.response?.data
                        val list = GsonProvider.getGson().fromJson<ArrayList<Lga>>(
                                data,
                                object : TypeToken<ArrayList<Lga>>() {}.type
                        )

                        beginTransaction(list)

                        val total = 0
                        val more = false
                        if (more) {
                            // there are more
                            commit(time * 1000L, total)
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
        private val TAG = "LgaSyncronizer"

        fun sync(context: Context) {
            LgaSyncronizer(context).sync()
        }
    }
}
