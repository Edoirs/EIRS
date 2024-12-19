package ng.gov.eirs.mas.erasmpoa.helper;

import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;

public abstract class SingleChoiceRecyclerViewAdapter<T, VH extends RecyclerView.ViewHolder> extends BaseRecyclerViewAdapter<T, VH> {

    private int mSelectedPosition;

    public SingleChoiceRecyclerViewAdapter(ArrayList<T> dataSet) {
        super(dataSet);
        mSelectedPosition = -1;
    }

    protected void setSelected(int position) {
        int oldPosition = mSelectedPosition;
        mSelectedPosition = position;

        notifyItemChanged(position);
        notifyItemChanged(oldPosition);
    }

    public int getSelectedItemPosition() {
        return mSelectedPosition;
    }

    public T getSelectedItem() {
        if (mSelectedPosition != -1) return getItem(mSelectedPosition);
        else return null;
    }
}
