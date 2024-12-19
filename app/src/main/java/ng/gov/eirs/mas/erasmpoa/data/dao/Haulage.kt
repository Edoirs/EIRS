package ng.gov.eirs.mas.erasmpoa.data.dao

import com.raizlabs.android.dbflow.annotation.Column
import com.raizlabs.android.dbflow.annotation.PrimaryKey
import com.raizlabs.android.dbflow.annotation.Table
import com.raizlabs.android.dbflow.structure.BaseModel
import ng.gov.eirs.mas.erasmpoa.data.DB
import java.io.Serializable

@Table(database = DB::class, allFields = true)
class Haulage(
        @PrimaryKey(autoincrement = true)
        var id: Long = 0,

        @Column(defaultValue = "1")
        var synced: Boolean = true,

        var offlineId: String = "",

        var areaId: Int = 0,
        var revenueTypeId: Int = 0,
        var beatId: Int = 0,

        var taxPayerTypeId: Int = 0,
        var name: String = "",
        var mobile: String = "",
        var vehicleRegNo: String = "",

        var submissionTime: Long = 0,
        var submittedBy: Long = 0
) : BaseModel(), Serializable {
    fun isSynced(): Boolean = synced
}