package ng.gov.eirs.mas.erasmpoa.data

import android.content.Context
import android.content.Intent
import ng.gov.eirs.mas.erasmpoa.data.dao.Haulage
import ng.gov.eirs.mas.erasmpoa.data.dao.Submission

class FlavoredDevices {
    companion object {
        fun startPrinter(context: Context, submission: Submission): Intent? {
            return null
        }

        fun startPrinter(context: Context, haulage: Haulage): Intent? {
            return null
        }
    }
}