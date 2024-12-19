package ng.gov.eirs.mas.erasmpoa.data.model

import java.io.Serializable

class MdaServiceSettlementItem : Serializable {
    var SettlementID: Long = 0
    var SettlementRefNo: String = ""
    var SettlementAmount: Double = 0.toDouble()
    var SettlementMethodID: Int = 0
    var SettlementMethodName: String = ""
    var SettlementStatusID: Int = 0
    var SettlementStatusName: String = ""
    var SettlementDate: String = ""
    var SettlementNotes: String = ""

    var TaxPayerTypeID: Long = 0
    var TaxPayerTypeName: String = ""
    var TaxPayerID: Long = 0
    var TaxPayerName: String = ""
    var TaxPayerRIN: String = ""


    var ASID: Long = 0
    var ASRefNo: String = ""
    var ASAmount: Double = 0.toDouble()
}