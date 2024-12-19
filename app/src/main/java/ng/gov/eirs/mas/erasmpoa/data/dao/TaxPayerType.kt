package ng.gov.eirs.mas.erasmpoa.data.dao

import com.raizlabs.android.dbflow.annotation.Column
import com.raizlabs.android.dbflow.annotation.PrimaryKey
import com.raizlabs.android.dbflow.annotation.Table
import com.raizlabs.android.dbflow.structure.BaseModel
import ng.gov.eirs.mas.erasmpoa.data.DB
import java.io.Serializable

@Table(database = DB::class, allFields = true)
class TaxPayerType(
        @PrimaryKey(autoincrement = true)
        var id: Long = 0,

        @Column(defaultValue = "1")
        var synced: Boolean = true,

        var taxPayerTypeId: Int = 0,
        var taxPayerType: String = ""
) : BaseModel(), Serializable {
    fun isSynced(): Boolean = synced

    override fun toString(): String = taxPayerType
}