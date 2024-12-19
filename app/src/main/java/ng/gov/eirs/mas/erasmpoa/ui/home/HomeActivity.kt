package ng.gov.eirs.mas.erasmpoa.ui.home

import android.Manifest
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.support.v4.content.FileProvider
import android.support.v7.app.AlertDialog
import android.view.Menu
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_home.*
import ng.gov.eirs.mas.erasmpoa.BuildConfig
import ng.gov.eirs.mas.erasmpoa.R
import ng.gov.eirs.mas.erasmpoa.data.DB
import ng.gov.eirs.mas.erasmpoa.data.GsonProvider
import ng.gov.eirs.mas.erasmpoa.data.constant.RequestCode
import ng.gov.eirs.mas.erasmpoa.data.model.AppVersion
import ng.gov.eirs.mas.erasmpoa.net.ApiClient
import ng.gov.eirs.mas.erasmpoa.sync.NetworkMonitorReceiver
import ng.gov.eirs.mas.erasmpoa.sync.SyncTracker
import ng.gov.eirs.mas.erasmpoa.ui.base.BaseDrawerActivity
import ng.gov.eirs.mas.erasmpoa.util.*
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.nio.channels.FileChannel

class HomeActivity : BaseDrawerActivity() {

    private var networkMonitorReceiver: NetworkMonitorReceiver? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        setUpToolbar(toolbar)
        setHomeAsUp(false)

        setUpDrawer(drawerLayout, navigationView, toolbar)

        viewPayBill.setOnClickListener { onClickPayBill() }
        viewPayAccount.setOnClickListener { onClickPayOnAccount() }
        viewHaulage.setOnClickListener { onClickHaulage() }

        networkMonitorReceiver = NetworkMonitorReceiver()

        watchNetwork()
        checkVersion()
    }

    private fun watchNetwork() {
        val intentFilter = IntentFilter()
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION)
        registerReceiver(networkMonitorReceiver, intentFilter)
    }

    override fun onDestroy() {
        super.onDestroy()
        unwatchNetwork()
    }

    private fun unwatchNetwork() {
        try {
            unregisterReceiver(networkMonitorReceiver)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menu.add(R.string.logout).setOnMenuItemClickListener {
            checkSync()
            true
        }

        menu.add(R.string.about).setOnMenuItemClickListener {
            showAboutDialog()
            true
        }

//        menu.add("share db")?.setOnMenuItemClickListener {
//            export()
//            return@setOnMenuItemClickListener true
//        }
        return super.onCreateOptionsMenu(menu)
    }

    private fun showAboutDialog() {
        AlertDialog.Builder(activity).apply {
            setTitle(R.string.about)
            setMessage("${getString(R.string.app_name)}\n${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
            setPositiveButton(R.string.okay, null)
            show()
        }
    }

    private fun export() {
        if (activity?.isPermissionGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE) == true) {
            exportDB()
        } else {
            activity?.requestPermission(
                RequestCode.PERMISSION,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == RequestCode.PERMISSION) {
            if (activity?.isPermissionGranted(
                    permissions,
                    grantResults,
                    Manifest.permission.CAMERA
                ) == true
            ) {
                exportDB()
            } else {
                activity?.let { activity ->
                    val builder = AlertDialog.Builder(activity)
                    builder.setTitle(R.string.permission_access_required)
                    builder.setMessage(
                        getString(
                            R.string.permission_is_required,
                            getString(R.string.storage)
                        )
                    )
                    builder.setPositiveButton(R.string.settings) { _, _ ->
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        val uri = Uri.fromParts("package", activity.packageName, null)
                        intent.data = uri
                        startActivityForResult(intent, RequestCode.PERMISSION)
                    }
                    builder.setNegativeButton(R.string.cancel, null)
                    builder.show()
                }
            }
        }
    }

    private fun exportDB() {
        val sd = Environment.getExternalStorageDirectory()
        val data = Environment.getDataDirectory()
        val source: FileChannel
        val destination: FileChannel
        val currentDBPath = "/data/$packageName/databases/${DB.NAME}.db"
        val backupDBPath = "eirs.db"
        val currentDB = File(data, currentDBPath)
        val backupDB = File(sd, backupDBPath)
        try {
            source = FileInputStream(currentDB).channel
            destination = FileOutputStream(backupDB).channel
            destination.transferFrom(source, 0, source.size())
            source.close()
            destination.close()
            shareSkype(backupDB)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun isAppInstalled(packageName: String): Boolean {
        val packageManager = packageManager
        try {
            packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
        } catch (e: PackageManager.NameNotFoundException) {
            return false
        }
        return true
    }

    private fun shareSkype(dbFile: File) {
        if (!isAppInstalled(SKYPE_PACKAGE_NAME)) {
            shareFile(dbFile)
            return
        }
        val shareIntent = packageManager.getLaunchIntentForPackage(SKYPE_PACKAGE_NAME)
        shareIntent?.action = Intent.ACTION_SEND

        val uriForFile = if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
            Uri.fromFile(dbFile)
        } else {
            FileProvider.getUriForFile(activity, "$packageName.provider", dbFile)
        }

        shareIntent?.putExtra(Intent.EXTRA_STREAM, uriForFile)
        startActivity(shareIntent)
    }

    private fun shareFile(dbFile: File) {
        val sharingIntent = Intent(Intent.ACTION_SEND)
        // sharingIntent.type = "text/*"

        val uriForFile = if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
            Uri.fromFile(dbFile)
        } else {
            FileProvider.getUriForFile(activity, "$packageName.provider", dbFile)
        }

        sharingIntent.putExtra(Intent.EXTRA_STREAM, uriForFile)
        startActivity(Intent.createChooser(sharingIntent, "share file with"))
    }


    private fun checkSync() {
        if (SyncTracker.canLogout()) {
            confirmLogout()
        } else {
            showSyncWarning()
        }
    }

    private fun confirmLogout() {
        val builder = AlertDialog.Builder(activity)
        builder.setTitle(R.string.logout)
        builder.setMessage(R.string.msg_logout_confirm)
        builder.setPositiveButton(R.string.logout) { _, _ -> logout() }
        builder.setNegativeButton(R.string.cancel, null)
        builder.show()
    }

    private fun showSyncWarning() {
        val builder = AlertDialog.Builder(activity)
        builder.setTitle(R.string.sync_error)
        builder.setMessage(R.string.sync_logout)
        builder.setPositiveButton(R.string.sync) { _, _ -> onClickSync() }
        builder.setNegativeButton(R.string.cancel, null)
        builder.show()
    }

    private fun checkVersion() {
        SyncTracker.addSync(
            ApiClient.getClient(activity, true)
                .checkVersion(
                    getToken().orEmpty(),
                    getUser()?.employeeId ?: 0,
                    getUser()?.organizationId ?: 0
                )
                .compose(bindToLifecycle())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ response ->
                    if (response.success == 1) {
                        val data = response.response?.data?.asJsonObject
                        val appVersion =
                            GsonProvider.getGson().fromJson(data, AppVersion::class.java)
                        checkUpdate(appVersion)
                    }
                }, {
                })
        )
    }

    private fun checkUpdate(appVersion: AppVersion) {
        if (appVersion.versionCode > BuildConfig.VERSION_CODE) {
            showUpdateDialog()
        }
    }

    private fun showUpdateDialog() {
        val intent = Intent(activity, AppUpdateActivity::class.java)
        startActivity(intent)
    }

    companion object {
        private val SKYPE_PACKAGE_NAME = "com.skype.raider"
    }

}
