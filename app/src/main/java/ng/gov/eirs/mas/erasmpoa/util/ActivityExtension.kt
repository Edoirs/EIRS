package ng.gov.eirs.mas.erasmpoa.util

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager

fun Activity.hideKeyboard() {
    hideKeyboard(currentFocus)
}

fun Activity.hideKeyboard(f: View?) {
    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    if (null != f && null != f.windowToken) {//&& EditText.class.isAssignableFrom(f.getClass())
        imm.hideSoftInputFromWindow(f.windowToken, 0)
    } else {
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
    }
}

fun Context.isPermissionGranted(vararg permissions: String): Boolean {
    var allGranted = true
    for (permission in permissions) {
        allGranted = allGranted and (ActivityCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED)
    }
    return allGranted
}

fun Activity.requestPermission(requestId: Int, vararg permissions: String) {
    ActivityCompat.requestPermissions(this, permissions, requestId)
}

fun Activity.isPermissionGranted(grantPermissions: Array<String>, grantResults: IntArray, permission: String): Boolean {
    for (i in grantPermissions.indices) {
        if (permission == grantPermissions[i]) {
            return grantResults[i] == PackageManager.PERMISSION_GRANTED
        }
    }
    return false
}
