package ng.gov.eirs.mas.erasmpoa.sync

import android.content.Context
import com.raizlabs.android.dbflow.sql.language.Method
import com.raizlabs.android.dbflow.sql.language.SQLite
import io.reactivex.schedulers.Schedulers
import ng.gov.eirs.mas.erasmpoa.data.constant.Action
import ng.gov.eirs.mas.erasmpoa.data.constant.SettlementMethod
import ng.gov.eirs.mas.erasmpoa.data.constant.SubmissionType
import ng.gov.eirs.mas.erasmpoa.data.dao.Submission
import ng.gov.eirs.mas.erasmpoa.data.dao.Submission_Table
import ng.gov.eirs.mas.erasmpoa.net.SpacePointeApiClient
import ng.gov.eirs.mas.erasmpoa.util.format
import ng.gov.eirs.mas.erasmpoa.util.getToken

class SubmissionSyncronizer private constructor(private val mContext: Context) {

    private fun sync() {
        startUploadCapture()
    }

    private fun startUploadCapture() {
        // if (there is data to sync then only call API and start upload )
        val cursor = SQLite.select(Method.sum(Submission_Table.amountPaid).`as`("totalAmount"),
                Method.count().`as`("totalCount"))
                .from(Submission::class.java)
                .where(Submission_Table.synced.eq(false))
                .query()
        var totalCount = 0
        var totalAmount = 0.toDouble()

        if (cursor != null && cursor.moveToFirst()) {
            totalCount = cursor.getIntOrDefault(cursor.getColumnIndex("totalCount"))
            totalAmount = cursor.getDoubleOrDefault(cursor.getColumnIndex("totalAmount"))
            cursor.close()
        }

        if (totalCount == 0) {
            SyncTracker.uploadSyncUpdate(mContext, Action.SUBMISSION_UPLOAD)
            return
        }

        uploadOfflineOrders()

//        val disposable = ApiClient.getClient(mContext)
//                .saveEmployeeActivity(mUser.organizationId, mUser.parentOrganizationId, mUser.employeeId,
//                        totalCount, totalAmount)
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe({ response ->
//                    if (response.success == 1) {
//                        val data = response.response.data?.asString
//                        uploadOfflineOrders(data)
//                    } else {
//                        SyncTracker.sendBroadcastUpdateForError(mContext)
//                    }
//                }, { error ->
//                    error.printStackTrace()
//                    SyncTracker.sendBroadcastUpdateForError(mContext)
//                })
//        SyncTracker.addSync(disposable)
    }

    private fun uploadOfflineOrders(syncId: String? = null) {
        val queryAsList = SQLite.select().from(Submission::class.java)
                .where(Submission_Table.synced.eq(false))
                .queryList()
        for (c in queryAsList) {
            addOffline(c, syncId)
        }
    }

    private fun addOffline(submission: Submission, syncId: String? = null) {
        val scratchCard = submission.scratchCard
//        val scratchCardJson = JsonParser().parse(scratchCard)
//        if (scratchCardJson.isJsonArray) {
//        }
        SyncTracker.addSync(SpacePointeApiClient.getClient(mContext).saveSubmission(
                mContext.getToken(),
                submission.menuSection,
                submission.group,
                submission.subGroup,
                submission.category,
                submission.priceSheetId,
                submission.priceSheetAmount.format(2, false),
                submission.taxPayerTypeId,
                submission.name,
                submission.mobile,
                submission.areaId,
                submission.address,
                submission.assessableIncome.format(2, false),
                submission.taxAssessed.format(2, false),
                submission.amountPaid.format(2, false),
                submission.notes,
                SettlementMethod.SCRATCH_CARD,
                scratchCard,
                SubmissionType.OFFLINE,
                submission.offlineId
        ).subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe({ response ->
                    //Crashlytics.log(response.response.data.toString())
                    if (response.success == 1) {
                        submission.delete()
                        SyncTracker.uploadSyncUpdate(mContext, Action.SUBMISSION_UPLOAD)
                    } else {
                        if (response.response?.message?.asJsonObject?.get("response")?.asString.equals("ALREADY_SYNCED")) {
                            submission.delete()
                            SyncTracker.uploadSyncUpdate(mContext, Action.SUBMISSION_UPLOAD)
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
        private var instance: SubmissionSyncronizer? = null

        private fun getInstance(context: Context): SubmissionSyncronizer {
            var r = instance
            if (r == null) {
                synchronized(lock) {
                    // While we were waiting for the lock, another
                    r = instance        // thread may have instantiated the object.
                    if (r == null) {
                        r = SubmissionSyncronizer(context)
                        instance = r
                    }
                }
            }
            return r!!
        }

        fun sync(context: Context) {
            getInstance(context).sync()
        }

        private val TAG = "SubmissionSyncronizer"

    }
}
