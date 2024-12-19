package ng.gov.eirs.mas.erasmpoa.sync

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import ng.gov.eirs.mas.erasmpoa.ui.sync.SyncActivity
import ng.gov.eirs.mas.erasmpoa.ui.sync.SyncService
import ng.gov.eirs.mas.erasmpoa.util.isConnectedToNetwork
import ng.gov.eirs.mas.erasmpoa.util.saveSyncStatus

class NetworkMonitorReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context?.isConnectedToNetwork() == true) {
            context.saveSyncStatus(SyncActivity.getSyncables(false))

            val serviceIntent = Intent(context, SyncService::class.java)
            context.startService(serviceIntent)
        }
    }
}
