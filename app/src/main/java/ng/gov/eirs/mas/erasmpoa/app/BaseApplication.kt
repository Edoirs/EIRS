package ng.gov.eirs.mas.erasmpoa.app

import android.app.Application
import android.content.Context
import android.support.multidex.MultiDex
import com.google.android.gms.security.ProviderInstaller
import com.raizlabs.android.dbflow.config.DatabaseConfig
import com.raizlabs.android.dbflow.config.FlowConfig
import com.raizlabs.android.dbflow.config.FlowLog
import com.raizlabs.android.dbflow.config.FlowManager
import ng.gov.eirs.mas.erasmpoa.BuildConfig
import ng.gov.eirs.mas.erasmpoa.data.DB

class BaseApplication : Application() {

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    override fun onCreate() {
        super.onCreate()

        val databaseConfig = DatabaseConfig.Builder(DB::class.java)
                .build()
        val flowConfig = FlowConfig.Builder(this)
                .addDatabaseConfig(databaseConfig)
                .build()
        FlowManager.init(flowConfig)
        if (BuildConfig.DEBUG) {
            FlowLog.setMinimumLoggingLevel(FlowLog.Level.V)
        }

        checkTls()
    }

    private fun checkTls() {
        try {
            ProviderInstaller.installIfNeeded(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
