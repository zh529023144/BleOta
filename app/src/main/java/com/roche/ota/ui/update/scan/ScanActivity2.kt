package com.roche.ota.ui.update.scan

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.roche.ota.R
import com.roche.ota.api.UrlConstant
import com.roche.ota.base.BaseActivity
import com.roche.ota.model.bean.BleDevice
import com.roche.ota.model.manager.BleScanHelper
import com.roche.ota.model.response.BaseResponse
import com.roche.ota.model.response.BleUpdateConfigResponse
import com.roche.ota.model.response.DevIsHasResponse
import com.roche.ota.model.response.HotelDetailResponse
import com.roche.ota.ui.update.ota.OtaActivity2
import com.roche.ota.utils.AppUtils
import com.roche.ota.utils.HexUtil
import com.roche.ota.utils.StatusBarUtil
import com.roche.ota.utils.showToast
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_scan.*
import kotlinx.android.synthetic.main.base_title_bar.*
import java.io.File

class ScanActivity2 : BaseActivity<ScanPresenter>(), IScanView {


    private lateinit var scanBleAdapter: BaseQuickAdapter<BleDevice, BaseViewHolder>
    private val mList = mutableListOf<BleDevice>()
    var isScan = false

    //2位 设备版本 字节数
    val bytes2 = ByteArray(2)
    //6位 mac 地址字节数
    val bytes6 = ByteArray(6)


    //9位 设备验证 字节数
    val bytes9 = ByteArray(9)
    //12位 mac 地址字节数 16进制
    val bytes12 = ByteArray(12)

    //蓝牙广播数据
    val bytes31 = ByteArray(31)


    //前16位
    val bytesTo = ByteArray(16)

    val count: Long = 60 * 1
    var fileUrl = ""
    var newVersion = ""

    private lateinit var mBleScanHelper: BleScanHelper

    //扫描时间
    private var mScanTime = 60000 * 60 * 24

    //升级版本 列表
    private lateinit var devList: List<String>


    override val mPresenter: ScanPresenter by lazy {
        ScanPresenter().apply {
            attachView(this@ScanActivity2)
        }
    }

