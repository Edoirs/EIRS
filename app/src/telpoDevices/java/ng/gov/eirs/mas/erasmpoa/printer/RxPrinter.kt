package ng.gov.eirs.mas.erasmpoa.printer

import android.graphics.Bitmap
import com.google.gson.JsonParser
import com.telpo.tps550.api.printer.UsbThermalPrinter
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import ng.gov.eirs.mas.erasmpoa.data.dao.*
import ng.gov.eirs.mas.erasmpoa.util.format
import java.util.*

object RxPrinter {
    fun startPrinter(usbThermalPrinter: UsbThermalPrinter?): Flowable<Int> {
        return Flowable.create({
            try {
                usbThermalPrinter?.start(0)
                usbThermalPrinter?.reset()
                usbThermalPrinter?.version
                it.onNext(usbThermalPrinter?.checkStatus() ?: 0)
                it.onComplete()
            } catch (e: Exception) {
                it.onError(e)
            }
        }, BackpressureStrategy.BUFFER)
    }

    fun printSubmission(
            usbThermalPrinter: UsbThermalPrinter?,
            submission: Submission?,
            logo: Bitmap,
            collector: String?
    ): Flowable<Unit> {
        return Flowable.create({
            try {
                usbThermalPrinter?.reset()
                usbThermalPrinter?.setAlgin(UsbThermalPrinter.ALGIN_MIDDLE)
                usbThermalPrinter?.setTextSize(20)
                usbThermalPrinter?.setGray(5)

                usbThermalPrinter?.printLogo(logo, false)
                usbThermalPrinter?.walkPaper(5)

//                usbThermalPrinter?.addString("EDO STATE GOVERNMENT")
//                usbThermalPrinter?.endLine()

                Lga.byAreaId(submission?.areaId ?: 0)?.let { area ->
                    usbThermalPrinter?.addString(area.areaName.toUpperCase(Locale.getDefault()))
                    usbThermalPrinter?.endLine()
                }

                usbThermalPrinter?.setBold(true)
                usbThermalPrinter?.addString("PAYMENT RECEIPT")
                usbThermalPrinter?.setBold(false)
                usbThermalPrinter?.endLine()
                usbThermalPrinter?.printString()

                usbThermalPrinter?.setAlgin(UsbThermalPrinter.ALGIN_LEFT)

                usbThermalPrinter?.setBold(true)
                usbThermalPrinter?.addString("DATE: ${Date(submission?.submissionTime
                        ?: 0).format("MMM, dd yyyy hh:mm:ss AA")}")

                usbThermalPrinter?.setBold(false)
                usbThermalPrinter?.endLine()

                usbThermalPrinter?.addString("COLLECTOR’S NAME: $collector")
                usbThermalPrinter?.endLine()

                usbThermalPrinter?.addString("TAXPAYER NAME: ${submission?.name}")
                usbThermalPrinter?.endLine()

                usbThermalPrinter?.addString("PHONE NO: " + submission?.mobile)
                usbThermalPrinter?.endLine()

                usbThermalPrinter?.addString("CATEGORY: " + submission?.group)
                usbThermalPrinter?.endLine()

                usbThermalPrinter?.addString("SUB CATEGORY: " + submission?.subGroup)
                usbThermalPrinter?.endLine()

//                if (poACapture?.vehiclePlate?.isNotEmpty() == true) {
//                    usbThermalPrinter?.addString("VEHICLE PLATE NO: " + poACapture.vehiclePlate)
//                    usbThermalPrinter?.endLine()
//                }

                usbThermalPrinter?.addString("AMOUNT PAID: " + "NGN " + submission?.amountPaid?.format(2, false))
                usbThermalPrinter?.endLine()

                usbThermalPrinter?.addString("CARD(s) USED:")
                usbThermalPrinter?.endLine()
                JsonParser().parse(submission?.scratchCard).asJsonArray?.let { array ->
                    array.forEach { card ->
                        usbThermalPrinter?.addString(card.asJsonObject["card"].asString + " : NGN " + card.asJsonObject["amount"].asDouble.format(2, false))
                        usbThermalPrinter?.endLine()
                    }
                }

                val submissionDate = Date(submission?.submissionTime ?: 0)
                if (submission?.menuSection?.equals("Mobile Location", false) == true) {
                    usbThermalPrinter?.addString("VALIDITY: " + submissionDate.format("dd/MMM/yyyy"))
                } else if (submission?.menuSection?.equals("Presumptive Taxes", false) == true) {
                    val c = Calendar.getInstance()
                    c.time = submissionDate
                    c.add(Calendar.YEAR, 1)
                    c.add(Calendar.DAY_OF_YEAR, -1)
                    val oneYearLater = c.time
                    usbThermalPrinter?.addString("VALIDITY: " + oneYearLater.format("dd/MMM/yyyy"))
                }
                usbThermalPrinter?.endLine()


//                val distribution = SQLite.select().from(SubSectorAgencyDistribution::class.java)
//                        .where(SubSectorAgencyDistribution_Table.lgaSubSectorId.eq(poACapture?.lgaSubSectorId))
//                        .queryList()
//
//                if (distribution.isNotEmpty()) {
//                    usbThermalPrinter?.setBold(true)
//                    usbThermalPrinter?.addString("Stakeholders:")
//                    usbThermalPrinter?.setBold(false)
//                    usbThermalPrinter?.endLine()
//
//                    distribution.forEach { dist ->
//                        usbThermalPrinter?.addString("${dist.agencyName}: NGN ${dist.priceValue.format(2, false)}")
//                        usbThermalPrinter?.endLine()
//                    }
//                }

                usbThermalPrinter?.setAlgin(UsbThermalPrinter.ALGIN_MIDDLE)
                usbThermalPrinter?.endLine()
                usbThermalPrinter?.addString("--------------------")
                usbThermalPrinter?.endLine()
                usbThermalPrinter?.addString("Contribute to the growth of Edo State by paying your tax")
                usbThermalPrinter?.endLine()
                usbThermalPrinter?.addString("Call 08130970146 for complain or enquiry")

                usbThermalPrinter?.printString()

                usbThermalPrinter?.walkPaper(10)
                it.onNext(Unit)
                it.onComplete()
            } catch (e: Exception) {
                it.onError(e)
            }
        }, BackpressureStrategy.BUFFER)
    }

    fun printHaulage(
            usbThermalPrinter: UsbThermalPrinter?,
            haulage: Haulage?,
            logo: Bitmap,
            collector: String?
    ): Flowable<Unit> {
        return Flowable.create({
            try {
                usbThermalPrinter?.reset()
                usbThermalPrinter?.setAlgin(UsbThermalPrinter.ALGIN_MIDDLE)
                usbThermalPrinter?.setTextSize(20)
                usbThermalPrinter?.setGray(5)

                usbThermalPrinter?.printLogo(logo, false)
                usbThermalPrinter?.walkPaper(5)

//                usbThermalPrinter?.addString("EDO STATE GOVERNMENT")
//                usbThermalPrinter?.endLine()

                Lga.byAreaId(haulage?.areaId ?: 0)?.let { lga ->
                    usbThermalPrinter?.addString(lga.areaName.toUpperCase())
                    usbThermalPrinter?.endLine()
                }

                usbThermalPrinter?.setBold(true)
                usbThermalPrinter?.addString("PAYMENT RECEIPT")
                usbThermalPrinter?.setBold(false)
                usbThermalPrinter?.endLine()
                usbThermalPrinter?.printString()

                usbThermalPrinter?.setAlgin(UsbThermalPrinter.ALGIN_LEFT)

                usbThermalPrinter?.setBold(true)
                usbThermalPrinter?.addString("DATE: ${Date(haulage?.submissionTime
                        ?: 0).format("MMM, dd yyyy hh:mm:ss AA")}")

                usbThermalPrinter?.setBold(false)
                usbThermalPrinter?.endLine()

                usbThermalPrinter?.addString("COLLECTOR’S NAME: $collector")
                usbThermalPrinter?.endLine()

                usbThermalPrinter?.addString("TAXPAYER NAME: ${haulage?.name}")
                usbThermalPrinter?.endLine()

                usbThermalPrinter?.addString("PHONE NO: " + haulage?.mobile)
                usbThermalPrinter?.endLine()

                Beat.byBeatId(haulage?.beatId ?: 0)?.let { beat ->
                    usbThermalPrinter?.addString("BEAT: " + beat.beatName)
                    usbThermalPrinter?.endLine()

                    usbThermalPrinter?.addString("REVENUE TYPE: " + beat.revenueTypeName)
                    usbThermalPrinter?.endLine()
                }

//                if (poACapture?.vehiclePlate?.isNotEmpty() == true) {
//                    usbThermalPrinter?.addString("VEHICLE PLATE NO: " + poACapture.vehiclePlate)
//                    usbThermalPrinter?.endLine()
//                }

//                usbThermalPrinter?.addString("AMOUNT PAID: " + "NGN " + submission?.amountPaid?.format(2, false))
//                usbThermalPrinter?.endLine()

//                val distribution = SQLite.select().from(SubSectorAgencyDistribution::class.java)
//                        .where(SubSectorAgencyDistribution_Table.lgaSubSectorId.eq(poACapture?.lgaSubSectorId))
//                        .queryList()
//
//                if (distribution.isNotEmpty()) {
//                    usbThermalPrinter?.setBold(true)
//                    usbThermalPrinter?.addString("Stakeholders:")
//                    usbThermalPrinter?.setBold(false)
//                    usbThermalPrinter?.endLine()
//
//                    distribution.forEach { dist ->
//                        usbThermalPrinter?.addString("${dist.agencyName}: NGN ${dist.priceValue.format(2, false)}")
//                        usbThermalPrinter?.endLine()
//                    }
//                }

                usbThermalPrinter?.setAlgin(UsbThermalPrinter.ALGIN_MIDDLE)
                usbThermalPrinter?.endLine()
                usbThermalPrinter?.addString("--------------------")
                usbThermalPrinter?.endLine()
                usbThermalPrinter?.addString("Contribute to the growth of Edo State by paying your tax")
                usbThermalPrinter?.endLine()
                usbThermalPrinter?.addString("Call 08130970146 for complain or enquiry")

                usbThermalPrinter?.printString()

                usbThermalPrinter?.walkPaper(10)
                it.onNext(Unit)
                it.onComplete()
            } catch (e: Exception) {
                it.onError(e)
            }
        }, BackpressureStrategy.BUFFER)
    }
}