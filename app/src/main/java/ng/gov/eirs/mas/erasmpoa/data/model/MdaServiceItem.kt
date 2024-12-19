package ng.gov.eirs.mas.erasmpoa.data.model

import java.io.Serializable

class MdaServiceItem : Serializable {
    var SBSIID: Long = 0
    var SBSID: Long = 0

    var MDAServiceID: Long = 0
    var MDAServiceName: String = ""
    var MDAServiceItemReferenceNo: String = ""
    var MDAServiceItemID: Long = 0
    var MDAServiceItemName: String = ""

    var ComputationName: String = ""
    var PaymentStatusID: Int = 0
    var PaymentStatusName: String = ""

    var ServiceAmount: Double = 0.toDouble()
    var SettlementAmount: Double = 0.toDouble()
    var PendingAmount: Double = 0.toDouble()
}