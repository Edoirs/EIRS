package ng.gov.eirs.mas.erasmpoa.ui.base

import android.content.Intent
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import android.widget.TextView
import ng.gov.eirs.mas.erasmpoa.R
import ng.gov.eirs.mas.erasmpoa.sync.SyncTracker
import ng.gov.eirs.mas.erasmpoa.ui.haulage.HaulageActivity
import ng.gov.eirs.mas.erasmpoa.ui.haulage.UnsyncedHaulageActivity
import ng.gov.eirs.mas.erasmpoa.ui.home.TargetActivity
import ng.gov.eirs.mas.erasmpoa.ui.paybill.PayBillActivity
import ng.gov.eirs.mas.erasmpoa.ui.payonaccount.PayOnAccountActivity
import ng.gov.eirs.mas.erasmpoa.ui.payonaccount.UnsyncedPoaActivity
import ng.gov.eirs.mas.erasmpoa.ui.sync.SyncActivity
import ng.gov.eirs.mas.erasmpoa.util.getUser

abstract class BaseDrawerActivity : BaseAppCompatActivity() {

    private var mDrawer: DrawerLayout? = null
    private var mNavigationView: NavigationView? = null

    protected fun setUpDrawer(
        drawer: DrawerLayout,
        navigationView: NavigationView,
        toolbar: Toolbar
    ) {
        mDrawer = drawer
        mNavigationView = navigationView

        val toggle =
            ActionBarDrawerToggle(activity, mDrawer, toolbar, R.string.app_name, R.string.app_name)
        mDrawer?.addDrawerListener(toggle)
        toggle.syncState()

        mNavigationView?.setCheckedItem(R.id.navHome)

        mNavigationView?.setNavigationItemSelectedListener { onItemSelected(it) }
        mNavigationView?.getHeaderView(0)?.setOnClickListener { onClickProfile() }
    }

    override fun onStart() {
        super.onStart()
        val user = getUser()

        mNavigationView?.getHeaderView(0)?.findViewById<TextView>(R.id.tvHead)?.text =
            user?.fullName
        mNavigationView?.getHeaderView(0)?.findViewById<TextView>(R.id.tvSubhead)?.text =
            user?.email
    }

    private fun onClickProfile() {

    }

    override fun onBackPressed() {
        //handle the back press :D close the drawer first and if the drawer is closed close the activity
        if (mDrawer?.isDrawerOpen(GravityCompat.START) == true) {
            mDrawer?.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    private fun onItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.

        when (item.itemId) {
            R.id.navHome -> {

            }
            R.id.navPayBill -> {
                onClickPayBill()
            }
            R.id.navPayAccount -> {
                onClickPayOnAccount()
            }
            R.id.navHaulage -> {
                onClickHaulage()
            }
            R.id.navTarget -> {
                onClickTarget()
            }
            R.id.navUnsyncedPoa -> {
                onClickUnsyncedPoa()
            }
            R.id.navUnsyncedHaulage -> {
                onClickUnsyncedHaulage()
            }
            R.id.navSync -> {
                onClickSync()
            }
        }

        mDrawer?.closeDrawer(GravityCompat.START)
        return true
    }

    private fun onClickUnsyncedPoa() {
        val intent = Intent(activity, UnsyncedPoaActivity::class.java)
        startActivity(intent)
    }

    private fun onClickUnsyncedHaulage() {
        val intent = Intent(activity, UnsyncedHaulageActivity::class.java)
        startActivity(intent)
    }

    fun onClickSync() {
        val intent = Intent(activity, SyncActivity::class.java)
        startActivity(intent)
    }

    private fun onClickTarget() {
        val intent = Intent(activity, TargetActivity::class.java)
        startActivity(intent)
    }

    fun onClickPayBill() {
        val intent = Intent(activity, PayBillActivity::class.java)
        startActivity(intent)
    }

    fun onClickPayOnAccount() {
        if (SyncTracker.isOfflineEligible) {
            val intent = Intent(activity, PayOnAccountActivity::class.java)
            startActivity(intent)
        } else {
            AlertDialog.Builder(this).apply {
                setTitle(R.string.sync)
                setMessage(R.string.sync_expired)
                setPositiveButton(R.string.sync) { _, _ -> onClickSync() }
                setNegativeButton(R.string.cancel, null)
                show()
            }
        }
    }

    fun onClickHaulage() {
        val intent = Intent(activity, HaulageActivity::class.java)
        startActivity(intent)
    }

}
