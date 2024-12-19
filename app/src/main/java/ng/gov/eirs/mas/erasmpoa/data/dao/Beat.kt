package ng.gov.eirs.mas.erasmpoa.data.dao

import com.raizlabs.android.dbflow.annotation.Column
import com.raizlabs.android.dbflow.annotation.PrimaryKey
import com.raizlabs.android.dbflow.annotation.Table
import com.raizlabs.android.dbflow.sql.language.SQLite
import com.raizlabs.android.dbflow.structure.BaseModel
import ng.gov.eirs.mas.erasmpoa.data.DB
import java.io.Serializable

@Table(database = DB::class, allFields = true)
class Beat(
        @PrimaryKey(autoincrement = true)
        var id: Long = 0,

        @Column(defaultValue = "1")
        var synced: Boolean = true,

        var areaId: Int = 0,
        var areaName: String = "",
        var beatId: Int = 0,
        var beatName: String = "",
        var revenueTypeId: Int = 0,
        var revenueTypeName: String = ""

) : BaseModel(), Serializable {
    fun isSynced(): Boolean = synced

    override fun toString(): String = beatName

    companion object
}

class RevenueType(
        var revenueTypeId: Int = 0,
        var revenueTypeName: String = ""
) : Serializable {
    override fun toString(): String = revenueTypeName
}

fun Beat.toLga(): Lga = Lga(
        areaId = this.areaId,
        areaName = this.areaName
)

fun Beat.toRevenueType(): RevenueType = RevenueType(
        revenueTypeId = this.revenueTypeId,
        revenueTypeName = this.revenueTypeName
)

fun Beat.Companion.byBeatId(beatId: Int): Beat? {
    return SQLite.select().from(Beat::class.java)
            .where(Beat_Table.beatId.eq(beatId))
            .querySingle()
}