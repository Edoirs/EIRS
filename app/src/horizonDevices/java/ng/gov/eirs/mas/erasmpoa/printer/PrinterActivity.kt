package ng.gov.eirs.mas.erasmpoa.printer

import android.app.ProgressDialog
import android.content.DialogInterface
import android.graphics.BitmapFactory
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.text.TextUtils
import android.util.Log
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.horizonDevices.activity_printer.*
import ng.gov.eirs.mas.erasmpoa.HorizonSmartPos
import ng.gov.eirs.mas.erasmpoa.R
import ng.gov.eirs.mas.erasmpoa.data.dao.Haulage
import ng.gov.eirs.mas.erasmpoa.data.dao.Submission
import ng.gov.eirs.mas.erasmpoa.sync.SyncTracker
import ng.gov.eirs.mas.erasmpoa.ui.base.BaseAppCompatActivity
import ng.gov.eirs.mas.erasmpoa.util.getUser

class PrinterActivity : BaseAppCompatActivity() {

//    private var usbThermalPrinter: UsbThermalPrinter? = null
    private lateinit var horizonPrinter: HorizonSmartPos
    private var submission: Submission? = null
    private var haulage: Haulage? = null
    private var isPrinterReady = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_printer)
        setSupportActionBar(toolbar)

        // usbThermalPrinter = UsbThermalPrinter(activity)
        horizonPrinter = HorizonSmartPos()
        showLoadingDialog()
        // Initialize device and wait for completion
//        horizonPrinter.initializeDevice(this)
        horizonPrinter.initializeDevice(this, object : HorizonSmartPos.InitCallback {
            override fun onSuccess() {
                dismissLoadingDialog()
                isPrinterReady = true
                startPrinter()
            }

            override fun onFailure(error: String) {
                dismissLoadingDialog()
                alert(getString(R.string.printer_init_error), error, null)
            }
        })

        submission = intent.getSerializableExtra("submission") as Submission?
        haulage = intent.getSerializableExtra("haulage") as Haulage?


//        startPrinter()

//        btnPrint.setOnClickListener {
//            // handle battery low and low paper
//            onClickPrint()
//        }
        btnPrint.setOnClickListener {
            if (isPrinterReady) {
                onClickPrint()
            } else {
                alert(getString(R.string.print_error), "Printer is not ready", null)
            }
        }

    }


    private fun showLoadingDialog() {
        mProgressDialog = ProgressDialog.show(activity, null, getString(R.string.printer_init))
    }
    private fun dismissLoadingDialog() {
        mProgressDialog?.dismiss()
    }


    private fun startPrinter() {
//        mProgressDialog = ProgressDialog.show(activity, null, getString(R.string.printer_init))
        SyncTracker.addSync(RxPrinter.startPrinter(horizonPrinter.getPrinter(this))
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                mProgressDialog?.dismiss()
                Log.d("PrinterActivity", "status $it")
            }, {
                it.printStackTrace()
                mProgressDialog?.dismiss()
                alert(
                    getString(R.string.print_error),
                    getString(R.string.print_error_unknown),
                    null
                )
//                when (it) {
////                    is NoPaperException -> {
//                    Printer.Status.OUT_OF_PAPER -> {
//                        alert(
//                            getString(R.string.print_error),
//                            getString(R.string.print_error_no_paper),
//                            null
//                        )
//                    }
////                    is OverHeatException -> {
//                    Printer.Status.OVER_HEAT -> {
//                        alert(
//                            getString(R.string.print_error),
//                            getString(R.string.print_error_over_heat),
//                            null
//                        )
//                    }
//                    else -> {
//                        alert(
//                            getString(R.string.print_error),
//                            getString(R.string.print_error_unknown),
//                            null
//                        )
//                    }
//                }
            }
            )
        )
    }

    private fun onClickPrint() {
        mProgressDialog = ProgressDialog.show(activity, null, getString(R.string.printing))

        val logo = BitmapFactory.decodeResource(resources, R.drawable.eirs_logo)

        if (submission != null) {
            SyncTracker.addSync(RxPrinter.printSubmission(
                horizonPrinter.getPrinter(this),
                submission,
                logo,
                "${getUser()?.firstName} ${getUser()?.lastName}"
            ).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    mProgressDialog?.dismiss()
                }, {
                    it.printStackTrace()
                    mProgressDialog?.dismiss()
                    alert(
                        getString(R.string.print_error),
                        getString(R.string.print_error_unknown),
                        null
                    )
//                    when (it) {
////                        is  NoPaperException -> {
//                        Printer.Status.OUT_OF_PAPER -> {
//                            alert(
//                                getString(R.string.print_error),
//                                getString(R.string.print_error_no_paper),
//                                null
//                            )
//                        }
////                        is OverHeatException -> {
//                        Printer.Status.OVER_HEAT -> {
//                            alert(
//                                getString(R.string.print_error),
//                                getString(R.string.print_error_over_heat),
//                                null
//                            )
//                        }
//                        else -> {
//                            alert(
//                                getString(R.string.print_error),
//                                getString(R.string.print_error_unknown),
//                                null
//                            )
//                        }
//                    }
                })
            )
        }

        if (haulage != null) {
            SyncTracker.addSync(RxPrinter.printHaulage(
                horizonPrinter.getPrinter(this),
                haulage,
                logo,
                "${getUser()?.firstName} ${getUser()?.lastName}"
            ).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    mProgressDialog?.dismiss()
                }, {
                    it.printStackTrace()
                    mProgressDialog?.dismiss()
                    alert(
                        getString(R.string.print_error),
                        getString(R.string.print_error_unknown),
                        null
                    )
//                    when (it) {
////                        is NoPaperException -> {
//                        Printer.Status.OUT_OF_PAPER -> {
//                            alert(
//                                getString(R.string.print_error),
//                                getString(R.string.print_error_no_paper),
//                                null
//                            )
//                        }
////                        is OverHeatException -> {
//                        Printer.Status.OVER_HEAT -> {
//                            alert(
//                                getString(R.string.print_error),
//                                getString(R.string.print_error_over_heat),
//                                null
//                            )
//                        }
//                        else -> {
//                            alert(
//                                getString(R.string.print_error),
//                                getString(R.string.print_error_unknown),
//                                null
//                            )
//                        }
//                    }
                })
            )
        }
    }

    fun alert(title: String?, message: String, ocl: DialogInterface.OnClickListener?) {
        val builder = AlertDialog.Builder(activity)
        if (!TextUtils.isEmpty(title)) {
            builder.setTitle(title)
        }
        if (!TextUtils.isEmpty(message)) {
            builder.setMessage(message)
        }
        builder.setPositiveButton(R.string.okay, ocl)
        builder.setCancelable(false)
        builder.show()
    }

    override fun onDestroy() {
        super.onDestroy()
//        usbThermalPrinter?.stop()
        horizonPrinter.destruct(this)
    }
}
