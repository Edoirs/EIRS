package ng.gov.eirs.mas.erasmpoa.printer

import android.graphics.Bitmap
import java.util.*

interface Printer {

    fun addText(text: String, format: TextFormat = TextFormat())
    fun addDoubleText(left: String, right: String, format: TextFormat = TextFormat())
    fun addImage(bitmap: Bitmap, width: Int, height: Int, offset: Int)
    fun addBarcode(text: String, expectedHeight: Int, align: Align)
    fun addQrCode(text: String, expectedHeight: Int, offset: Int)
    fun feedLine(count: Int)
    fun addLine()
    fun getStatus(): Status
    fun print(): Status



    enum class Status {
        OK, OUT_OF_PAPER, OVER_HEAT, LOW_VOLTAGE, PAPER_JAM, BUSY, BUFFER_OVERFLOW, ERROR;

        override fun toString() = name.lowercase(Locale.getDefault()).capitalize(Locale.ROOT).split("_").joinToString(" ")
    }
}

data class TextFormat(
    val fontSize: FontSize = FontSize.NORMAL,
    val align: Align = Align.CENTER,
    val fontStyle: FontStyle = FontStyle.NORMAL
)


enum class Align {
    LEFT, CENTER, RIGHT
}

enum class FontSize {
    TINY,SMALL, NORMAL, LARGE, EXTRA_LARGE, BIG
}

enum class FontStyle {
    NORMAL, ITALIC, BOLD, INVERTED, BOLD_INVERTED
}

