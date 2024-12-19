package ng.gov.eirs.mas.erasmpoa.printer

import android.graphics.Bitmap
import com.horizonpay.smartpossdk.aidl.printer.AidlPrinterListener
import com.horizonpay.smartpossdk.aidl.printer.IAidlPrinter
import com.horizonpay.smartpossdk.data.PrinterConst
import kotlinx.coroutines.runBlocking
import ng.gov.eirs.mas.erasmpoa.utils.CombBitmap
import ng.gov.eirs.mas.erasmpoa.utils.GenerateBitmap
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

internal class HorizonPrinter(private val printer: IAidlPrinter): Printer {
    private val LINE_LENTH = 30
    private var printerBitmap = CombBitmap()

    private fun getAlign(align: Align): GenerateBitmap.AlignEnum = when (align) {
            Align.RIGHT -> GenerateBitmap.AlignEnum.RIGHT
            Align.LEFT -> GenerateBitmap.AlignEnum.LEFT
            Align.CENTER -> GenerateBitmap.AlignEnum.CENTER
    }

    private fun getTextSize(size: FontSize): Int {
       return when (size) {
            FontSize.TINY -> 14
           FontSize.SMALL -> 16
           FontSize.NORMAL -> 20
           FontSize.LARGE -> 36
           FontSize.EXTRA_LARGE -> 42
           FontSize.BIG -> 50
        }
    }

    private fun isBold(style: FontStyle) = when (style) {
            FontStyle.BOLD , FontStyle.BOLD_INVERTED -> true
            else -> false
    }

    private fun isInverted(style: FontStyle) = when (style) {
        FontStyle.INVERTED , FontStyle.BOLD_INVERTED -> true
        else -> false
    }


    override fun addText(text: String, format: TextFormat) {
        printerBitmap.addBitmap(GenerateBitmap.str2Bitmap(text, getTextSize(format.fontSize), getAlign(format.align), isBold(format.fontStyle), isInverted(format.fontStyle)))
    }

    override fun addDoubleText(left: String, right: String, format: TextFormat) {

        if ((format.fontSize !in arrayOf(FontSize.SMALL, FontSize.TINY)) && ((left.length + right.length) > LINE_LENTH)) {
            addText(left, format.copy(align = Align.LEFT))
            addText(right, format.copy(align = Align.RIGHT))
            return
        }

        printerBitmap.addBitmap(GenerateBitmap.str2Bitmap(left, right,
            getTextSize(format.fontSize), isBold(format.fontStyle), isInverted(format.fontStyle)))
    }

    override fun addImage(bitmap: Bitmap, width: Int, height: Int, offset: Int) {
        val newBitmap =  Bitmap.createScaledBitmap(bitmap, width, height, true)
        printerBitmap.addBitmap(newBitmap)
    }

    override fun addBarcode(text: String, expectedHeight: Int, align: Align) {
        printerBitmap.addBitmap(GenerateBitmap.generateBarCodeBitmap(text, expectedHeight, expectedHeight))
    }

    override fun addQrCode(text: String, expectedHeight: Int, offset: Int) {
        printerBitmap.addBitmap(GenerateBitmap.generateQRCodeBitmap(text, expectedHeight))
    }

    override fun feedLine(count: Int) {
        for ( i in 1..count) {
            printerBitmap.addBitmap(GenerateBitmap.generateGap(getTextSize(FontSize.LARGE)))
        }
    }

    override fun addLine() {
        printerBitmap.addBitmap(GenerateBitmap.generateLine(2))
    }

    override fun getStatus(): Printer.Status = horizonStateToPrinterStatus(printer.printerState)

    private fun horizonStateToPrinterStatus(state: Int) =  when (state) {
        PrinterConst.State.PRINTER_STATE_NORMAL -> Printer.Status.OK
        PrinterConst.State.PRINTER_STATE_BUSY -> Printer.Status.BUSY
        PrinterConst.State.PRINTER_STATE_NOPAPER ,PrinterConst.State.ERROR_PRINT_NOPAPER -> Printer.Status.OUT_OF_PAPER
        PrinterConst.State.PRINTER_STATE_HIGHTEMP -> Printer.Status.OVER_HEAT
        PrinterConst.State.ERROR_PRINT_BITMAP_WIDTH_OVERFLOW -> Printer.Status.BUFFER_OVERFLOW
        else -> Printer.Status.ERROR
    }

    override fun print() = runBlocking<Printer.Status> {
            val result =  suspendCoroutine<Printer.Status> {
                    printer.printGray = PrinterConst.Gray.LEVEL_5
                    printer.printBmp(true, true, printerBitmap.combBitmap, 0, object : AidlPrinterListener.Stub() {

                        override fun onError(error: Int) {
                            println("Printer Error: $error")
                            it.resume(horizonStateToPrinterStatus(error))
                        }

                        override fun onPrintSuccess() {
                            println("")
                            it.resume(Printer.Status.OK)
                        }
                    })
                    printer.cutPaper()
                }

        printerBitmap.removeAll()
        result
    }
}