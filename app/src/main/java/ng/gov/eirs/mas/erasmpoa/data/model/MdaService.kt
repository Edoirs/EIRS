package ng.gov.eirs.mas.erasmpoa.data.model

import java.io.Serializable

class MdaService : Serializable {
    var SBSID: Long = 0

    var MDAServiceID: Long = 0
    var MDAServiceName: String = ""

    var TaxYear: String = ""
    var ServiceAmount: Double = 0.toDouble()
    var SettledAmount: Double = 0.toDouble()
}