package ng.gov.eirs.mas.erasmpoa.ui.paybill

import android.os.Bundle
import kotlinx.android.synthetic.main.activity_tax_payer_detail.*
import ng.gov.eirs.mas.erasmpoa.R
import ng.gov.eirs.mas.erasmpoa.data.model.Bill
import ng.gov.eirs.mas.erasmpoa.ui.base.BaseAppCompatActivity

class TaxPayerDetailActivity : BaseAppCompatActivity() {

    private var mBill: Bill? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tax_payer_detail)

        setUpToolbar(toolbar)
        setHomeAsUp(true)

        mBill = intent.getSerializableExtra("bill") as Bill?

        setValues()
    }

    private fun setValues() {
        tvTaxPayerName.text = mBill?.TaxPayerName
        tvTaxPayerRin.text = mBill?.TaxPayerRIN
        tvTaxPayerType.text = mBill?.TaxPayerTypeName
    }

}