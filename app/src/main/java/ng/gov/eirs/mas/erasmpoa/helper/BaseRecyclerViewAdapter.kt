package ng.gov.eirs.mas.erasmpoa.helper

abstract class BaseRecyclerViewAdapter<T, VH : android.support.v7.widget.RecyclerView.ViewHolder>(val dataSet: MutableList<T>) :
        android.support.v7.widget.RecyclerView.Adapter<VH>() {

    fun getItem(position: Int): T {
        return dataSet[position]
    }

    override fun getItemCount(): Int {
        return dataSet.size
    }

    fun add(item: T, notify: Boolean = true) {
        val positionStart = dataSet.size
        dataSet.add(item)
        if (notify) notifyItemInserted(positionStart)
    }

    fun add(position: Int, item: T, notify: Boolean = true) {
        dataSet.add(position, item)
        if (notify) notifyItemInserted(position)
    }

    fun add(list: List<T>, notify: Boolean = true) {
        //        int oldSize = dataSet.size();
        //        dataSet.addAll(list);
        //        int newSize = dataSet.size();
        //        notifyItemRangeInserted(oldSize, newSize);

        val startPosition = dataSet.size
        dataSet.addAll(list)
        if (notify) notifyItemRangeInserted(startPosition, list.size)
    }

    fun remove(item: T, notify: Boolean = true) {
        val position = dataSet.indexOf(item)
        if (position >= 0) removeAt(position, notify)
    }

    fun removeAt(position: Int, notify: Boolean = true) {
        dataSet.removeAt(position)
        if (notify) notifyItemRemoved(position)
        // notifyItemRangeChanged(position, dataSet.size());
    }

    fun clear(notify: Boolean = true) {
        val size = itemCount
        dataSet.clear()
        if (notify) notifyItemRangeRemoved(0, size)
    }

    fun isEmpty(): Boolean = dataSet.isEmpty()

    fun removeAll(notify: Boolean = true) {
        val size = dataSet.size
        if (size > 0) {
            for (i in 0 until size) {
                dataSet.removeAt(0)
            }
            if (notify) notifyItemRangeRemoved(0, size)
        }
    }
}