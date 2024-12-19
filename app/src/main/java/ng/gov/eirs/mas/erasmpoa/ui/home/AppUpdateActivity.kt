package ng.gov.eirs.mas.erasmpoa.ui.home

import android.os.Bundle
import android.support.v7.app.AlertDialog
import ng.gov.eirs.mas.erasmpoa.R
import ng.gov.eirs.mas.erasmpoa.ui.base.BaseAppCompatActivity

class AppUpdateActivity : BaseAppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showUpdateDialog()
    }

    private fun showUpdateDialog() {
        val builder = AlertDialog.Builder(activity)
        builder.setTitle(R.string.update_available)
        builder.setMessage(R.string.a_new_version_is_available)
        builder.setPositiveButton(R.string.okay) { _, _ -> finish() }
        builder.setCancelable(false)
        builder.show()
    }

}
