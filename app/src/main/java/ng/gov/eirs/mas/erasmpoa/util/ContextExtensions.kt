package ng.gov.eirs.mas.erasmpoa.util

import android.content.Context
import android.content.Context.CONNECTIVITY_SERVICE
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.net.ConnectivityManager
import android.provider.Settings
import android.support.annotation.ColorRes
import android.support.annotation.StringRes
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.widget.Toast
import com.google.gson.reflect.TypeToken
import com.raizlabs.android.dbflow.sql.language.SQLite
import ng.gov.eirs.mas.erasmpoa.R
import ng.gov.eirs.mas.erasmpoa.data.ConfigData
import ng.gov.eirs.mas.erasmpoa.data.GsonProvider
import ng.gov.eirs.mas.erasmpoa.data.dao.*
import ng.gov.eirs.mas.erasmpoa.data.model.CollectionTarget
import ng.gov.eirs.mas.erasmpoa.data.model.MenuSection
import ng.gov.eirs.mas.erasmpoa.data.model.Syncable
import ng.gov.eirs.mas.erasmpoa.data.model.User
import ng.gov.eirs.mas.erasmpoa.sync.SyncTracker
import ng.gov.eirs.mas.erasmpoa.ui.auth.LoginActivity
import ng.gov.eirs.mas.erasmpoa.ui.home.HomeActivity
import ng.gov.eirs.mas.erasmpoa.util.ModelUtil.Companion.fromGsonString
import ng.gov.eirs.mas.erasmpoa.util.ModelUtil.Companion.toGsonString
import java.util.*

fun Context.isConnectedToNetwork(): Boolean {
    val cm = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
    val activeNetwork = cm.activeNetworkInfo
    return activeNetwork != null && activeNetwork.isConnected
}

fun Context.isCameraAvailable(): Boolean {
    return packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA)
}

fun Context.toast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

fun Context.toast(message: Int) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

fun Context.getUniqueId(): String {
    return Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
}

fun Context.setColorFilter(d: Drawable?, @ColorRes colorRes: Int): Drawable? {
    d?.setColorFilter(ContextCompat.getColor(this, colorRes), PorterDuff.Mode.SRC_ATOP)
    return d
}

fun Context.alertWithOkay(@StringRes title: Int, @StringRes msg: Int) {
    alertWithOkay(getString(title), getString(msg))
}

fun Context.alertWithOkay(title: String, msg: String) {
    AlertDialog.Builder(this).apply {
        setTitle(title)
        setMessage(msg)
        setPositiveButton(R.string.okay, null)
        show()
    }
}

fun Context.getSession(): SharedPreferences {
    return getSharedPreferences(ConfigData.USER_PREF, MODE_PRIVATE)
}

fun Context.saveToken(token: String) {
    getSession().edit().putString("token", token).apply()
}

fun Context.getToken(): String? {
    return getSession().getString("token", null)
}

fun Context.saveUser(user: User) {
    getSession().edit().putString("user", toGsonString(user)).apply()
}

fun Context.getUser(): User? {
    val user = getSession().getString("user", null)
    return if (user == null) null else fromGsonString(user)
}

fun Context.saveMenuSection(menuSection: Array<MenuSection>) {
    getSession().edit().putString("menuSection", toGsonString(menuSection)).apply()
}

fun Context.getMenuSection(): Array<MenuSection>? {
    val menuSection = getSession().getString("menuSection", null)
    return if (menuSection == null) null
    else GsonProvider.getGson()
        .fromJson<Array<MenuSection>>(menuSection, object : TypeToken<Array<MenuSection>>() {}.type)
}

fun Context.isLoggedIn(): Boolean {
    return getUser() != null
}

fun Context.navigateSession() {
    if (isLoggedIn()) {
//        if (getUser()?.passwordStatus == PasswordStatus.CREATE || getUser()?.passwordStatus == PasswordStatus.FORGOT) {
//            val intent = Intent(this, ResetPasswordActivity::class.java)
//            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
//            startActivity(intent)
//        } else {
        val intent = Intent(this, HomeActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
//        }
    }
}

fun Context.getTarget(): CollectionTarget? {
    val target = getSession().getString("target", null)
    return if (target == null) null else fromGsonString(target)
}

fun Context.saveTarget(target: CollectionTarget) {
    getSession().edit().putString("target", toGsonString(target)).apply()
}

fun Context.logout() {
    deleteAll()
    // SyncTracker.disposeAllSync()
    getSession().edit().clear().apply()
    SyncTracker.deleteAll(this)

    val intent = Intent(this, LoginActivity::class.java)
    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
    startActivity(intent)
}

fun deleteAll() {
    SQLite.delete().from(Submission::class.java).execute()
    SQLite.delete().from(PriceSheet::class.java).execute()
    SQLite.delete().from(TaxPayerType::class.java).execute()
    SQLite.delete().from(Lga::class.java).execute()
    SQLite.delete().from(ScratchCard::class.java).execute()

    SQLite.delete().from(Haulage::class.java).execute()
    SQLite.delete().from(Beat::class.java).execute()
}

fun Context.saveSyncStatus(syncables: List<Syncable>) {
    getSession().edit()
        .putString("syncables", GsonProvider.getGson().toJson(syncables))
        .apply()
}

fun Context.getSyncStatus(): ArrayList<Syncable> {
    val syncables = getSession().getString("syncables", null)
    return if (syncables.isNullOrBlank()) {
        ArrayList()
    } else {
        GsonProvider.getGson()
            .fromJson(syncables, object : TypeToken<ArrayList<Syncable>>() {}.type)
    }
}