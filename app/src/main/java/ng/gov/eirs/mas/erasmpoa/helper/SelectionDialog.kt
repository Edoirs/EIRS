package ng.gov.eirs.mas.erasmpoa.helper

import android.content.Context
import android.support.v7.app.AlertDialog
import ng.gov.eirs.mas.erasmpoa.R

object SelectionDialog {

    fun <T> showDialog(context: Context, title: String, data: List<T>, osl: ((T) -> Unit)) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(title)
        val items = arrayOfNulls<String>(data.size)
        for (i in data.indices) {
            val t = data[i]
            items[i] = t.toString()
        }
        builder.setItems(items) { dialog, which -> osl.invoke(data[which]) }
        builder.setNegativeButton(R.string.cancel, null)
        builder.show()
    }

    fun <T> showDialogPair(context: Context, title: String, data: List<Pair<T, String>>, osl: ((Pair<T, String>) -> Unit)) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(title)
        val items = arrayOfNulls<String>(data.size)
        for (i in data.indices) {
            val (_, second) = data[i]
            items[i] = second
        }
        builder.setItems(items) { dialog, which -> osl.invoke(data[which]) }
        builder.setNegativeButton(R.string.cancel, null)
        builder.show()
    }
}
