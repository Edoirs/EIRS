package ng.gov.eirs.mas.erasmpoa.util

import android.util.Log

object LogUtil {
    const val TAG = "LogTag"

    fun d(msg: String?) {
        d(TAG, msg ?: "null")
    }

    fun d(tag: String, msg: String?) {
        Log.d(tag, msg ?: "null")
    }
}
