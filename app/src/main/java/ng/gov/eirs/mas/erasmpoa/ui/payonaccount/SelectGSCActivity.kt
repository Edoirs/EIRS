package ng.gov.eirs.mas.erasmpoa.ui.payonaccount

import android.content.Intent
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.gson.reflect.TypeToken
import com.raizlabs.android.dbflow.annotation.Collate
import com.raizlabs.android.dbflow.rx2.language.RXSQLite
import com.raizlabs.android.dbflow.sql.language.SQLite
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_select_gsc.*
import kotlinx.android.synthetic.main.row_gsc.view.*
import ng.gov.eirs.mas.erasmpoa.R
import ng.gov.eirs.mas.erasmpoa.data.GsonProvider
import ng.gov.eirs.mas.erasmpoa.data.constant.Action
import ng.gov.eirs.mas.erasmpoa.data.dao.PriceSheet
import ng.gov.eirs.mas.erasmpoa.data.dao.PriceSheet_Table
import ng.gov.eirs.mas.erasmpoa.data.model.ApiResponse
import ng.gov.eirs.mas.erasmpoa.helper.BaseRecyclerViewAdapter
import ng.gov.eirs.mas.erasmpoa.net.ApiClient
import ng.gov.eirs.mas.erasmpoa.net.exception.NoInternetConnectionException
import ng.gov.eirs.mas.erasmpoa.sync.SyncTracker
import ng.gov.eirs.mas.erasmpoa.ui.base.BaseAppCompatActivity
import ng.gov.eirs.mas.erasmpoa.util.*
import java.net.SocketTimeoutException

class SelectGSCActivity : BaseAppCompatActivity() {

    private var mAdapter: ListAdapter? = null
    private var mMenuSection: String? = null
    private var mGroup: String? = null
    private var mSubGroup: String? = null

