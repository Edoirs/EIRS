package ng.gov.eirs.mas.erasmpoa.sync

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.support.v4.content.LocalBroadcastManager
import com.raizlabs.android.dbflow.sql.language.SQLite
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import ng.gov.eirs.mas.erasmpoa.data.constant.Action
import ng.gov.eirs.mas.erasmpoa.data.dao.Submission
import ng.gov.eirs.mas.erasmpoa.data.dao.Submission_Table
import ng.gov.eirs.mas.erasmpoa.data.model.ScratchCardDenomination
import java.util.*

/**
 * SyncTracker for all classes
 */
object SyncTracker {
    private fun getPreferences(context: Context): SharedPreferences { //PreferenceManager.getDefaultSharedPreferences(context);
        return context.getSharedPreferences("SYNC", Context.MODE_PRIVATE)
    }

    fun saveSyncTime(context: Context, clz: Class<*>, time: Long, amount: Double? = null) {
        //Log.d("SyncTracker", "saveSyncTime: class=" + clz.simpleName + " time=" + DateUtil.getDateForServer(time))
        val amountKey = amount?.toString() ?: ""
        getPreferences(context).edit().putLong("${clz.name}$amountKey", time).apply()
    }

    fun getSyncTime(context: Context, clz: Class<*>, amount: Double? = null): Long {
        val amountKey = amount?.toString() ?: ""
        return getPreferences(context).getLong("${clz.name}$amountKey", 0L)
    }

    fun sendBroadcastUpdateForError(context: Context) {
        LocalBroadcastManager.getInstance(context).sendBroadcast(Intent(Action.SYNC_UPDATE_ERROR))
    }

    fun uploadSyncUpdate(context: Context, action: String) {
        LocalBroadcastManager.getInstance(context).sendBroadcast(Intent(action))
    }

    fun downloadSyncUpdate(context: Context, action: String, total: Int, completed: Boolean) {
        val intent = Intent(action)
        intent.putExtra("completed", completed)
        intent.putExtra("total", total)
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
    }

    fun downloadDenominationSyncUpdate(context: Context, action: String, denomination: ScratchCardDenomination, total: Int, completed: Boolean) {
        val intent = Intent(action)
        intent.putExtra("completed", completed)
        intent.putExtra("denomination", denomination)
        intent.putExtra("total", total)
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
    }

    val isOfflineEligible: Boolean
        get() {
            val yesterdayNight = Calendar.getInstance()
            yesterdayNight.add(Calendar.HOUR_OF_DAY, -yesterdayNight[Calendar.HOUR_OF_DAY])
            yesterdayNight.add(Calendar.MINUTE, -yesterdayNight[Calendar.MINUTE])
            yesterdayNight.add(Calendar.SECOND, -yesterdayNight[Calendar.SECOND])
            val before24Hours = yesterdayNight.timeInMillis
            val unsyncedData = SQLite.selectCountOf().from(Submission::class.java)
                    .where(Submission_Table.synced.eq(false))
                    .and(Submission_Table.submissionTime.lessThan(before24Hours))
                    .longValue()
            return unsyncedData == 0L
        }

    fun canLogout(): Boolean {
        val unsyncedData = SQLite.selectCountOf().from(Submission::class.java)
                .where(Submission_Table.synced.eq(false))
                .longValue()
        return unsyncedData == 0L
    }

    fun deleteAll(context: Context) {
        getPreferences(context).edit().clear().apply()
    }

    private var runningSync: CompositeDisposable? = null

    private fun getRunningSync(): CompositeDisposable {
        return runningSync?.let { it }
                ?: kotlin.run { CompositeDisposable().also { runningSync = it } }
    }

    fun addSync(disposable: Disposable) {
        getRunningSync().add(disposable)
    }

    fun disposeAllSync() {
        if (!getRunningSync().isDisposed) getRunningSync().dispose()
        getRunningSync().clear()
        runningSync = null
    }
}