    override fun layoutId(): Int = R.layout.activity_scan


    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(mBleReceiver)
        mBleScanHelper.stopScanBle()
        mBleScanHelper.onDestroy()
    }

    override fun initView() {
        base_tv_title.text = "自动升级"
        base_tv_title.setTextColor(Color.BLACK)
        rl_base.setBackgroundResource(R.color.white)
        StatusBarUtil.setWindowStatusBarColor(this, R.color.backgroundColor)
        base_iv_back.setImageResource(R.drawable.icon_back_black)

        val filter = IntentFilter()
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
        registerReceiver(mBleReceiver, filter)
        requestBluetooth()

        scanBleAdapter =
            object : BaseQuickAdapter<BleDevice, BaseViewHolder>(R.layout.item_ble_item, null) {
                override fun convert(helper: BaseViewHolder?, item: BleDevice?) {
                    helper?.setText(R.id.tv_ble_name, "蓝牙名称：" + item?.scanRecord?.deviceName)
                    helper?.setText(R.id.tv_ble_mac, "蓝牙地址：" + item?.device?.address)

                    //厂商数据
                    val byte = item?.scanRecordBytes
                    val hexData = HexUtil.encodeHexStr(byte)
                    val gbData = hexData.substring(44, 62)

                    helper?.setText(
                        R.id.tv_ble_record, "广播信息：$gbData"
                    )
                }
            }

        scanBleAdapter.setOnItemClickListener { adapter, view, position ->

        }

        recycle_dev.layoutManager = LinearLayoutManager(this@ScanActivity2)
        recycle_dev.adapter = scanBleAdapter

        base_iv_back.visibility = View.VISIBLE
        base_iv_back.setOnClickListener {
            mBleScanHelper.stopScanBle()
            finish()
        }
    }

    override fun initData() {
//        mPresenter.getVersionConfig(UrlConstant.model)//获取升级版本号


        //初始化蓝牙
        initBluetooth()

        bn_scan.setOnClickListener {
            if (isScan) {
                isScan = false
                bn_scan.text = "开始扫描"
                mBleScanHelper.stopScanBle()
//                observable?.dispose()

            } else {
                mList.clear()
                isScan = true
                bn_scan.text = "停止扫描"
                mBleScanHelper.startScanBle()
                showLoading()

            }

        }
    }

    private fun initBluetooth() {
        //创建扫描辅助类
        mBleScanHelper = BleScanHelper.getInstance(this)
        mBleScanHelper.init()

        //扫描数据回调
        mBleScanHelper.setOnScanListener(object : BleScanHelper.onScanListener {
            override fun onNext(device: BleDevice) {

                if (device.rssi > -80) {
                    mList.add(device)
                    scanBleAdapter.setNewData(mList.distinctBy {
                        it.device.address
                    })
                    autoConnect(device)

                }
            }

//            override fun onFinish() {
//                dismissLoading()
//                mSwipeRefreshLayout.isRefreshing = false
//            }
        })
    }


    //匹配规则后 自动连接蓝牙
    private fun autoConnect(scanResult: BleDevice) {
        //蓝牙广播
        val byteArray = scanResult.scanRecord?.bytes
        val bleDevice = scanResult.device

        Log.e(TAG, "广播地址：${HexUtil.encodeHexStr(byteArray)}")
        //广播数据
        System.arraycopy(byteArray, 0, bytes31, 0, 31)//buty源数组,截取起始位置,截取后存放的数组,截取后存放的数组起始位置,截取数组长度
        //12位的mac 地址字节数
        System.arraycopy(bytes31, 9, bytes12, 0, 12)
        //
        System.arraycopy(bytes31, 22, bytes9, 0, 9)

        if (bytes9[0].toInt() == -1) {
            // -1,0, 0, -126, 0, 0, 0, 1, 52,
            System.arraycopy(bytes9, 1, bytes2, 0, 2) //设备版本号
            System.arraycopy(bytes9, 3, bytes6, 0, 6)//mac 地址
//            if (HexUtil.encodeHexStr(bytes2) == newVersion) {
//                Log.e(TAG, "----当前设备已经是最新版本-----")
//                return
//            }
            Log.e(
                TAG, "蓝牙名称：${scanResult.scanRecord?.deviceName}   " +
                        "蓝牙mac地址：${bleDevice.address}   " +
                        "设备版本号 ${HexUtil.encodeHexStr(bytes2)}  " +
                        "广播里面的mac地址：${HexUtil.encodeHexStr(bytes6, false)}"
            )


            if (scanResult.scanRecord?.deviceName == HexUtil.encodeHexStr(bytes6, false)) {
//                val fileVersion =
//                    String.format("%02X", bytesTo[5]) + String.format("%02X", bytesTo[4])

                //判断版本号 1，是否在升级的列表当中 2，是否跟文件版本号相同
//                devList.forEach {
//                    if (HexUtil.encodeHexStr(bytes2) == it) {
//                        if (fileVersion != HexUtil.encodeHexStr(bytes2)) {
//                            Log.e(TAG, "--------我找到对象啦---------fileVersion $fileVersion")
//
//
                UrlConstant.blueToothId = HexUtil.encodeHexStr(bytes6, false)
//
                mPresenter.getIsHasDev(null, UrlConstant.blueToothId)
//                        } else {
//                            Log.e(TAG, "--------升级文件版本跟设备版本一致---------")
//                        }
//                    }
//                }
            }

        }

    }

    override fun showLoading() {
        showWaitingDialog("搜索中")
    }

    override fun dismissLoading() {
        hideWaitingDialog()
    }


    //广播接受  usb 状态
    private val mBleReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                BluetoothAdapter.ACTION_STATE_CHANGED
                -> {
                    Log.e(TAG, "蓝牙状态  发生改变-------------------")
                    val r = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1)
                    if (r == BluetoothAdapter.STATE_OFF) {
                        Log.e(TAG, "蓝牙关闭")
                        requestBluetooth()
                    } else if (r == BluetoothAdapter.STATE_ON) {
                        Log.e(TAG, "蓝牙打开")
                    }
                }
            }
        }
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100) {
            requestBluetooth()
        } else if (requestCode == 800) {
            if (resultCode == Activity.RESULT_OK) {
                mList.clear()
                isScan = true
                bn_scan.text = "停止扫描"
                mBleScanHelper.startScanBle()
                showLoading()
            }
        }
    }

    override fun onGetUpdateConfigSucceed(response: BleUpdateConfigResponse) {
        //获取需要升级版本号  版本号列表 及升级文件
        fileUrl = response.result.installPackage
//        newVersion = response.result.version
//        devList = response.result.versionRange
        Log.e(TAG, "即将升级的版本  ${response.result.version}  型号：${response.result.model}")

        val intent = Intent(this, OtaActivity2::class.java)
        intent.putExtra("name", UrlConstant.blueToothId)
        intent.putExtra("mac", AppUtils.getStr(UrlConstant.blueToothId))
        intent.putExtra("fileUrl", fileUrl)
        intent.putExtra("isUnbind", true)
        startActivityForResult(intent, 800)

//        val dis = Observable.create<String> {
//            AppUtils.getUrlDownByte(fileUrl)
//            it.onNext("y")
//        }
//            .subscribeOn(Schedulers.io())
//            .observeOn(AndroidSchedulers.mainThread())
//            .subscribe({
//
//                AppUtils.getFileByte(File(UrlConstant.LOCAL_FILE_UPDATE), bytesTo)
//
//                Log.e(TAG, "bytesTo：${HexUtil.encodeHexStr(bytesTo)}")
//            }, {
//
//            })


    }

    override fun onGetUpdateConfigError(error: String, errorCode: Int) {
        Log.e(TAG, "onGetUpdateConfigError：$error")
        mBleScanHelper.stopScanBle()
        isScan = false
        bn_scan.text = "开始扫描"
        dismissLoading()
        showToast(error)
    }

    override fun onGetIsHasDevSucceed(response: DevIsHasResponse) {
        //可操作
        val status = response.result.status
        UrlConstant.deviceId = response.result.devId
        UrlConstant.model = response.result.model
        UrlConstant.hasPwd = response.result.hasPwd
        UrlConstant.cellNum = response.result.cellNum
        UrlConstant.charge = response.result.charge
        when (status) {
            0 -> //未升级
            {
                mBleScanHelper.stopScanBle()
                isScan = false
                bn_scan.text = "开始扫描"
                dismissLoading()

                mPresenter.getVersionConfig(UrlConstant.model)//获取升级版本号

//                val intent = Intent(this, OtaActivity2::class.java)
//                intent.putExtra("name", UrlConstant.blueToothId)
//                intent.putExtra("mac", AppUtils.getStr(UrlConstant.blueToothId))
//                intent.putExtra("fileUrl", fileUrl)
//                intent.putExtra("isUnbind", true)
//                startActivityForResult(intent, 800)
            }

            1 -> //已升级
                Log.e(TAG, "设备已升级")
            2 -> //不可升级
                Log.e(TAG, "设备不可升级")
        }


    }

    override fun onGetIsHasDevDevError(error: String, errorCode: Int) {
        Log.e(TAG, "error：$error")
//区分一种特殊情况  自动升级的可以  未入库 有权限的 直接升级
//        if (errorCode == 501) {
//            mBleScanHelper.stopScanBle()
//            isScan = false
//            bn_scan.text = "开始扫描"
//            dismissLoading()

//            val intent = Intent(this, OtaActivity2::class.java)
//            intent.putExtra("name", UrlConstant.blueToothId)
//            intent.putExtra("mac", AppUtils.getStr(UrlConstant.blueToothId))
//            intent.putExtra("fileUrl", fileUrl)
//            intent.putExtra("isUnbind", false)
//            startActivityForResult(intent, 800)
//        }

    }
}