    private var mAction = Action.GROUP

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_gsc)
        setUpToolbar(toolbar)
        setHomeAsUp(true)

        recyclerView.setHasFixedSize(false)
        val layoutManager = android.support.v7.widget.LinearLayoutManager(activity)
        recyclerView.layoutManager = layoutManager

        ContextCompat.getDrawable(activity, R.drawable.divider_1dp_grey300)?.let {
            val decoration = android.support.v7.widget.DividerItemDecoration(activity, android.support.v7.widget.DividerItemDecoration.VERTICAL)
            decoration.setDrawable(it)
            recyclerView.addItemDecoration(decoration)
        }

        mAdapter = ListAdapter(ArrayList())
        recyclerView.adapter = mAdapter

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable) {
                ivSearchAction.isEnabled = s.isNotEmpty()
                mAdapter?.filter(getSearchQuery())
                updateSearchAction()
            }
        })

        ivSearchAction.setOnClickListener {
            etSearch.setText("")
            updateSearchAction()
        }

        intent.action?.let { mAction = it }

        mMenuSection = intent.getStringExtra("menuSection")
        mGroup = intent.getStringExtra("group")
        mSubGroup = intent.getStringExtra("subGroup")

        title = when (mAction) {
            Action.GROUP -> getString(R.string.select_group)
            Action.SUB_GROUP -> getString(R.string.select_sub_group)
            Action.CATEGORY -> getString(R.string.select_category)
            else -> getString(R.string.select_group)
        }

        loadList()
    }

    private fun updateSearchAction() {
        if (getSearchQuery().isEmpty()) {
            ivSearchAction.setImageResource(R.drawable.ic_search_white_24dp)
            hideKeyboard(etSearch)
        } else {
            ivSearchAction.setImageResource(R.drawable.ic_close_white_24dp)
        }
    }

    private fun getSearchQuery() = etSearch.text.toString()

    private fun loadList() {
        if (isConnectedToNetwork()) {
            SyncTracker.addSync(when (mAction) {
                Action.GROUP -> ApiClient.getClient(activity, true).psGroupList(getToken(), mMenuSection)
                Action.SUB_GROUP -> ApiClient.getClient(activity, true).psSubGroupList(getToken(), mMenuSection, mGroup)
                Action.CATEGORY -> ApiClient.getClient(activity, true).psCategoryList(getToken(), mMenuSection, mGroup, mSubGroup)
                else -> ApiClient.getClient(activity, true).psGroupList(getToken(), mMenuSection)
            }.compose<ApiResponse>(bindToLifecycle<ApiResponse>())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ response ->
                        progressBar.gone()
                        if (response.success == 1) {
                            val data = response.response?.data
                            val list = GsonProvider.getGson().fromJson<Array<String>>(data, object : TypeToken<Array<String>>() {}.type)
                            mAdapter?.add(list.toMutableList())

                            showEmptyView()
                        } else {
                            response.response?.message?.let {
                                when {
                                    it.isJsonObject -> coordinatorLayout.snackBar(R.string.please_try_again)
                                    it.isJsonPrimitive -> coordinatorLayout.snackBar(it.asString)
                                    else -> coordinatorLayout.snackBar(R.string.please_try_again)
                                }
                            }
                                    ?: kotlin.run { coordinatorLayout.snackBar(R.string.please_try_again) }
                        }
                    }, {
                        progressBar.gone()
                        showEmptyView()
                        if (it is SocketTimeoutException || it is NoInternetConnectionException) {
                            coordinatorLayout.snackBar(R.string.connection_error)
                        } else {
                            coordinatorLayout.snackBar(R.string.please_try_again)
                        }
                        it.printStackTrace()
                    }))
            progressBar.visible()
        } else {
            coordinatorLayout.snackBar(R.string.connection_error)
//            val query = when (mAction) {
//                Action.GROUP -> {
//                    SQLite.select(PriceSheet_Table.GroupName.distinct())
//                            .from(PriceSheet::class.java)
//                            .where(PriceSheet_Table.MenuSection.eq(mMenuSection).collate(Collate.NOCASE))
//                }
//                Action.SUB_GROUP -> {
//                    SQLite.select(PriceSheet_Table.SubGroupName.distinct())
//                            .from(PriceSheet::class.java)
//                            .where(PriceSheet_Table.MenuSection.eq(mMenuSection).collate(Collate.NOCASE))
//                            .and(PriceSheet_Table.GroupName.eq(mGroup).collate(Collate.NOCASE))
//                }
//                Action.CATEGORY -> {
//                    SQLite.select(PriceSheet_Table.CategoryName.distinct())
//                            .from(PriceSheet::class.java)
//                            .where(PriceSheet_Table.MenuSection.eq(mMenuSection).collate(Collate.NOCASE))
//                            .and(PriceSheet_Table.GroupName.eq(mGroup).collate(Collate.NOCASE))
//                            .and(PriceSheet_Table.SubGroupName.eq(mSubGroup).collate(Collate.NOCASE))
//                }
//                else -> {
//                    SQLite.select(PriceSheet_Table.GroupName.distinct())
//                            .from(PriceSheet::class.java)
//                            .where(PriceSheet_Table.MenuSection.eq(mMenuSection).collate(Collate.NOCASE))
//                }
//            }
//
//            SyncTracker.addSync(RXSQLite.rx(query)
//                    .query()
//                    .doFinally {
//                        progressBar.gone()
//                        showEmptyView()
//                    }
//                    .subscribe {
//                        val list = ArrayList<String>()
//                        while (it.moveToNext()) {
//                            list.add(it.getString(0))
//                        }
//                        it.close()
//                        mAdapter?.add(list)
//                    })
        }
    }

    private fun showEmptyView() {
        if (mAdapter?.itemCount == 0) {
            emptyView.visibility = View.VISIBLE
        } else {
            emptyView.visibility = View.GONE
        }
    }

    private inner class ListAdapter(list: MutableList<String>) : BaseRecyclerViewAdapter<String, ListAdapter.ViewHolder>(list) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.row_gsc, parent, false)
            return ViewHolder(v)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = getItem(position)

            val highlightColor = ContextCompat.getColor(activity, R.color.accent)

            val highlightedName = item.highlight(getSearchQuery(), highlightColor)
            holder.itemView.tvTitle.setText(highlightedName, TextView.BufferType.SPANNABLE)

            holder.itemView.setOnClickListener {
                if (mAction == Action.GROUP) {
                    val intent = Intent(activity, SelectGSCActivity::class.java)
                    intent.action = Action.SUB_GROUP
                    intent.putExtra("menuSection", mMenuSection)
                    intent.putExtra("group", item)
                    startActivity(intent)
                } else if (mAction == Action.SUB_GROUP) {
                    val intent = Intent(activity, SelectGSCActivity::class.java)
                    intent.action = Action.CATEGORY
                    intent.putExtra("menuSection", mMenuSection)
                    intent.putExtra("group", mGroup)
                    intent.putExtra("subGroup", item)
                    startActivity(intent)
                } else if (mAction == Action.CATEGORY) {
                    val intent = Intent(activity, SubmissionActivity::class.java)
                    intent.putExtra("menuSection", mMenuSection)
                    intent.putExtra("group", mGroup)
                    intent.putExtra("subGroup", mSubGroup)
                    intent.putExtra("category", item)
                    startActivity(intent)
                }
            }
        }

        var originalData: MutableList<String>? = null

        fun filter(query: String) {
            if (originalData == null) {
                originalData = dataSet.toMutableList()
            }
            clear()

            originalData?.forEach {
                if (it.contains(query, true)) {
                    add(it)
                }
            }
        }

        internal inner class ViewHolder(root: View) : android.support.v7.widget.RecyclerView.ViewHolder(root)
    }

}