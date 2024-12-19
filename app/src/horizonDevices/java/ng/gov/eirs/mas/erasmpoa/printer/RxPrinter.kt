package ng.gov.eirs.mas.erasmpoa.printer

import android.graphics.Bitmap
import com.google.gson.JsonParser
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import ng.gov.eirs.mas.erasmpoa.data.dao.*
import ng.gov.eirs.mas.erasmpoa.util.format
import java.util.*

object RxPrinter {
    fun startPrinter(horizonPrinter: Printer?): Flowable<Int> {
        return Flowable.create({
            try {
//                horizonPrinter?.start(0)
//                horizonPrinter?.reset()
//                horizonPrinter?.version
//                it.onNext(horizonPrinter?.checkStatus() ?: 0)
//                it.onComplete()
            } catch (e: Exception) {
                it.onError(e)
            }
        }, BackpressureStrategy.BUFFER)
    }

    fun printSubmission(
            horizonPrinter: Printer?,
            submission: Submission?,
            logo: Bitmap,
            collector: String?
    ): Flowable<Unit> {
        return Flowable.create({
            try {
//                horizonPrinter?.reset()
//                horizonPrinter?.setAlgin(HorizonPrinter.ALGIN_MIDDLE)
//                horizonPrinter?.setTextSize(20)
//                horizonPrinter?.setGray(5)
//
//                horizonPrinter?.printLogo(logo, false)
//                horizonPrinter?.walkPaper(5)

                horizonPrinter?.feedLine(1)

                logo.let {
                    horizonPrinter?.addImage(logo, logo.width, logo.height, 90)
                }

////                horizonPrinter?.addString("EDO STATE GOVERNMENT")
// //               horizonPrinter?.endLine()

                Lga.byAreaId(submission?.areaId ?: 0)?.let { area ->
//                    horizonPrinter?.addString(area.areaName.toUpperCase(Locale.getDefault()))
//                    horizonPrinter?.endLine()
                    horizonPrinter?.addText(
                        area.areaName.toUpperCase(Locale.getDefault()),
                        TextFormat(align = Align.CENTER)
                    )
                    horizonPrinter?.addLine()
                }

//                horizonPrinter?.setBold(true)
//                horizonPrinter?.addString("PAYMENT RECEIPT")
//                horizonPrinter?.setBold(false)
//                horizonPrinter?.endLine()
//                horizonPrinter?.printString()

                horizonPrinter?.addText(
                    "PAYMENT RECEIPT",
                    TextFormat(FontSize.LARGE, Align.CENTER)
                )
                horizonPrinter?.addLine()


//                horizonPrinter?.setAlgin(HorizonPrinter.ALGIN_LEFT)

//                horizonPrinter?.setBold(true)
//                horizonPrinter?.addString("DATE: ${Date(submission?.submissionTime
//                        ?: 0).format("MMM, dd yyyy hh:mm:ss AA")}")

//                horizonPrinter?.setBold(false)
//                horizonPrinter?.endLine()

                horizonPrinter?.addText(
                    "DATE: ${Date(submission?.submissionTime
                        ?: 0).format("MMM, dd yyyy hh:mm:ss AA")}",
                    TextFormat(FontSize.LARGE, Align.LEFT)
                )
                horizonPrinter?.addLine()



//                horizonPrinter?.addString("COLLECTOR’S NAME: $collector")
//                horizonPrinter?.endLine()
                horizonPrinter?.addText(
                    "COLLECTOR’S NAME: $collector",
                    TextFormat(align = Align.LEFT)
                )
                horizonPrinter?.addLine()


//                horizonPrinter?.addString("TAXPAYER NAME: ${submission?.name}")
//                horizonPrinter?.endLine()
                horizonPrinter?.addText(
                    "TAXPAYER NAME: ${submission?.name}",
                    TextFormat(align = Align.LEFT)
                )
                horizonPrinter?.addLine()

//                horizonPrinter?.addString("PHONE NO: " + submission?.mobile)
//                horizonPrinter?.endLine()
                horizonPrinter?.addText(
                    "PHONE NO: " + submission?.mobile,
                    TextFormat(align = Align.LEFT)
                )
                horizonPrinter?.addLine()

//                horizonPrinter?.addString("CATEGORY: " + submission?.group)
//                horizonPrinter?.endLine()
                horizonPrinter?.addText(
                    "CATEGORY: " + submission?.group,
                    TextFormat(align = Align.LEFT)
                )
                horizonPrinter?.addLine()

//                horizonPrinter?.addString("SUB CATEGORY: " + submission?.subGroup)
//                horizonPrinter?.endLine()
                horizonPrinter?.addText(
                    "SUB CATEGORY: " + submission?.subGroup,
                    TextFormat(align = Align.LEFT)
                )
                horizonPrinter?.addLine()

////                if (poACapture?.vehiclePlate?.isNotEmpty() == true) {
//  //                  horizonPrinter?.addString("VEHICLE PLATE NO: " + poACapture.vehiclePlate)
//    //                horizonPrinter?.endLine()
//      //          }

//                horizonPrinter?.addString("AMOUNT PAID: " + "NGN " + submission?.amountPaid?.format(2, false))
//                horizonPrinter?.endLine()
                horizonPrinter?.addText(
                    "AMOUNT PAID: " + "NGN " + submission?.amountPaid?.format(2, false),
                    TextFormat(align = Align.LEFT)
                )
                horizonPrinter?.addLine()


//                horizonPrinter?.addString("CARD(s) USED:")
//                horizonPrinter?.endLine()
                horizonPrinter?.addText( "CARD(s) USED:",TextFormat(align = Align.LEFT))
                horizonPrinter?.addLine()


                JsonParser().parse(submission?.scratchCard).asJsonArray?.let { array ->
                    array.forEach { card ->
//                        horizonPrinter?.addString(card.asJsonObject["card"].asString + " : NGN " + card.asJsonObject["amount"].asDouble.format(2, false))
//                        horizonPrinter?.endLine()
                        horizonPrinter?.addText( card.asJsonObject["card"].asString + " : NGN " + card.asJsonObject["amount"].asDouble.format(2, false),TextFormat(align = Align.LEFT))
                        horizonPrinter?.addLine()
                    }
                }

                val submissionDate = Date(submission?.submissionTime ?: 0)
                if (submission?.menuSection?.equals("Mobile Location", false) == true) {
//                    horizonPrinter?.addString("VALIDITY: " + submissionDate.format("dd/MMM/yyyy"))
                    horizonPrinter?.addText( "VALIDITY: " + submissionDate.format("dd/MMM/yyyy"),TextFormat(align = Align.LEFT))
                } else if (submission?.menuSection?.equals("Presumptive Taxes", false) == true) {
                    val c = Calendar.getInstance()
                    c.time = submissionDate
                    c.add(Calendar.YEAR, 1)
                    c.add(Calendar.DAY_OF_YEAR, -1)
                    val oneYearLater = c.time
//                    horizonPrinter?.addString("VALIDITY: " + oneYearLater.format("dd/MMM/yyyy"))
                    horizonPrinter?.addText( "VALIDITY: " + oneYearLater.format("dd/MMM/yyyy"),TextFormat(align = Align.LEFT))
                }
//                horizonPrinter?.endLine()
                horizonPrinter?.addLine()


//  //              val distribution = SQLite.select().from(SubSectorAgencyDistribution::class.java)
//    //                    .where(SubSectorAgencyDistribution_Table.lgaSubSectorId.eq(poACapture?.lgaSubSectorId))
//    //                    .queryList()
////
//  //              if (distribution.isNotEmpty()) {
//    //                horizonPrinter?.setBold(true)
//      //              horizonPrinter?.addString("Stakeholders:")
//        //            horizonPrinter?.setBold(false)
//          //          horizonPrinter?.endLine()
////
//  //                  distribution.forEach { dist ->
//    //                    horizonPrinter?.addString("${dist.agencyName}: NGN ${dist.priceValue.format(2, false)}")
//      //                  horizonPrinter?.endLine()
//        //            }
//          //      }

//               horizonPrinter?.setAlgin(HorizonPrinter.ALGIN_MIDDLE)
//              horizonPrinter?.endLine()
//              horizonPrinter?.addString("--------------------")
//               horizonPrinter?.endLine()
//                horizonPrinter?.addString("Contribute to the growth of Edo State by paying your tax")
//                horizonPrinter?.endLine()
//                horizonPrinter?.addString("Call 08130970146 for complain or enquiry")
//
//                horizonPrinter?.printString()
//                horizonPrinter?.walkPaper(10)

                horizonPrinter?.addLine()
                horizonPrinter?.addText(
                    "--------------------",
                    TextFormat(align = Align.CENTER)
                )
                horizonPrinter?.addLine()
                horizonPrinter?.addText(
                    "Contribute to the growth of Edo State by paying your tax",
                    TextFormat(align = Align.CENTER)
                )
                horizonPrinter?.addLine()
                horizonPrinter?.addText(
                    "Call 08130970146 for complain or enquiry",
                    TextFormat(align = Align.CENTER)
                )
                horizonPrinter?.addLine()

                horizonPrinter?.feedLine(3)

                when (val status = horizonPrinter?.print()) {
                    Printer.Status.OK -> {
//                        callback?.invoke(status)
                        println("TIM::: - horizonPrinter?.print() OK $status")
                    }
                    else -> {
                        println("TIM::: - horizonPrinter?.print() NOT OK $status")
//                        callback?.invoke(status)
                    }
                }

                it.onNext(Unit)
                it.onComplete()
            } catch (e: Exception) {
                it.onError(e)
            }
        }, BackpressureStrategy.BUFFER)
    }

    fun printHaulage(
            horizonPrinter: Printer?,
            haulage: Haulage?,
            logo: Bitmap,
            collector: String?
    ): Flowable<Unit> {
        return Flowable.create({
            try {
//                horizonPrinter?.reset()
//                horizonPrinter?.setAlgin(HorizonPrinter.ALGIN_MIDDLE)
//                horizonPrinter?.setTextSize(20)
//                horizonPrinter?.setGray(5)
//
//                horizonPrinter?.printLogo(logo, false)
//                horizonPrinter?.walkPaper(5)

                horizonPrinter?.feedLine(1)

                logo.let {
                    horizonPrinter?.addImage(logo, logo.width, logo.height, 90)
                }

// //               horizonPrinter?.addString("EDO STATE GOVERNMENT")
// //               horizonPrinter?.endLine()

                Lga.byAreaId(haulage?.areaId ?: 0)?.let { lga ->
//                    horizonPrinter?.addString(lga.areaName.toUpperCase())
//                    horizonPrinter?.endLine()
                    horizonPrinter?.addText(
                        lga.areaName.toUpperCase(),
                        TextFormat(align = Align.CENTER)
                    )
                    horizonPrinter?.addLine()
                }

//                horizonPrinter?.setBold(true)
//                horizonPrinter?.addString("PAYMENT RECEIPT")
//                horizonPrinter?.setBold(false)
//                horizonPrinter?.endLine()
//                horizonPrinter?.printString()

                horizonPrinter?.addText(
                    "PAYMENT RECEIPT",
                    TextFormat(FontSize.LARGE, Align.CENTER)
                )
                horizonPrinter?.addLine()


//                horizonPrinter?.setAlgin(HorizonPrinter.ALGIN_LEFT)
//
//                horizonPrinter?.setBold(true)
//                horizonPrinter?.addString("DATE: ${Date(haulage?.submissionTime
//                        ?: 0).format("MMM, dd yyyy hh:mm:ss AA")}")
//
//                horizonPrinter?.setBold(false)
//                horizonPrinter?.endLine()

                horizonPrinter?.addText(
                    "DATE: ${Date(haulage?.submissionTime
                        ?: 0).format("MMM, dd yyyy hh:mm:ss AA")}",
                    TextFormat(FontSize.LARGE, Align.LEFT)
                )
                horizonPrinter?.addLine()



//                horizonPrinter?.addString("COLLECTOR’S NAME: $collector")
//                horizonPrinter?.endLine()
                horizonPrinter?.addText(
                    "COLLECTOR’S NAME: $collector",
                    TextFormat(align = Align.LEFT)
                )
                horizonPrinter?.addLine()

//                horizonPrinter?.addString("TAXPAYER NAME: ${haulage?.name}")
//                horizonPrinter?.endLine()
                horizonPrinter?.addText(
                    "TAXPAYER NAME: ${haulage?.name}",
                    TextFormat(align = Align.LEFT)
                )
                horizonPrinter?.addLine()

//                horizonPrinter?.addString("PHONE NO: " + haulage?.mobile)
//                horizonPrinter?.endLine()

                horizonPrinter?.addText(
                    "PHONE NO: " + haulage?.mobile,
                    TextFormat(align = Align.LEFT)
                )
                horizonPrinter?.addLine()

                Beat.byBeatId(haulage?.beatId ?: 0)?.let { beat ->
//                    horizonPrinter?.addString("BEAT: " + beat.beatName)
//                    horizonPrinter?.endLine()
                    horizonPrinter?.addText(
                        "BEAT: " + beat.beatName,
                        TextFormat(align = Align.LEFT)
                    )
                    horizonPrinter?.addLine()


//                    horizonPrinter?.addString("REVENUE TYPE: " + beat.revenueTypeName)
//                    horizonPrinter?.endLine()
                    horizonPrinter?.addText(
                        "REVENUE TYPE: " + beat.revenueTypeName,
                        TextFormat(align = Align.LEFT)
                    )
                    horizonPrinter?.addLine()
                }

////                if (poACapture?.vehiclePlate?.isNotEmpty() == true) {
//  ///                  horizonPrinter?.addString("VEHICLE PLATE NO: " + poACapture.vehiclePlate)
//     //               horizonPrinter?.endLine()
//       //         }

//         //       horizonPrinter?.addString("AMOUNT PAID: " + "NGN " + submission?.amountPaid?.format(2, false))
//           //     horizonPrinter?.endLine()

//             //   val distribution = SQLite.select().from(SubSectorAgencyDistribution::class.java)
//               //         .where(SubSectorAgencyDistribution_Table.lgaSubSectorId.eq(poACapture?.lgaSubSectorId))
//                 //       .queryList()
////
//  //              if (distribution.isNotEmpty()) {
//    //                horizonPrinter?.setBold(true)
//      //              horizonPrinter?.addString("Stakeholders:")
//        //            horizonPrinter?.setBold(false)
//          //          horizonPrinter?.endLine()
////
//  //                  distribution.forEach { dist ->
//    //                    horizonPrinter?.addString("${dist.agencyName}: NGN ${dist.priceValue.format(2, false)}")
//      //                  horizonPrinter?.endLine()
//        //            }
//          //      }

//                horizonPrinter?.setAlgin(HorizonPrinter.ALGIN_MIDDLE)
//                horizonPrinter?.endLine()
//                horizonPrinter?.addString("--------------------")
//                horizonPrinter?.endLine()
//                horizonPrinter?.addString("Contribute to the growth of Edo State by paying your tax")
//                horizonPrinter?.endLine()
//                horizonPrinter?.addString("Call 08130970146 for complain or enquiry")
//
//                horizonPrinter?.printString()
//
//                horizonPrinter?.walkPaper(10)


                horizonPrinter?.addLine()
                horizonPrinter?.addText(
                    "--------------------",
                    TextFormat(align = Align.CENTER)
                )
                horizonPrinter?.addLine()
                horizonPrinter?.addText(
                    "Contribute to the growth of Edo State by paying your tax",
                    TextFormat(align = Align.CENTER)
                )
                horizonPrinter?.addLine()
                horizonPrinter?.addText(
                    "Call 08130970146 for complain or enquiry",
                    TextFormat(align = Align.CENTER)
                )
                horizonPrinter?.addLine()

                horizonPrinter?.feedLine(3)

                when (val status = horizonPrinter?.print()) {
                    Printer.Status.OK -> {
//                        callback?.invoke(status)
                        println("TIM::: - horizonPrinter?.print() OK $status")
                    }
                    else -> {
                        println("TIM::: - horizonPrinter?.print() NOT OK $status")
//                        callback?.invoke(status)
                    }
                }


                it.onNext(Unit)
                it.onComplete()
            } catch (e: Exception) {
                it.onError(e)
            }
        }, BackpressureStrategy.BUFFER)
    }
}