package ng.gov.eirs.mas.erasmpoa.sync

import android.content.Context
import com.google.gson.reflect.TypeToken
import com.raizlabs.android.dbflow.sql.language.SQLite
import io.reactivex.schedulers.Schedulers
import ng.gov.eirs.mas.erasmpoa.data.GsonProvider
import ng.gov.eirs.mas.erasmpoa.data.constant.Action
import ng.gov.eirs.mas.erasmpoa.data.dao.Beat
import ng.gov.eirs.mas.erasmpoa.data.dao.Beat_Table
import ng.gov.eirs.mas.erasmpoa.net.ApiClient
import ng.gov.eirs.mas.erasmpoa.util.getToken

class BeatSyncronizer private constructor(private val mContext: Context) {

    private fun sync() {
        val syncTime = SyncTracker.getSyncTime(mContext, Beat::class.java)
        load(syncTime)
    }

    private fun beginTransaction(list: ArrayList<Beat>) {
        list.forEach {
            SQLite.delete(Beat::class.java)
                    .where(Beat_Table.beatId.eq(it.beatId))
                    .execute()
            it.save()
        }
    }

    private fun commit(time: Long, total: Int) {
        SyncTracker.downloadSyncUpdate(mContext, Action.BEAT_DOWNLOAD, total,
                SyncTracker.getSyncTime(mContext, Beat::class.java) != time)
        SyncTracker.saveSyncTime(mContext, Beat::class.java, time)
    }

    private fun load(time: Long) {
        val disposable = ApiClient.getClient(mContext, true)
                .haulageBeatList(mContext.getToken())
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe({ response ->
                    if (response.success == 1) {
                        // if set 1
                        SQLite.delete().from(Beat::class.java).execute()

                        val data = response.response?.data
                        val list = GsonProvider.getGson().fromJson<ArrayList<Beat>>(
                                data,
                                object : TypeToken<ArrayList<Beat>>() {}.type
                        )

                        beginTransaction(list)

                        val total = 0
                        val more = false
                        if (more) {
                            // there are more
                            commit(time, total)
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
        private val TAG = "BeatSyncronizer"

        fun sync(context: Context) {
            BeatSyncronizer(context).sync()
        }
    }
}
