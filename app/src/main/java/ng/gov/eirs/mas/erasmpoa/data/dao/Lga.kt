package ng.gov.eirs.mas.erasmpoa.data.dao

import com.google.gson.annotations.SerializedName
import com.raizlabs.android.dbflow.annotation.Column
import com.raizlabs.android.dbflow.annotation.PrimaryKey
import com.raizlabs.android.dbflow.annotation.Table
import com.raizlabs.android.dbflow.sql.language.SQLite
import com.raizlabs.android.dbflow.structure.BaseModel
import ng.gov.eirs.mas.erasmpoa.data.DB
import java.io.Serializable
import java.util.*

@Table(database = DB::class, allFields = true)
class Lga(
        @PrimaryKey(autoincrement = true)
        var id: Long = 0,

        @Column(defaultValue = "1")
        var synced: Boolean = true,

        var areaId: Int = 0,
        var areaName: String = "",
        var stateId: Int = 0,

        @SerializedName("isSgActive")
        var sgActive: Int = 0,
        var suffix: String = "",
        var createDt: Date = Date()

) : BaseModel(), Serializable {
    fun isSynced(): Boolean = synced

    override fun toString(): String = areaName

    companion object
}

fun Lga.Companion.byAreaId(areaId: Int): Lga? {
    return SQLite.select().from(Lga::class.java)
            .where(Lga_Table.areaId.eq(areaId))
            .querySingle()
}