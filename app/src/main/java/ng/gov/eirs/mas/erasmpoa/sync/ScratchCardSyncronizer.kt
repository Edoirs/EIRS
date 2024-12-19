package ng.gov.eirs.mas.erasmpoa.sync

import android.content.Context
import com.raizlabs.android.dbflow.sql.language.SQLite
import io.reactivex.schedulers.Schedulers
import ng.gov.eirs.mas.erasmpoa.data.ConfigData
import ng.gov.eirs.mas.erasmpoa.data.GsonProvider
import ng.gov.eirs.mas.erasmpoa.data.constant.Action
import ng.gov.eirs.mas.erasmpoa.data.dao.ScratchCard
import ng.gov.eirs.mas.erasmpoa.data.dao.ScratchCard_Table
import ng.gov.eirs.mas.erasmpoa.data.model.ScratchCardDenomination
import ng.gov.eirs.mas.erasmpoa.data.model.User
import ng.gov.eirs.mas.erasmpoa.net.ApiClient
import ng.gov.eirs.mas.erasmpoa.util.*
import java.util.*

class ScratchCardSyncronizer private constructor(private val mContext: Context, private val denomination: ScratchCardDenomination) {

    private val mUser: User? = mContext.getUser()

    private fun sync() {
        val syncTime = SyncTracker.getSyncTime(mContext, ScratchCard::class.java, denomination.amount)
        load(1, syncTime)

//        SyncTracker.addSync(RXSQLite.rx(SQLite.delete().from(ScratchCard::class.java))
//                .executeUpdateDelete()
//                .subscribe { _, t2 ->
//                    if (t2 == null) {
//
//                    } else {
//                        SyncTracker.sendBroadcastUpdateForError(mContext)
//                    }
//                })
    }

    private fun beginTransaction(list: ArrayList<ScratchCard>) {
        list.forEach {
            SQLite.delete().from(ScratchCard::class.java)
                    .where(ScratchCard_Table.qrCode.eq(it.qrCode))
            if (it.consumed != 1) {
                it.save()
            }
        }
    }

    private fun commit(time: Long, total: Int) {
        SyncTracker.downloadDenominationSyncUpdate(
                mContext,
                Action.SCRATCH_CARD_DOWNLOAD,
                denomination,
                total,
                SyncTracker.getSyncTime(mContext, ScratchCard::class.java, denomination.amount) != time
        )
        SyncTracker.saveSyncTime(mContext, ScratchCard::class.java, time, denomination.amount)
    }

    private fun load(set: Int, time: Long) {
        val oldYear = Calendar.getInstance().apply {
            set(Calendar.YEAR, 2017)
        }.timeInMillis
        val onlyActivated = if (time < oldYear) 1 else 0

        SyncTracker.addSync(ApiClient.getClient(mContext, true)
                .scratchCards(
                        mContext.getToken(),
                        set,
                        ConfigData.PER_SET,
                        onlyActivated,
                        time.toDate().format(SERVER_DATE_FORMAT),
                        denomination.amount
                ).subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe({ response ->
                    if (response.success == 1) {
                        val responseData = response.response
                        val data = responseData?.data?.asJsonArray

                        val list = ArrayList<ScratchCard>()
                        for (i in 0 until (data?.size() ?: 0)) {
                            val item = GsonProvider.getGson().fromJson(data?.get(i), ScratchCard::class.java)
                            list.add(item)
                        }
                        beginTransaction(list)

                        val total = responseData?.total?.asInt ?: 0
                        val more = responseData?.more?.asBoolean ?: false
                        if (more) {
                            // there are more
                            commit(time, total)
                            load(set + 1, time)
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
                }))
    }

    companion object {
        fun sync(context: Context, denomination: ScratchCardDenomination) {
            ScratchCardSyncronizer(context, denomination).sync()
        }
    }
}
