package ng.gov.eirs.mas.erasmpoa.data.model

import java.io.Serializable

class Bill : Serializable {

    var AssessmentID: Long = 0
    var AssessmentNotes: String = ""
    private var AssessmentRefNo: String = ""
    private var AssessmentDate: String = ""
    private var AssessmentAmount: Double = 0.toDouble()

    var ServiceBillID: Long = 0
    private var ServiceBillRefNo: String = ""
    private var ServiceBillDate: String = ""
    private var ServiceBillAmount: Double = 0.toDouble()

    var TaxPayerTypeID: Int = 0
    var TaxPayerTypeName: String = ""
    var TaxPayerID: Long = 0
    var TaxPayerName: String = ""
    var TaxPayerRIN: String = ""

    var SettlementAmount: Double = 0.toDouble()
    var SettlementDueDate: String = ""
    var SettlementStatusID: Int = 0
    var SettlementStatusName: String = ""
    var SettlementDate: String = ""

    var Active: Boolean = true
    var ActiveText: String = ""

    fun isAssessmentBill(): Boolean = AssessmentID != 0L
    fun isServiceBill(): Boolean = ServiceBillID != 0L

    fun getBillDate(): String = if (isAssessmentBill()) AssessmentDate else ServiceBillDate
    fun getBillId(): Long = if (isAssessmentBill()) AssessmentID else ServiceBillID
    fun getBillRef(): String = if (isAssessmentBill()) AssessmentRefNo else ServiceBillRefNo
    fun getBillAmount(): Double = if (isAssessmentBill()) AssessmentAmount else ServiceBillAmount
    fun getOutstandingAmount(): Double = getBillAmount() - SettlementAmount
}