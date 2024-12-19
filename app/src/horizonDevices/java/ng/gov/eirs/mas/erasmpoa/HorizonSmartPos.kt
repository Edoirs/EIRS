package ng.gov.eirs.mas.erasmpoa

import android.app.Application
import android.content.Context
import com.horizonpay.smartpossdk.PosAidlDeviceServiceUtil
import com.horizonpay.smartpossdk.aidl.IAidlDevice
import com.horizonpay.utils.BaseUtils
import ng.gov.eirs.mas.erasmpoa.printer.HorizonPrinter
import ng.gov.eirs.mas.erasmpoa.printer.Printer

class HorizonSmartPos {

    private var aidl: IAidlDevice? = null
    private lateinit var printer: HorizonPrinter
    private var isInitialized = false

    interface InitCallback {
        fun onSuccess()
        fun onFailure(error: String)
    }

     fun initializeDevice(context: Context, callback: InitCallback) {
        PosAidlDeviceServiceUtil.connectDeviceService(
            context,
            object : PosAidlDeviceServiceUtil.DeviceServiceListen {
                override fun onConnected(p0: IAidlDevice) {
                    aidl = p0
                    printer = HorizonPrinter(aidl!!.printer)
                    isInitialized = true
                    callback.onSuccess()
                    println("TIM:: Horizon Device - initializeDevice - onConnected")
                }

                override fun error(errorCode: Int) {
                    println("TIM:: Horizon Device - initializeDevice - error")
                    callback.onFailure("Initialization failed: Error Code $errorCode")
//                    throw RuntimeException("Initialization failed: Error Code $errorCode")
                }

                override fun onDisconnected() {
                    // Handle disconnection
                    callback.onFailure("Device disconnected unexpectedly")
                    println("TIM:: Horizon Device - initializeDevice - onDisconnected")
                }

                override fun onUnCompatibleDevice() {
                    println("TIM:: Horizon Device - initializeDevice - onUnCompatibleDevice")
                    callback.onFailure("Device not compatible")
//                    throw RuntimeException("Device not compatible")
                }
            }
        )

         BaseUtils.init(context.applicationContext as Application)
         println("TIM:: Horizon Device - initializeDevice - BaseUtils.init")
    }

     fun getPrinter(context: Context): Printer {
         println("TIM:: Horizon Device - getPrinter")
         if (!isInitialized) {
             throw IllegalStateException("Printer not initialized yet!")
         }
         return printer
    }

     fun destruct(context: Context) {
        PosAidlDeviceServiceUtil.disconnectedDeviceService(context, null)
        aidl = null
         isInitialized = false
         println("TIM:: Horizon Device - destruct")
    }
}
