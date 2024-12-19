package ng.gov.eirs.mas.erasmpoa.data.dao

import com.raizlabs.android.dbflow.annotation.Column
import com.raizlabs.android.dbflow.annotation.PrimaryKey
import com.raizlabs.android.dbflow.annotation.Table
import com.raizlabs.android.dbflow.structure.BaseModel
import ng.gov.eirs.mas.erasmpoa.data.DB
import java.io.Serializable

@Table(database = DB::class, allFields = true)
class PriceSheet(
        @PrimaryKey(autoincrement = true)
        var id: Long = 0,

        @Column(defaultValue = "1")
        var synced: Boolean = true,

        var priceSheetAggregateId: Int = 0,
        var AssessmentRuleID: Int = 0,
        var AssessmentRuleCode: String = "",
        var AssessmentRuleName: String = "",
        var AssessmentRuleAmount: Double = 0.toDouble(),
        var MenuSection: String = "",
        var GroupName: String = "",
        var SubGroupName: String = "",
        var CategoryName: String = "",
        var Price: Double = 0.toDouble()
) : BaseModel(), Serializable {
    fun isSynced(): Boolean = synced
}