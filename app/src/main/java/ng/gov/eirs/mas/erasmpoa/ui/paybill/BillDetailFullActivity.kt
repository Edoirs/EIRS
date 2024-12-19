package ng.gov.eirs.mas.erasmpoa.ui.paybill

import android.os.Bundle
import kotlinx.android.synthetic.main.activity_bill_detail_full.*
import ng.gov.eirs.mas.erasmpoa.R
import ng.gov.eirs.mas.erasmpoa.data.model.Bill
import ng.gov.eirs.mas.erasmpoa.ui.base.BaseAppCompatActivity
import ng.gov.eirs.mas.erasmpoa.util.EIRS_DATE_FORMAT
import ng.gov.eirs.mas.erasmpoa.util.format
import ng.gov.eirs.mas.erasmpoa.util.toDate

class BillDetailFullActivity : BaseAppCompatActivity() {

    private var mBill: Bill? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bill_detail_full)

        setUpToolbar(toolbar)
        setHomeAsUp(true)

        mBill = intent.getSerializableExtra("bill") as Bill?

        setValues()
    }

    private fun setValues() {
        tvBillRef.text = mBill?.getBillRef()
        tvBillStatus.text = mBill?.SettlementStatusName
        tvBillAmount.text = "${getString(R.string.niara)} ${mBill?.getBillAmount()?.format(2)}"

        tvBillDate.text = mBill?.SettlementDueDate?.toDate(EIRS_DATE_FORMAT)?.format("MMM dd, yyyy")
        tvBillOutstanding.text = "${getString(R.string.niara)} ${mBill?.getOutstandingAmount()?.format(2)}"
    }

}