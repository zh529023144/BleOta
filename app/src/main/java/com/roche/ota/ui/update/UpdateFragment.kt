package com.roche.ota.ui.update


import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment

import com.roche.ota.R
import com.roche.ota.api.UrlConstant
import com.roche.ota.base.BaseFragment
import com.roche.ota.model.response.*
import com.roche.ota.ui.update.ota.OtaActivity2
import com.roche.ota.ui.update.scan.ScanActivity2
import com.roche.ota.utils.AppUtils
import com.roche.ota.utils.HexUtil
import com.roche.ota.utils.showToast
import com.yzq.zxinglibrary.android.CaptureActivity
import com.yzq.zxinglibrary.common.Constant
import kotlinx.android.synthetic.main.fragment_update.*

/**
 * A simple [Fragment] subclass.
 */
class UpdateFragment : BaseFragment<UpdatePresenter>(), IUpdateView {


    override fun layoutId(): Int {
        return R.layout.fragment_update
    }

    override val mPresenter: UpdatePresenter by lazy {
        UpdatePresenter().apply {
            attachView(this@UpdateFragment)
        }
    }

    override fun initView() {
        bn_manual.setOnClickListener {
            val isEnabled = BluetoothAdapter.getDefaultAdapter().isEnabled
            Log.e(TAG, "蓝牙开启状态：$isEnabled")
            if (!isEnabled) { //蓝牙未开启 直接开启   开启后  等待usb 指令连接蓝牙
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(enableBtIntent, 100)
            }else{
                startActivityForResult(Intent(activity, CaptureActivity::class.java), 300)
            }


        }
        bn_auto.setOnClickListener {
            val intent = Intent(activity, ScanActivity2::class.java)
            startActivity(intent)
        }
    }

    override fun lazyLoadData() {
//        val filter = IntentFilter()
//        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
//        activity?.registerReceiver(mBleReceiver, filter)
    }

    //打开蓝牙
    private fun requestBluetooth() {
        val isEnabled = BluetoothAdapter.getDefaultAdapter().isEnabled
        Log.e(TAG, "蓝牙开启状态：$isEnabled")
        if (!isEnabled) { //蓝牙未开启 直接开启   开启后  等待usb 指令连接蓝牙
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, 100)
        }
    }

//    //广播接受  usb 状态
//    private val mBleReceiver = object : BroadcastReceiver() {
//        override fun onReceive(context: Context, intent: Intent) {
//            when (intent.action) {
//                BluetoothAdapter.ACTION_STATE_CHANGED
//                -> {
//                    Log.e(TAG, "蓝牙状态  发生改变-------------------")
//                    val r = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1)
//                    if (r == BluetoothAdapter.STATE_OFF) {
//                        Log.e(TAG, "蓝牙关闭")
//                        requestBluetooth()
//                    } else if (r == BluetoothAdapter.STATE_ON) {
//                        Log.e(TAG, "蓝牙打开")
//                    }
//                }
//            }
//        }
//    }

//    override fun onDestroyView() {
//        super.onDestroyView()
//        activity?.unregisterReceiver(mBleReceiver)
//    }

    override fun showLoading() {
    }

    override fun dismissLoading() {
    }

    companion object {
        fun newInstance(): UpdateFragment {//调用这个函数，创建新的fragment

            val fragment = UpdateFragment()
            val bundle = Bundle()
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 300 && resultCode == Activity.RESULT_OK) {
            val result = data?.getStringExtra(Constant.CODED_CONTENT) ?: return
            Log.e(TAG, "扫描信息：${result} ")
            //扫描到的二维码 要做区分   罗趣 微信 荣邦
            if (result.contains("weixin")) {
                //微信的  请求后台获取 设备ID
                mPresenter.getDevCode(result, type = 300)
                UrlConstant.qrCode = result
            } else {
                if (result.contains("?")) {
                    val newResult = result.substring(result.lastIndexOf("=") + 1)
                    Log.e(TAG, "扫描信息：${result} 截取最后的设备id:${newResult}")
                    UrlConstant.deviceId = newResult //设备id
                    UrlConstant.qrCode = result
                    mPresenter.getIsHasDev(UrlConstant.deviceId, null)
                } else {
                    val newResult = result.substring(result.lastIndexOf("/") + 1)
                    Log.e(TAG, "扫描信息：${result} 截取最后的设备id:${newResult}")
                    UrlConstant.deviceId = newResult //设备id
                    UrlConstant.qrCode = result

                    mPresenter.getIsHasDev(UrlConstant.deviceId, null)

                }

            }
        } else {
            requestBluetooth()
        }
    }

    override fun onGetDevCodeSucceed(response: BaseResponse) {
        Log.e(TAG, "微信二维码获取设备id成功：$response")

        UrlConstant.deviceId = response.result
        mPresenter.getIsHasDev(UrlConstant.deviceId, null)
    }

    override fun onGetDevCodeError(error: String, errorCode: Int) {
        Log.e(TAG, "微信二维码获取设备id失败：$error")
        showToast(error)
    }


    override fun onGetIsHasDevSucceed(response: DevIsHasResponse) {
        val status = response.result.status
        UrlConstant.blueToothId = response.result.blueToothId
        UrlConstant.deviceId = response.result.devId
        var type = "Y"
        when (status) {
            0 -> //未升级
                mPresenter.getVersionConfig()   //能够升级 获取升级相关信息
            1 -> //已升级
            {
                type = "设备已升级"
                showToast("设备已升级")
            }


            2
            -> //不可升级
            {
                type = "设备不可升级"
                showToast("设备不可升级")
            }

        }
        Log.e(
            TAG, "当前设备的版本 ${response.result.btVersion}   " +
                    "固定版本：  ${HexUtil.hexStr2Str(response.result.initBtCode)}  " +
                    "升级状态：$type"
        )
    }

    override fun onGetIsHasDevDevError(error: String, errorCode: Int) {
        Log.e(TAG, "onGetDevError  $error")
        //手动升级的  都不可以升级
        if (errorCode == 501) {
            showToast("当前设备不可手动升级,请选择自动升级")
        } else {
            showToast(error)
        }

    }

    override fun onGetUpdateConfigSucceed(response: BleUpdateConfigResponse) {
        //拿到配置文件

        val fileUrl = response.result.installPackage
        Log.e(TAG, "即将升级的版本  ${response.result.version}")

        val intent = Intent(activity, OtaActivity2::class.java)
        intent.putExtra("name", UrlConstant.deviceId)
        intent.putExtra("mac", AppUtils.getStr(UrlConstant.blueToothId))
        intent.putExtra("isUnbind", true)
        intent.putExtra("fileUrl", fileUrl)
        startActivity(intent)
    }

    override fun onGetUpdateConfigError(error: String, errorCode: Int) {
        Log.e(TAG, "onGetUpdateConfigError  $error")
    }
}
