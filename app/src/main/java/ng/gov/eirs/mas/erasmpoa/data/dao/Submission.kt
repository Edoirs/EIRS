package ng.gov.eirs.mas.erasmpoa.data.dao

import com.raizlabs.android.dbflow.annotation.Column
import com.raizlabs.android.dbflow.annotation.PrimaryKey
import com.raizlabs.android.dbflow.annotation.Table
import com.raizlabs.android.dbflow.structure.BaseModel
import ng.gov.eirs.mas.erasmpoa.data.DB
import java.io.Serializable

@Table(database = DB::class, allFields = true)
class Submission(
        @PrimaryKey(autoincrement = true)
        var id: Long = 0,

        @Column(defaultValue = "1")
        var synced: Boolean = true,

        var offlineId: String = "",
        var name: String = "",
        var mobile: String = "",
        var address: String = "",
        var assessableIncome: Double = 0.toDouble(),
        var taxAssessed: Double = 0.toDouble(),
        var amountPaid: Double = 0.toDouble(),
        var notes: String = "",
        var menuSection: String = "",
        var group: String = "",
        var subGroup: String = "",
        var category: String = "",

        var priceSheetId: Int = 0,
        var priceSheetAmount: Double = 0.toDouble(),
        var taxPayerTypeId: Int = 0,
        var areaId: Int = 0,
        var paymentType: Int = 0,
        var scratchCard: String = "",

        var submissionTime: Long = 0,
        var submittedBy: Long = 0
) : BaseModel(), Serializable {
    fun isSynced(): Boolean = synced
    override fun toString(): String {
        return "Submission(id=$id, synced=$synced, offlineId='$offlineId', name='$name', mobile='$mobile', address='$address', assessableIncome=$assessableIncome, taxAssessed=$taxAssessed, amountPaid=$amountPaid, notes='$notes', menuSection='$menuSection', group='$group', subGroup='$subGroup', category='$category', priceSheetId=$priceSheetId, priceSheetAmount=$priceSheetAmount, taxPayerTypeId=$taxPayerTypeId, areaId=$areaId, paymentType=$paymentType, scratchCard='$scratchCard', submissionTime=$submissionTime, submittedBy=$submittedBy)"
    }


}