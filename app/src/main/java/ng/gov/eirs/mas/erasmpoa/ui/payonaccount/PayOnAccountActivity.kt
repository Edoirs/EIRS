package ng.gov.eirs.mas.erasmpoa.ui.payonaccount

import android.content.Intent
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.activity_pay_on_account.*
import kotlinx.android.synthetic.main.row_menu_section.view.*
import ng.gov.eirs.mas.erasmpoa.R
import ng.gov.eirs.mas.erasmpoa.data.constant.Action
import ng.gov.eirs.mas.erasmpoa.data.model.MenuSection
import ng.gov.eirs.mas.erasmpoa.helper.BaseRecyclerViewAdapter
import ng.gov.eirs.mas.erasmpoa.ui.base.BaseAppCompatActivity
import ng.gov.eirs.mas.erasmpoa.util.getMenuSection

class PayOnAccountActivity : BaseAppCompatActivity() {

    private val mAdapter = ListAdapter(ArrayList())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pay_on_account)
        setUpToolbar(toolbar)
        setHomeAsUp(true)

        recycleView.setHasFixedSize(false)
        recycleView.layoutManager = android.support.v7.widget.LinearLayoutManager(activity)
        ContextCompat.getDrawable(activity, R.drawable.divider_1dp_grey300)?.let {
            val decoration = android.support.v7.widget.DividerItemDecoration(activity, android.support.v7.widget.DividerItemDecoration.VERTICAL)
            decoration.setDrawable(it)
            recycleView.addItemDecoration(decoration)
        }

        getMenuSection()?.asList()?.let { mAdapter.add(it) }
        recycleView.adapter = mAdapter
    }

    private inner class ListAdapter(list: MutableList<MenuSection>) : BaseRecyclerViewAdapter<MenuSection, ListAdapter.ItemHolder>(list) {

        override fun onCreateViewHolder(parent: ViewGroup, position: Int): ItemHolder {
            return ItemHolder(LayoutInflater.from(parent.context).inflate(R.layout.row_menu_section, parent, false))
        }

        override fun onBindViewHolder(holder: ItemHolder, position: Int) {
            val item = getItem(position)

            holder.itemView.tvMenuSection.text = item.menuSection
            holder.itemView.setOnClickListener {
                if (item.menuSection == "Mobile Location" || item.menuSection == "Presumptive Taxes") {
                    val intent = Intent(activity, SelectGSCActivity::class.java)
                    intent.action = Action.GROUP
                    intent.putExtra("menuSection", item.menuSection)
                    startActivity(intent)
                }
            }
        }

        inner class ItemHolder(root: View) : android.support.v7.widget.RecyclerView.ViewHolder(root)
    }
}
