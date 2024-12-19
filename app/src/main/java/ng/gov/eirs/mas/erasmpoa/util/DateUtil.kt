package ng.gov.eirs.mas.erasmpoa.util

import android.text.format.DateFormat
import java.text.SimpleDateFormat
import java.util.*

var SERVER_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss"
var EIRS_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss"

fun Date.format(format: String): String = DateFormat.format(format, this).toString()
fun Long.toDate(): Date = Date(this)
fun String.toDate(pattern: String = SERVER_DATE_FORMAT): Date =
        SimpleDateFormat(pattern, Locale.getDefault()).parse(this)