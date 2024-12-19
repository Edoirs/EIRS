package ng.gov.eirs.mas.erasmpoa.data.model

import java.io.Serializable
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

data class CollectionTarget(
    val targetAmount: Double,
    val achievedAmount: Double
) : Serializable {

}

fun CollectionTarget.getRemainingAmount(unsyncedAmount: Double): Double {
    return max(targetAmount - achievedAmount - unsyncedAmount, 0.0)
}

fun CollectionTarget.getTargetAchievePercentage(unsyncedAmount: Double): Int {
    val actualAchievedAmount = achievedAmount + unsyncedAmount
    val percentage = actualAchievedAmount * 100 / targetAmount
    return min(percentage, 100.0).roundToInt()
}