package ng.gov.eirs.mas.erasmpoa.printer

import android.app.ProgressDialog
import android.content.DialogInterface
import android.graphics.BitmapFactory
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.text.TextUtils
import android.util.Log
import com.telpo.tps550.api.printer.NoPaperException
import com.telpo.tps550.api.printer.OverHeatException
import com.telpo.tps550.api.printer.UsbThermalPrinter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.telpoDevices.activity_printer.*
import ng.gov.eirs.mas.erasmpoa.R
import ng.gov.eirs.mas.erasmpoa.data.dao.Haulage
import ng.gov.eirs.mas.erasmpoa.data.dao.Submission
import ng.gov.eirs.mas.erasmpoa.sync.SyncTracker
import ng.gov.eirs.mas.erasmpoa.ui.base.BaseAppCompatActivity
import ng.gov.eirs.mas.erasmpoa.util.getUser

class PrinterActivity : BaseAppCompatActivity() {

    private var usbThermalPrinter: UsbThermalPrinter? = null
    private var submission: Submission? = null
    private var haulage: Haulage? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_printer)
        setSupportActionBar(toolbar)

        submission = intent.getSerializableExtra("submission") as Submission?
        haulage = intent.getSerializableExtra("haulage") as Haulage?

        usbThermalPrinter = UsbThermalPrinter(activity)

        startPrinter()

        btnPrint.setOnClickListener {
            // handle battery low and low paper
            onClickPrint()
        }
    }

    private fun startPrinter() {
        mProgressDialog = ProgressDialog.show(activity, null, getString(R.string.printer_init))
        SyncTracker.addSync(RxPrinter.startPrinter(usbThermalPrinter)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                mProgressDialog?.dismiss()
                Log.d("PrinterActivity", "status $it")
            }, {
                it.printStackTrace()
                mProgressDialog?.dismiss()
                when (it) {
                    is NoPaperException -> {
                        alert(
                            getString(R.string.print_error),
                            getString(R.string.print_error_no_paper),
                            null
                        )
                    }
                    is OverHeatException -> {
                        alert(
                            getString(R.string.print_error),
                            getString(R.string.print_error_over_heat),
                            null
                        )
                    }
                    else -> {
                        alert(
                            getString(R.string.print_error),
                            getString(R.string.print_error_unknown),
                            null
                        )
                    }
                }
            })
        )
    }

    private fun onClickPrint() {
        mProgressDialog = ProgressDialog.show(activity, null, getString(R.string.printing))

        val logo = BitmapFactory.decodeResource(resources, R.drawable.eirs_logo)

        if (submission != null) {
            SyncTracker.addSync(RxPrinter.printSubmission(
                usbThermalPrinter,
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
                    when (it) {
                        is NoPaperException -> {
                            alert(
                                getString(R.string.print_error),
                                getString(R.string.print_error_no_paper),
                                null
                            )
                        }
                        is OverHeatException -> {
                            alert(
                                getString(R.string.print_error),
                                getString(R.string.print_error_over_heat),
                                null
                            )
                        }
                        else -> {
                            alert(
                                getString(R.string.print_error),
                                getString(R.string.print_error_unknown),
                                null
                            )
                        }
                    }
                })
            )
        }

        if (haulage != null) {
            SyncTracker.addSync(RxPrinter.printHaulage(
                usbThermalPrinter,
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
                    when (it) {
                        is NoPaperException -> {
                            alert(
                                getString(R.string.print_error),
                                getString(R.string.print_error_no_paper),
                                null
                            )
                        }
                        is OverHeatException -> {
                            alert(
                                getString(R.string.print_error),
                                getString(R.string.print_error_over_heat),
                                null
                            )
                        }
                        else -> {
                            alert(
                                getString(R.string.print_error),
                                getString(R.string.print_error_unknown),
                                null
                            )
                        }
                    }
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
        usbThermalPrinter?.stop()
    }
}
