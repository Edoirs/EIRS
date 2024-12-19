package ng.gov.eirs.mas.erasmpoa.util

import android.support.annotation.StringRes
import android.support.design.widget.Snackbar
import android.view.View
import android.view.animation.AnimationUtils
import ng.gov.eirs.mas.erasmpoa.R

fun View.gone() {
    visibility = View.GONE
}

fun View.visible() {
    visibility = View.VISIBLE
}

fun View.invisible() {
    visibility = View.INVISIBLE
}


fun View.snackBar(@StringRes msgRes: Int) {
    snackBar(context.getString(msgRes))
}

fun View.snackBar(msg: String) {
    Snackbar.make(this, msg, Snackbar.LENGTH_SHORT).show()
}


fun View.shake() {
    val shake = AnimationUtils.loadAnimation(context, R.anim.shake)
    this.startAnimation(shake)
}
