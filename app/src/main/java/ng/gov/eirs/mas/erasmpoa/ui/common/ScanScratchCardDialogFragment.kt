package ng.gov.eirs.mas.erasmpoa.ui.common

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.support.v7.app.AlertDialog
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import kotlinx.android.synthetic.main.dialog_scan_scratch_card.*
import ng.gov.eirs.mas.erasmpoa.R
import ng.gov.eirs.mas.erasmpoa.data.constant.RequestCode
import ng.gov.eirs.mas.erasmpoa.ui.barcode.SimpleScannerActivity
import ng.gov.eirs.mas.erasmpoa.ui.base.BaseDialogFragment
import ng.gov.eirs.mas.erasmpoa.util.hideKeyboard
import ng.gov.eirs.mas.erasmpoa.util.isCameraAvailable
import ng.gov.eirs.mas.erasmpoa.util.isPermissionGranted
import ng.gov.eirs.mas.erasmpoa.util.requestPermission

class ScanScratchCardDialogFragment : BaseDialogFragment() {
    private var mOnSelectedListener: ((String) -> Unit)? = null

    private var remainingAmount = 0.toDouble()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_scan_scratch_card, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        remainingAmount = arguments?.getDouble("remainingAmount") ?: 0.toDouble()

        viewScan.setOnClickListener { onClickScan() }
        btnAdd.setOnClickListener { onClickAttach() }

        btnCancel.setOnClickListener {
            activity?.hideKeyboard(etScratchCard)
            dismissAllowingStateLoss()
        }
    }

    private fun onClickScan() {
        if (activity?.isPermissionGranted(Manifest.permission.CAMERA) == true) {
            launchBarcodeScanner()
        } else {
            activity?.requestPermission(RequestCode.PERMISSION, Manifest.permission.CAMERA)
        }
    }

    private fun launchBarcodeScanner() {
        if (activity?.isCameraAvailable() == true) {
            val intent = Intent(activity, SimpleScannerActivity::class.java)
            // intent.putExtra(ZBarConstants.SCAN_MODES, new int[] { Symbol.UPCA, Symbol.UPCE });
            startActivityForResult(intent, SimpleScannerActivity.ZBAR_SCANNER_REQUEST)
        } else {
            Toast.makeText(activity, "Camera Unavailable", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == RequestCode.PERMISSION) {
            if (activity?.isPermissionGranted(permissions, grantResults, Manifest.permission.CAMERA) == true) {
                launchBarcodeScanner()
            } else {
                activity?.let { activity ->
                    val builder = AlertDialog.Builder(activity)
                    builder.setTitle(R.string.permission_access_required)
                    builder.setMessage(getString(R.string.permission_is_required, getString(R.string.camera)))
                    builder.setPositiveButton(R.string.settings) { _, _ ->
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        val uri = Uri.fromParts("package", activity.packageName, null)
                        intent.data = uri
                        startActivityForResult(intent, RequestCode.PERMISSION)
                    }
                    builder.setNegativeButton(R.string.cancel, null)
                    builder.show()
                }
            }
        }
    }

    private fun onClickAttach() {
        val code = etScratchCard.text.toString()

        val isOkay: Boolean

        isOkay = validateScratchCard(code)

        if (isOkay) {
            activity?.hideKeyboard(etScratchCard)
            dismissAllowingStateLoss()
            mOnSelectedListener?.invoke(code)
        }
    }

    private fun validateScratchCard(code: String): Boolean {
        var isOkay = true

        if (TextUtils.isEmpty(code)) {
            tilScratchCard.error = getString(R.string.required_field)
            etScratchCard.requestFocus()
            isOkay = false
        }

        if (code.length != 16) {
            tilScratchCard.error = "PIN must be 16 digit"
            etScratchCard.requestFocus()
            isOkay = false
        }

        if (!code.matches("[0-9]+".toRegex())) {
            tilScratchCard.error = "PIN must be numbers only"
            etScratchCard.requestFocus()
            isOkay = false
        }

        return isOkay
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RequestCode.PERMISSION) {
            onClickScan()
        } else if (requestCode == SimpleScannerActivity.ZBAR_SCANNER_REQUEST && resultCode == Activity.RESULT_OK) {
            data?.getStringExtra(SimpleScannerActivity.SCAN_RESULT)?.let {
                etScratchCard.setText(it)
                validateScratchCard(it)
            }
        }
    }

    fun setOnSelectedListener(listener: (String) -> Unit) {
        mOnSelectedListener = listener
    }

    companion object {
        fun newInstance(remainingAmount: Double): ScanScratchCardDialogFragment {
            val bundle = Bundle()
            bundle.putDouble("remainingAmount", remainingAmount)

            val fragment = ScanScratchCardDialogFragment()
            fragment.arguments = bundle

            return fragment
        }
    }
}
