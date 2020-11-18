package com.roche.ota.ui.statistic.detail

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.kingja.loadsir.core.LoadService
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.core.BasePopupView
import com.lxj.xpopup.interfaces.SimpleCallback
import com.roche.ota.R
import com.roche.ota.base.BaseActivity
import com.roche.ota.ext.hideSoftKeyboard
import com.roche.ota.ext.loadServiceInit
import com.roche.ota.ext.showEmpty
import com.roche.ota.ext.showError
import com.roche.ota.model.bean.BleModel
import com.roche.ota.model.response.HotelDetailResponse
import com.roche.ota.model.response.HotelDetailResultRecord
import com.roche.ota.model.response.HotelListResponse
import com.roche.ota.utils.HexUtil
import com.roche.ota.utils.StatusBarUtil
import com.roche.ota.utils.showToast
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadmoreListener
import kotlinx.android.synthetic.main.activity_statistic_detail.*
import kotlinx.android.synthetic.main.activity_statistic_detail.iv_list_flag
import kotlinx.android.synthetic.main.fragment_statistic.*

class StatisticDetailActivity : BaseActivity<StatisticDetailPresenter>(), IStatisticDetailView {

    //界面状态管理者
    private lateinit var loadsir: LoadService<Any>

    var pageNumber = 1
    var pageSize = 20
    var total = -1
    lateinit var xPopupRoom: BasePopupView
    lateinit var xPopupUpdate: BasePopupView

    var mStatus: String? = null
    var mHotelId: String? = null
    var mRoomId: String? = null
    var mDevId: String? = null
    var status = 0

    private lateinit var mAdapter: BaseQuickAdapter<HotelDetailResultRecord, BaseViewHolder>
    override val mPresenter: StatisticDetailPresenter by lazy {
        StatisticDetailPresenter().apply {
            attachView(this@StatisticDetailActivity)
        }
    }

    override fun layoutId(): Int = R.layout.activity_statistic_detail


