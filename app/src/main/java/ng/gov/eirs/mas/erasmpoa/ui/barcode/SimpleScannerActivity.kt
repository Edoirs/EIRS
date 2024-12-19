package ng.gov.eirs.mas.erasmpoa.ui.barcode

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.google.zxing.Result
import me.dm7.barcodescanner.zxing.ZXingScannerView
import ng.gov.eirs.mas.erasmpoa.ui.base.BaseAppCompatActivity

class SimpleScannerActivity : BaseAppCompatActivity(), ZXingScannerView.ResultHandler {
    companion object {
        const val ZBAR_SCANNER_REQUEST = 10
        const val SCAN_RESULT = "scan_result"
    }

    private var mScannerView: ZXingScannerView? = null

    public override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        mScannerView = ZXingScannerView(this)
        mScannerView?.setAutoFocus(true)
        setContentView(mScannerView)
    }

    public override fun onResume() {
        super.onResume()
        mScannerView?.setResultHandler(this)
        mScannerView?.startCamera()
    }

    public override fun onPause() {
        super.onPause()
        mScannerView?.stopCamera()
    }

    override fun handleResult(rawResult: Result) {
        val data = Intent()
        data.putExtra(SCAN_RESULT, rawResult.text)
        setResult(Activity.RESULT_OK, data)
        finish()
    }
}
