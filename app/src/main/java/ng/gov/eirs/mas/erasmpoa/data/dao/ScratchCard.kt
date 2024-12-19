package ng.gov.eirs.mas.erasmpoa.data.dao

import com.google.gson.annotations.SerializedName
import com.raizlabs.android.dbflow.annotation.Collate
import com.raizlabs.android.dbflow.annotation.PrimaryKey
import com.raizlabs.android.dbflow.annotation.Table
import com.raizlabs.android.dbflow.sql.language.SQLite
import com.raizlabs.android.dbflow.structure.BaseModel
import ng.gov.eirs.mas.erasmpoa.data.DB
import ng.gov.eirs.mas.erasmpoa.util.md5
import java.io.Serializable

@Table(database = DB::class, allFields = true)
class ScratchCard : BaseModel(), Serializable {
    @PrimaryKey(autoincrement = true)
    var id: Long = 0

    var serialNo: Long = 0
    var amount = 0.0
    var qrCode: String? = null
    var active: Int = 1

    @SerializedName("isConsumed")
    var consumed: Int = 1

    companion object
}

fun ScratchCard.Companion.deleteScratchCard(card: String) {
    SQLite.delete().from(ScratchCard::class.java)
            .where(ScratchCard_Table.qrCode.eq(card.md5()).collate(Collate.NOCASE))
            .execute()
}