    override fun initView() {

        StatusBarUtil.setWindowStatusBarColor(this, R.color.backgroundColor)

        val type = intent.getStringExtra("type")
        if (type == "bind") {
            mHotelId = intent.getStringExtra("hotelId")

        }

        loadsir = loadServiceInit(refreshLayout_device) {
            //点击重试时触发的操作
            showWaitingDialog(getString(R.string.loading_text))
            refreshLayout_device.autoRefresh()

        }






        mAdapter =
            object : BaseQuickAdapter<HotelDetailResultRecord, BaseViewHolder>(
                R.layout.item_list_device,
                null
            ) {
                override fun convert(helper: BaseViewHolder?, item: HotelDetailResultRecord?) {
                    helper?.setText(
                        R.id.tv_dev_room,
                        if (item?.roomCode.isNullOrBlank()) "未知房间号" else item?.roomCode
                    )
                    helper?.setText(R.id.tv_dev_hotel, "所属酒店：" + (item?.hotelName ?: "未绑定酒店"))
                    helper?.setText(R.id.tv_dev_manage, "酒店管理员：" + (item?.hotelManager ?: "未知管理员"))
                    helper?.setText(R.id.tv_dev_code, "设备号：" + item?.devId)
                    helper?.setText(
                        R.id.tv_dev_versions,
                        "版本号：" + item?.initBtCode
                    )
                    helper?.setText(R.id.tv_list_model, "机型："+item?.model)
                    when (item?.status) {
                        0 -> helper?.setText(R.id.tv_dev_update_state, "未升级")
                        1 -> helper?.setText(R.id.tv_dev_update_state, "已升级")
                        2 -> helper?.setText(R.id.tv_dev_update_state, "不可升级")
                    }

                }
            }


        recycle_device.layoutManager = LinearLayoutManager(this@StatisticDetailActivity)
        recycle_device.adapter = mAdapter

        refreshLayout_device.setOnRefreshLoadmoreListener(object : OnRefreshLoadmoreListener {
            override fun onLoadmore(refreshlayout: RefreshLayout?) {
                if (pageNumber * pageSize <= total) {
                    pageNumber++
                    mPresenter.getDevDetail(
                        mStatus,
                        mHotelId,
                        mRoomId,
                        mDevId,
                        null,
                        pageNumber,
                        pageSize
                    )
                } else {
                    showToast("没有更多数据啦")
                    refreshlayout?.finishLoadmore()//
                }
            }

            override fun onRefresh(refreshlayout: RefreshLayout?) {
                pageNumber = 1
                mPresenter.getDevDetail(
                    mStatus,
                    mHotelId,
                    mRoomId,
                    mDevId,
                    null,
                    pageNumber,
                    pageSize
                )
            }

        })



        et_search_room.setOnEditorActionListener(object : TextView.OnEditorActionListener {
            override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    hideSoftKeyboard(this@StatisticDetailActivity)
                    pageNumber = 1
                    when (status) {
                        //房间号
                        0 -> {
                            mRoomId = et_search_room.text.toString().trim()
                            mDevId = null
                            if (mRoomId.isNullOrBlank()) {

                                showToast("请输入搜索内容")
                            } else {
                                showWaitingDialog(getString(R.string.loading_text))
                                mPresenter.getDevDetail(
                                    mStatus,
                                    mHotelId,
                                    mRoomId,
                                    mDevId,
                                    null,
                                    pageNumber,
                                    pageSize
                                )
                            }


                        }
                        //设备号
                        1 -> {
                            mDevId = et_search_room.text.toString().trim()
                            mRoomId = null
                            if (mDevId.isNullOrBlank()) {
                                showToast("请输入搜索内容")
                            } else {
                                showWaitingDialog(getString(R.string.loading_text))
                                mPresenter.getDevDetail(
                                    mStatus,
                                    mHotelId,
                                    mRoomId,
                                    mDevId,
                                    null,
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

        et_search_room.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (p0.toString().trim().isEmpty()) {
                    iv_room_clean.visibility = View.GONE
                    hideSoftKeyboard(this@StatisticDetailActivity)
                    mRoomId = null
                    mDevId = null
                    refreshLayout_device.autoRefresh()
                } else {
                    iv_room_clean.visibility = View.VISIBLE
                }
            }

        })


    }

    override fun initData() {

        refreshLayout_device.autoRefresh()

        //房间号 设备号
        xPopupRoom = XPopup.Builder(this)
            .setPopupCallback(object : SimpleCallback() {
                override fun beforeDismiss(popupView: BasePopupView?) {
                    iv_device_flag.setImageResource(R.drawable.icon_up_san)
                }
            })
            .hasShadowBg(false)
            .dismissOnBackPressed(false)
            .atView(tv_device_select)  // 依附于所点击的View，内部会自动判断在上方或者下方显示
            .asAttachList(
                arrayOf("房间号", "设备号"),
                null
            ) { position, text ->
                when (position) {
                    0 -> status = 0
                    1 -> status = 1
                }

                tv_device_select.text = text
                iv_device_flag.setImageResource(R.drawable.icon_up_san)
            }



        ll_device_select.setOnClickListener {
            xPopupRoom.show()
            iv_device_flag.setImageResource(R.drawable.icon_down_san)
        }

        //已升级 未升级
        xPopupUpdate = XPopup.Builder(this)
            .setPopupCallback(object : SimpleCallback() {
                override fun beforeDismiss(popupView: BasePopupView?) {
                    iv_list_flag.setImageResource(R.drawable.icon_up_san)
                }
            })
            .hasShadowBg(false)
            .dismissOnBackPressed(false)
            .atView(tv_update_select)  // 依附于所点击的View，内部会自动判断在上方或者下方显示
            .asAttachList(
                arrayOf("全部", "未升级", "已升级", "不可升级"),
                null
            ) { position, text ->
                when (position) {
                    0 -> mStatus = null
                    1 -> mStatus = "0"
                    2 -> mStatus = "1"
                    3 -> mStatus = "2"
                }
                refreshLayout_device.autoRefresh()

                tv_update_select.text = text
                iv_list_flag.setImageResource(R.drawable.icon_up_san)


            }

        ll_update_select.setOnClickListener {
            xPopupUpdate.show()
            iv_list_flag.setImageResource(R.drawable.icon_down_san)
        }

        iv_device_back.setOnClickListener {
            finish()
        }

        iv_room_clean.setOnClickListener {
            et_search_room.setText("")
        }
    }

    override fun showLoading() {
        showWaitingDialog(getString(R.string.loading_text))
    }

    override fun dismissLoading() {
        hideWaitingDialog()
    }

    override fun onGetHotelDetailSucceed(response: HotelDetailResponse) {

        refreshLayout_device.finishRefresh()
        refreshLayout_device.finishLoadmore()

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

    override fun onGetHotelDetailError(error: String, errorCode: Int) {
        Log.e(TAG, error)
        loadsir.showError(error)
        showToast(error)
    }
}
