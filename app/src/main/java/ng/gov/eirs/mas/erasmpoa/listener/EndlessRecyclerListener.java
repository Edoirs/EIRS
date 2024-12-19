package ng.gov.eirs.mas.erasmpoa.listener;

import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

/**
 * Created by Himanshu on 6/1/2015.
 */
public abstract class EndlessRecyclerListener extends RecyclerView.OnScrollListener {

    private int firstVisibleItem, visibleItemCount, totalItemCount;
    private LinearLayoutManager mLinearLayoutManager;
    private int previousTotal = 0;
    private boolean loading = true;
    private int mVisibleThreshold = 5;

    public EndlessRecyclerListener(LinearLayoutManager linearLayoutManager, int visibleThreshold) {
        mLinearLayoutManager = linearLayoutManager;
        mVisibleThreshold = visibleThreshold;
    }

    public EndlessRecyclerListener(LinearLayoutManager linearLayoutManager) {
        mLinearLayoutManager = linearLayoutManager;
    }

    public void resetListener() {
        firstVisibleItem = 0;
        visibleItemCount = 0;
        totalItemCount = 0;
        previousTotal = 0;
    }

    @Override
    public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);
        visibleItemCount = recyclerView.getChildCount();
        totalItemCount = mLinearLayoutManager.getItemCount();
        firstVisibleItem = mLinearLayoutManager.findFirstVisibleItemPosition();
        if (loading) {
            if (totalItemCount > previousTotal) {
                loading = false;
                previousTotal = totalItemCount;
            }
        }
        if (!loading && (totalItemCount - visibleItemCount) <= (firstVisibleItem + mVisibleThreshold)) {
            onNextPage();
            loading = true;
        }
    }

    public abstract void onNextPage();
}
