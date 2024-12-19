package ng.gov.eirs.mas.erasmpoa.data

import android.content.Context
import android.content.Intent
import ng.gov.eirs.mas.erasmpoa.data.dao.Haulage
import ng.gov.eirs.mas.erasmpoa.data.dao.Submission
import ng.gov.eirs.mas.erasmpoa.printer.PrinterActivity

class FlavoredDevices {
    companion object {
        fun startPrinter(context: Context, submission: Submission): Intent? {
            val intent = Intent(context, PrinterActivity::class.java)
            intent.putExtra("submission", submission)
            return intent
        }

        fun startPrinter(context: Context, haulage: Haulage): Intent? {
            val intent = Intent(context, PrinterActivity::class.java)
            intent.putExtra("haulage", haulage)
            return intent
        }
    }
}