package com.roche.ota.ui.statistic

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.flyco.tablayout.listener.OnTabSelectListener
import com.kingja.loadsir.core.LoadService
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.core.BasePopupView
import com.lxj.xpopup.interfaces.SimpleCallback
import com.lxj.xpopup.interfaces.XPopupCallback
import com.roche.ota.R
import com.roche.ota.base.BaseFragment
import com.roche.ota.ext.*
import com.roche.ota.model.bean.BleModel
import com.roche.ota.model.response.*
import com.roche.ota.ui.mine.web.WebActivity
import com.roche.ota.ui.statistic.child.StatisticChildFragment
import com.roche.ota.ui.statistic.detail.StatisticDetailActivity
import com.roche.ota.utils.StatusBarUtil
import com.roche.ota.utils.showToast
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadmoreListener
import kotlinx.android.synthetic.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_statistic_detail.*
import kotlinx.android.synthetic.main.base_title_bar.*
import kotlinx.android.synthetic.main.fragment_statistic.*
import kotlinx.android.synthetic.main.fragment_statistic.iv_list_flag
import kotlinx.android.synthetic.main.fragment_statistic.refreshLayout


/**
 * @author madreain
 * @date
 * module：
 * description：
 */

class StatisticFragment : BaseFragment<StatisticPresenter>(), IStatisticView {


    //界面状态管理者
    private lateinit var loadsir: LoadService<Any>

    var pageNumber = 1
    var pageSize = 20

    var total = -1

    var status = 0

    var mSearchHotel: String? = null

    var mLastPartner: String? = null

    lateinit var xPopup: BasePopupView

    var isLoadmore = false

    private lateinit var mAdapter: BaseQuickAdapter<BindHotelResultRecord, BaseViewHolder>
    override fun layoutId(): Int {
        return R.layout.fragment_statistic
    }

    override val mPresenter: StatisticPresenter by lazy {
        StatisticPresenter().apply {
            attachView(this@StatisticFragment)
        }
    }

