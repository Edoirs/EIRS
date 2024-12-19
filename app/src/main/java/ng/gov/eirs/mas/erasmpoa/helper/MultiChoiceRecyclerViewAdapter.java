package ng.gov.eirs.mas.erasmpoa.helper;

import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;

import java.util.ArrayList;
import java.util.List;

public abstract class MultiChoiceRecyclerViewAdapter<T, VH extends RecyclerView.ViewHolder> extends BaseRecyclerViewAdapter<T, VH> {

    private SparseBooleanArray mSelectedPositions;

    public MultiChoiceRecyclerViewAdapter(ArrayList<T> dataSet) {
        super(dataSet);
        mSelectedPositions = new SparseBooleanArray();
    }


    /**
     * Indicates if the item at position position is selected
     *
     * @param position Position of the item to check
     * @return true if the item is selected, false otherwise
     */
    public boolean isSelected(int position) {
        return mSelectedPositions.get(position, false);
        // return getSelectedItems().contains(position);
    }

    /**
     * Toggle the selection status of the item at a given position
     *
     * @param position Position of the item to toggle the selection status for
     */
    public void toggleSelection(int position) {
        if (mSelectedPositions.get(position, false)) {
            mSelectedPositions.put(position, false);
        } else {
            mSelectedPositions.put(position, true);
        }
        notifyItemChanged(position);
    }

    protected void setSelected(int position, boolean selected) {
        mSelectedPositions.put(position, selected);
    }

    /**
     * Clear the selection status for all items
     */
    public void clearSelection() {
        List<Integer> selection = getSelectedItemPositions();
        mSelectedPositions.clear();
        for (Integer i : selection) {
            notifyItemChanged(i);
        }
    }

    /**
     * Selects  all items
     */
    public void selectAll() {
        for (int i = 0; i < getItemCount(); i++) {
            setSelected(i, true);
            notifyItemChanged(i);
        }
    }

    /**
     * deselects all items
     */
    public void deselectAll() {
        for (int i = 0; i < getItemCount(); i++) {
            setSelected(i, false);
            notifyItemChanged(i);
        }
    }

    /**
     * Count the selected items
     *
     * @return Selected items count
     */
    public int getSelectedItemCount() {
        return mSelectedPositions.size();
    }

    /**
     * Indicates the list of selected items
     *
     * @return List of selected items ids
     */
    public List<Integer> getSelectedItemPositions() {
        List<Integer> items = new ArrayList<>(mSelectedPositions.size());
        for (int i = 0; i < mSelectedPositions.size(); ++i) {
            items.add(mSelectedPositions.keyAt(i));
        }
        return items;
    }

    /**
     * Indicates the list of selected items
     *
     * @return List of selected items ids
     */
    public List<T> getSelectedItems() {
        List<T> items = new ArrayList<>(mSelectedPositions.size());
        for (int i = 0; i < mSelectedPositions.size(); ++i) {
            int selectedPos = mSelectedPositions.keyAt(i);
            if (mSelectedPositions.get(selectedPos, false)) items.add(getItem(selectedPos));
        }
        return items;
    }
}
