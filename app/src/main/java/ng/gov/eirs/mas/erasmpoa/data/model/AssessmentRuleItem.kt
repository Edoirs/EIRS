package ng.gov.eirs.mas.erasmpoa.data.model

import java.io.Serializable

class AssessmentRuleItem : Serializable {
    var AAIID: Long = 0
    var AARID: Long = 0

    var AssessmentRuleID: Long = 0
    var AssessmentRuleName: String = ""
    var AssessmentItemReferenceNo: String = ""
    var AssessmentItemID: Long = 0
    var AssessmentItemName: String = ""

    var ComputationName: String = ""
    var PaymentStatusID: Int = 0
    var PaymentStatusName: String = ""

    var TaxAmount: Double = 0.toDouble()
    var SettlementAmount: Double = 0.toDouble()
    var PendingAmount: Double = 0.toDouble()
}