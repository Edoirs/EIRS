package ng.gov.eirs.mas.erasmpoa.data.model

import java.io.Serializable

class AssessmentRule : Serializable {
    var AARID: Long = 0

    var AssetTypeID: Int = 0
    var AssetTypeName: String = ""
    var AssetID: Long = 0
    var AssetRIN: String = ""

    var ProfileID: Long = 0
    var ProfileDescription: String = ""

    var AssessmentRuleID: Long = 0
    var AssessmentRuleName: String = ""
    var TaxYear: String = ""

    var AssessmentRuleAmount: Double = 0.toDouble()
    var SettledAmount: Double = 0.toDouble()
}