    override fun initView() {
        base_tv_title.text = "列表"


        loadsir = loadServiceInit(refreshLayout) {
            //点击重试时触发的操作
            showLoading()
            refreshLayout.autoRefresh()

        }



        mAdapter =
            object : BaseQuickAdapter<BindHotelResultRecord, BaseViewHolder>(
                R.layout.item_list_hotel,
                null
            ) {
                override fun convert(helper: BaseViewHolder?, item: BindHotelResultRecord?) {
                    helper?.setText(R.id.tv_hotelName, item?.hotelName)
                    helper?.setText(R.id.tv_hotelPartner, "合伙人：" + item?.lastPartner)
                    helper?.setText(R.id.tv_dev_total, "设备总数：" + item?.totalDevCount)
                    helper?.setText(R.id.tv_dev_noUpdate, "未升级设备数：" + item?.notUpgradeDevCount)
                    helper?.setText(R.id.tv_dev_update, "已升级设备数：" + item?.upgradeDevCount)
                    helper?.setText(R.id.tv_dev_neverUp, "不可升级设备数：" + item?.prohibitDevCount)
                }
            }


        recycle_list.layoutManager = LinearLayoutManager(activity)
        recycle_list.adapter = mAdapter

        refreshLayout.setOnRefreshLoadmoreListener(object : OnRefreshLoadmoreListener {
            override fun onLoadmore(refreshlayout: RefreshLayout?) {
                if (pageNumber * pageSize <= total) {
                    pageNumber++
                    mPresenter.getBindHotel(mSearchHotel, mLastPartner, pageNumber, pageSize)
                } else {
                    showToast("没有更多数据啦")
                    refreshlayout?.finishLoadmore()//
                }

            }

            override fun onRefresh(refreshlayout: RefreshLayout?) {
                pageNumber = 1
                mPresenter.getBindHotel(mSearchHotel, mLastPartner, pageNumber, pageSize)
                mPresenter.getUnbindHotel()
//                refreshlayout?.finishRefresh()//
            }

        })

        mAdapter.setOnItemClickListener { adapter, view, position ->
            val bindHotelResultRecord = adapter.getItem(position) as BindHotelResultRecord

            val intent = Intent(activity, StatisticDetailActivity::class.java)
            intent.putExtra("hotelId", bindHotelResultRecord.hotelId)
            intent.putExtra("type", "bind")
            startActivity(intent)

        }

        ll_unBind.setOnClickListener {
            val intent = Intent(activity, StatisticDetailActivity::class.java)
            intent.putExtra("type", "unbind")
            startActivity(intent)
        }


        ll_list_select.setOnClickListener {
            xPopup.show()
            iv_list_flag.setImageResource(R.drawable.icon_down_san)
        }

        iv_hotel_clean.setOnClickListener {

            et_search.setText("")
        }

        et_search.setOnEditorActionListener(object : TextView.OnEditorActionListener {
            override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    hideSoftKeyboard(activity)
                    when (status) {
                        0 -> {
                            mSearchHotel = et_search.text.toString().trim()
                            mLastPartner = null
                            if (mSearchHotel.isNullOrBlank()) {

                                showToast("请输入搜索内容")
                            } else {
                                showLoading()
                                mPresenter.getBindHotel(
                                    mSearchHotel,
                                    mLastPartner,
                                    pageNumber,
                                    pageSize
                                )
                            }


                        }
                        else -> {
                            mLastPartner = et_search.text.toString().trim()
                            mSearchHotel = null

                            if (mLastPartner.isNullOrBlank()) {
                                showToast("请输入搜索内容")
                            } else {
                                showLoading()
                                mPresenter.getBindHotel(
                                    mSearchHotel,
                                    mLastPartner,
                                    pageNumber,
                                    pageSize
                                )
                            }

                        }

                    }
                    return true
                }
                return false
            }
        })

        et_search.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (p0.toString().trim().isEmpty()) {
                    iv_hotel_clean.visibility=View.GONE
                    hideSoftKeyboard(activity)
                    mSearchHotel = null
                    mLastPartner = null
                    refreshLayout.autoRefresh()
                }else{
                    iv_hotel_clean.visibility=View.VISIBLE
                }
            }

        })


    }

    override fun lazyLoadData() {
        xPopup = XPopup.Builder(context)
            .setPopupCallback(object : SimpleCallback(){
                override fun beforeDismiss(popupView: BasePopupView?) {
                    iv_list_flag.setImageResource(R.drawable.icon_up_san)
                }
            })
            .hasShadowBg(false)
            .dismissOnBackPressed(false)
            .atView(tv_list_select)  // 依附于所点击的View，内部会自动判断在上方或者下方显示
            .asAttachList(
                arrayOf("酒店", "合伙人"), null
            ) { position, text ->
                tv_list_select.text = text
                iv_list_flag.setImageResource(R.drawable.icon_up_san)
                Log.e(TAG, "选择了：$position")

                status = when (position) {
                    0 -> 0
                    else -> 1
                }

            }

        refreshLayout.autoRefresh()
//        mPresenter.getBindHotel(mSearchHotel, mLastPartner, pageNumber, pageSize)

    }

    override fun showLoading() {
        showWaitingDialog(getString(R.string.loading_text))
    }

    override fun dismissLoading() {
        hideWaitingDialog()
    }

    companion object {
        fun newInstance(): StatisticFragment {//调用这个函数，创建新的fragment

            val fragment = StatisticFragment()
            val bundle = Bundle()
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onGetBindHotelSucceed(response: BindHotelResponse) {

        refreshLayout.finishRefresh()
        refreshLayout.finishLoadmore()

        total = response.result.total

        val currentPage = response.result.current
        if (currentPage > 1) {
            mAdapter.addData(response.result.records)
        } else {
            val record = response.result.records
            if (record.isEmpty()) {
                loadsir.showEmpty()
            } else {
                loadsir.showSuccess()
                mAdapter.setNewData(response.result.records)
            }

        }


    }

    override fun onGetBindHotelError(error: String, errorCode: Int) {
        refreshLayout.finishRefresh()
        refreshLayout.finishLoadmore()
        loadsir.showError(error)
        Log.e(TAG, error)
        showToast(error)
    }

    @SuppressLint("SetTextI18n")
    override fun onGetUnbindHotelSucceed(response: UnbindHotelResponse) {
        val total = response.result.totalDevCount
        if (total == 0) {
            ll_unBind.visibility = View.GONE
        } else {
            ll_unBind.visibility = View.VISIBLE
            tv_hotelPartner.text = "合伙人：" + response.result.lastPartner
            tv_dev_total.text = "设备总数：" + response.result.totalDevCount
            tv_dev_noUpdate.text = "未升级设备数：" + response.result.notUpgradeDevCount
            tv_dev_update.text = "已升级设备数：" + response.result.upgradeDevCount
            tv_dev_neverUp.text = "不可升级设备数：" + response.result.prohibitDevCount
        }


    }

    override fun onGetUnbindHotelError(error: String, errorCode: Int) {
        Log.e(TAG, error)
        showToast(error)
    }

}
