package ng.gov.eirs.mas.erasmpoa.ui.base;

import android.app.ProgressDialog;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.trello.rxlifecycle2.components.support.RxAppCompatActivity;

import ng.gov.eirs.mas.erasmpoa.util.ActivityExtensionKt;

public abstract class BaseAppCompatActivity extends RxAppCompatActivity {

    protected ProgressDialog mProgressDialog;

    protected void setUpToolbar(Toolbar toolbar) {
        setSupportActionBar(toolbar);
        setHomeAsUp(true);
    }

    protected void setHomeAsUp(boolean homeAsUp) {
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(homeAsUp);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityExtensionKt.hideKeyboard(this);
    }

    public AppCompatActivity getActivity() {
        return this;
    }
}
