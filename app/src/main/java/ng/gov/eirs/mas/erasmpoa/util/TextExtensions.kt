package ng.gov.eirs.mas.erasmpoa.util

import android.support.annotation.DrawableRes
import android.support.design.widget.TextInputLayout
import android.text.Spannable
import android.text.style.ForegroundColorSpan
import android.widget.EditText
import android.widget.TextView
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.text.NumberFormat

fun TextInputLayout.errorNull() {
    isErrorEnabled = false
    error = null
}

fun EditText.empty() {
    setText("")
}

fun TextView.setDrawableRightEnd(@DrawableRes drawableRes: Int) {
    setCompoundDrawablesWithIntrinsicBounds(0, 0, drawableRes, 0)
}

fun String.superTrim(): String {
    return trim { it <= ' ' }.replace("\\s{2,}".toRegex(), " ")
}

fun String.getInitials(): String {
    if (isEmpty()) return ""

    var initials = ""
    val split = superTrim().split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
    if (split.size >= 2) {
        if (split[0].isNotEmpty()) {
            initials += split[0].toUpperCase()[0]
        }
        if (split[1].isNotEmpty()) {
            initials += split[1].toUpperCase()[0]
        }
    } else {
        if (this.length >= 2) {
            initials += this.toUpperCase()[0]
            initials += this.toUpperCase()[1]
        } else {
            initials += this.toUpperCase()[0]
        }
    }
    return initials
}

fun getInitials(name1: String, name2: String): String {
    var initials = ""
    if (name1.trim { it <= ' ' }.isNotEmpty()) initials += name1.toUpperCase()[0]
    if (name2.trim { it <= ' ' }.isNotEmpty()) initials += name2.toUpperCase()[0]
    return initials
}

fun String.highlight(searchString: String?, highlightColor: Int): Spannable {
    searchString?.let { query ->
        if (!query.isBlank() && this.toLowerCase().contains(query.toLowerCase())) {
            // LogUtil.d(TAG, "setHighlightText: " + text + " contains: " + query);

            var startPos = this.toLowerCase().indexOf(query.toLowerCase(), 0)
            // val lastIndex = this.toLowerCase().lastIndexOf(query.toLowerCase())

            var hasMore: Boolean

            val spanText = Spannable.Factory.getInstance().newSpannable(this)
            do {
                val endPos = startPos + query.length
                // LogUtil.d(TAG, "setHighlightText: startPos=" + startPos + " lastIndex=" + lastIndex + " endPos=" + endPos);
                spanText.setSpan(ForegroundColorSpan(highlightColor), startPos, endPos, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

                startPos = this.toLowerCase().indexOf(query.toLowerCase(), endPos)
                hasMore = startPos != -1
            } while (hasMore)

            //tv.setText(spanText, TextView.BufferType.SPANNABLE);
            return spanText
        } else {
            return Spannable.Factory.getInstance().newSpannable(this)
        }
    } ?: run {
        return Spannable.Factory.getInstance().newSpannable(this)
    }
}

fun Double.format(scale: Int, grouping: Boolean = true): String {
    val numberFormat = NumberFormat.getInstance()
    numberFormat.maximumFractionDigits = scale
    numberFormat.isGroupingUsed = grouping
    return numberFormat.format(this)

    // return new BigDecimal(value).setScale(scale, BigDecimal.ROUND_HALF_EVEN).toString();
}

fun String.toDoubleOrZero(): Double {
    return if (this.toDoubleOrNull() == null) 0.toDouble()
    else this.toDouble()
}


fun String.toBearerHeader(): String = "Bearer $this"

fun String.md5(): String {
    val MD5 = "MD5"
    try { // Create MD5 Hash
        val digest = MessageDigest
                .getInstance(MD5)
        digest.update(this.toByteArray())
        val messageDigest = digest.digest()
        // Create Hex String
        val hexString = StringBuilder()
        for (aMessageDigest in messageDigest) {
            var h = Integer.toHexString(0xFF and aMessageDigest.toInt())
            while (h.length < 2) h = "0$h"
            hexString.append(h)
        }
        return hexString.toString()
    } catch (e: NoSuchAlgorithmException) {
        e.printStackTrace()
    }
    return ""
}