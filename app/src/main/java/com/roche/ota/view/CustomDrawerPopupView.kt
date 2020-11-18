package com.roche.ota.view

import android.content.Context
import android.util.Log
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.lxj.xpopup.core.CenterPopupView
import com.lxj.xpopup.core.DrawerPopupView
import com.roche.ota.R
import com.roche.ota.model.response.BindHotelResultRecord
import com.roche.ota.model.response.HotelModelResult
import kotlinx.android.synthetic.main.custom_drawer_pop.view.*

class CustomDrawerPopupView(context: Context) : CenterPopupView(context) {


    private  var mAdapter: BaseQuickAdapter<HotelModelResult, BaseViewHolder>

    override fun getImplLayoutId(): Int {
        return R.layout.custom_drawer_pop
    }

    init {
        Log.e("CustomDrawerPopupView", "init")
        mAdapter =
            object : BaseQuickAdapter<HotelModelResult, BaseViewHolder>(
                R.layout.item_pop,
                null
            ) {
                override fun convert(helper: BaseViewHolder?, item: HotelModelResult?) {
                    helper?.setText(R.id.tv_pop_model, item?.model)
                    helper?.setText(R.id.tv_pop_weishengji, item?.notUpgradeDevCount.toString())
                    helper?.setText(R.id.tv_pop_yishengji, item?.upgradeDevCount.toString())
                    helper?.setText(R.id.tv_pop_bukeshengji, item?.prohibitDevCount.toString())
                    helper?.setText(R.id.tv_pop_sum, item?.totalDevCount.toString())
//
                }
            }



    }

    override fun onCreate() {
        super.onCreate()
        Log.e("CustomDrawerPopupView", "onCreate")
        recycle_pop.layoutManager = LinearLayoutManager(context)
//        recycle_pop.addItemDecoration(
//            DividerItemDecoration(
//                context,
//                DividerItemDecoration.VERTICAL
//            )
//        )
        recycle_pop.adapter = mAdapter

    }

    fun setData(list: List<HotelModelResult>) {
        Log.e("CustomDrawerPopupView", "setData")
        mAdapter.setNewData(list)

    }

}