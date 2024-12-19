package ng.gov.eirs.mas.erasmpoa.sync

import android.content.Context
import com.raizlabs.android.dbflow.sql.language.SQLite
import io.reactivex.schedulers.Schedulers
import ng.gov.eirs.mas.erasmpoa.data.constant.Action
import ng.gov.eirs.mas.erasmpoa.data.dao.Haulage
import ng.gov.eirs.mas.erasmpoa.data.dao.Haulage_Table
import ng.gov.eirs.mas.erasmpoa.net.ApiClient
import ng.gov.eirs.mas.erasmpoa.util.getToken

class HaulageSyncronizer private constructor(private val mContext: Context) {

    private fun sync() {
        uploadOfflineOrders()
    }

    private fun uploadOfflineOrders() {
        val queryAsList = SQLite.select().from(Haulage::class.java)
                .where(Haulage_Table.synced.eq(false))
                .queryList()
        for (c in queryAsList) {
            addOffline(c)
        }
    }

    private fun addOffline(haulage: Haulage) {
        SyncTracker.addSync(ApiClient.getClient(mContext, true).haulageSubmissionAdd(
                mContext.getToken(),
                haulage.areaId,
                haulage.revenueTypeId,
                haulage.beatId,
                haulage.vehicleRegNo,
                haulage.taxPayerTypeId,
                haulage.name,
                haulage.mobile,
                haulage.offlineId
        ).subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe({ response ->
                    //Crashlytics.log(response.response.data.toString())
                    if (response.success == 1) {
                        haulage.delete()
                        SyncTracker.uploadSyncUpdate(mContext, Action.HAULAGE_UPLOAD)
                    } else {
                        if (response.response?.data?.asString.equals("DATA_EXISTED")) {
                            SyncTracker.uploadSyncUpdate(mContext, Action.HAULAGE_UPLOAD)
                            haulage.delete()
                        } else {
                            SyncTracker.sendBroadcastUpdateForError(mContext)
                        }
                    }
                }, {
                    //Crashlytics.logException(IllegalStateException(it))
                    SyncTracker.sendBroadcastUpdateForError(mContext)
                    it.printStackTrace()
                }))
    }

    companion object {

        private val lock = Any()
        @Volatile
        private var instance: HaulageSyncronizer? = null

        private fun getInstance(context: Context): HaulageSyncronizer {
            var r = instance
            if (r == null) {
                synchronized(lock) {
                    // While we were waiting for the lock, another
                    r = instance        // thread may have instantiated the object.
                    if (r == null) {
                        r = HaulageSyncronizer(context)
                        instance = r
                    }
                }
            }
            return r!!
        }

        fun sync(context: Context) {
            getInstance(context).sync()
        }

        private val TAG = "HaulageSyncronizer"
    }
}
