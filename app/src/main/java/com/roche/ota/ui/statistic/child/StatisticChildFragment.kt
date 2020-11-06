package com.roche.ota.ui.statistic.child

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.roche.ota.R
import com.roche.ota.base.BaseFragment
import com.roche.ota.model.bean.BleModel
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadmoreListener
import kotlinx.android.synthetic.main.fragment_statistic_child.*


/**
 * @author madreain
 * @date
 * module：
 * description：
 */

class StatisticChildFragment : BaseFragment<StatisticChildPresenter>(), IStatisticChildView {

    private lateinit var updateAdapter: BaseQuickAdapter<BleModel, BaseViewHolder>

    override fun layoutId(): Int {
        return R.layout.fragment_statistic_child
    }

    override val mPresenter: StatisticChildPresenter by lazy {
        StatisticChildPresenter().apply {
            attachView(this@StatisticChildFragment)
        }
    }

    override fun initView() {
        val mList = mutableListOf<BleModel>()


        for (index in 1..20){
            mList.add(BleModel("123456789$index","123456789$index"))
        }


        updateAdapter =
            object : BaseQuickAdapter<BleModel, BaseViewHolder>(R.layout.item_ble_item, mList) {
                override fun convert(helper: BaseViewHolder?, item: BleModel?) {
                    helper?.setText(R.id.tv_ble_name, "蓝牙名称：" + item?.name)
                    helper?.setText(R.id.tv_ble_mac, "蓝牙地址：" + item?.mac)
                }
            }


        recycle_update.layoutManager = LinearLayoutManager(activity)
        recycle_update.adapter = updateAdapter

        refreshLayout.setOnRefreshLoadmoreListener(object :OnRefreshLoadmoreListener{
            override fun onLoadmore(refreshlayout: RefreshLayout?) {
                refreshlayout?.finishLoadmore(2000/*,false*/)//传入false表示加载失败
            }

            override fun onRefresh(refreshlayout: RefreshLayout?) {
                refreshlayout?.finishRefresh(2000/*,false*/)//传入false表示刷新失败
            }

        })
    }

    override fun lazyLoadData() {

    }

    override fun showLoading() {
    }

    override fun dismissLoading() {
    }

    companion object {
        fun newInstance(): StatisticChildFragment {//调用这个函数，创建新的fragment

            val fragment = StatisticChildFragment()
            val bundle = Bundle()
            fragment.arguments = bundle
            return fragment
        }
    }

}
