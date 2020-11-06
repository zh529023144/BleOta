package com.roche.ota.ui.update.scan

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.polidea.rxandroidble2.scan.ScanResult
import com.roche.ota.R
import com.roche.ota.api.UrlConstant
import com.roche.ota.base.BaseActivity
import com.roche.ota.model.event.BleEvent
import com.roche.ota.model.manager.BleManager
import com.roche.ota.model.response.BleUpdateConfigResponse
import com.roche.ota.model.response.DevIsHasResponse
import com.roche.ota.model.response.HotelDetailResponse
import com.roche.ota.ui.update.ota.OtaActivity
import com.roche.ota.utils.AppUtils
import com.roche.ota.utils.HexUtil
import com.roche.ota.utils.showToast
import com.tbruyelle.rxpermissions2.RxPermissions
import kotlinx.android.synthetic.main.activity_scan.*

class ScanActivity : BaseActivity<ScanPresenter>(), IScanView,
    BleManager.BleManagerListener {
    override fun onGetIsHasDevSucceed(response: DevIsHasResponse) {
    }

    override fun onGetIsHasDevDevError(error: String, errorCode: Int) {
    }

    override fun onGetUpdateConfigSucceed(response: BleUpdateConfigResponse) {
    }

    override fun onGetUpdateConfigError(error: String, errorCode: Int) {
    }


    private lateinit var scanBleAdapter: BaseQuickAdapter<ScanResult, BaseViewHolder>
    private val mList = mutableListOf<ScanResult>()
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


    //升级版本 列表
    val devList = listOf("0105", "0106", "0107", "0108","0109")


    override val mPresenter: ScanPresenter by lazy {
        ScanPresenter().apply {
            attachView(this@ScanActivity)
        }
    }

    override fun layoutId(): Int = R.layout.activity_scan

    override fun onResume() {
        super.onResume()
        BleManager.instance.addBleManagerListener(this)
    }

    override fun onPause() {
        super.onPause()
        BleManager.instance.removeBleManagerListener(this)
    }

    override fun onDestroy() {
        BleManager.instance.stopBle()
        super.onDestroy()
        unregisterReceiver(mBleReceiver)
    }

    override fun initView() {
        val filter = IntentFilter()
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
        registerReceiver(mBleReceiver, filter)
        requestBluetooth()

        scanBleAdapter =
            object : BaseQuickAdapter<ScanResult, BaseViewHolder>(R.layout.item_ble_item, null) {
                override fun convert(helper: BaseViewHolder?, item: ScanResult?) {
                    helper?.setText(R.id.tv_ble_name, "蓝牙名称：" + item?.bleDevice?.name)
                    helper?.setText(R.id.tv_ble_mac, "蓝牙地址：" + item?.bleDevice?.macAddress)

                    helper?.setText(
                        R.id.tv_ble_record,
                        HexUtil.encodeHexStr(item?.scanRecord?.bytes)
                    )


                }
            }

        scanBleAdapter.setOnItemClickListener { adapter, view, position ->
            //连接蓝牙 配对  跳转
            val result = adapter.getItem(position) as ScanResult
            UrlConstant.blueToothId =result.bleDevice.macAddress
            BleManager.instance.connectBle()
//
//            val intent = Intent(this, OtaActivity::class.java)
//            intent.putExtra("name", result.bleDevice.name)
//            intent.putExtra("mac", result.bleDevice.macAddress)
////            intent.putExtra("",result.scanRecord.bytes)
//
//            isScan = false
//            bn_scan.text = "开始扫描"
//            BleManager.instance.stopScan()
//            startActivityForResult(intent, 800)

        }

        recycle_dev.layoutManager = LinearLayoutManager(this@ScanActivity)
        recycle_dev.adapter = scanBleAdapter
    }

    override fun initData() {
        AppUtils.getAssetsByte(this, bytesTo)

        bn_scan.setOnClickListener {
            if (isScan) {
                isScan = false
                bn_scan.text = "开始扫描"
                BleManager.instance.stopScan()

//                observable?.dispose()

            } else {
                mList.clear()
                isScan = true
                bn_scan.text = "停止扫描"
                BleManager.instance.scanBle()
                showLoading()

            }

        }

        //权限申请
        val dis = RxPermissions(this).request(
            Manifest.permission.CAMERA,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
            .subscribe {

                if (it) {
                    showToast("权限通过")
                } else {
                    showToast("权限未通过")
                }
            }

    }

    override fun scan(scanResult: ScanResult) {
        if (scanResult.rssi>-50){
            mList.add(scanResult)
            scanBleAdapter.setNewData(mList.distinctBy {
                it.bleDevice.macAddress
            })
//            autoConnect(scanResult)

        }


    }

    //匹配规则后 自动连接蓝牙
    private fun autoConnect(scanResult: ScanResult) {
        //蓝牙广播
        val byteArray = scanResult.scanRecord.bytes
        val bleDevice = scanResult.bleDevice

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


            Log.e(
                TAG, "蓝牙名称：${scanResult.scanRecord?.deviceName}   " +
                        "蓝牙mac地址：${bleDevice.macAddress}   " +
                        "设备版本号 ${HexUtil.encodeHexStr(bytes2)}  " +
                        "广播里面的mac地址：${HexUtil.encodeHexStr(bytes6, false)}"
            )


            if (scanResult.scanRecord?.deviceName == HexUtil.encodeHexStr(bytes6, false)) {
                val fileVersion =
                    String.format("%02X", bytesTo[5]) + String.format("%02X", bytesTo[4])

                //判断版本号 1，是否在升级的列表当中 2，是否跟文件版本号相同
                devList.forEach {
                    if (HexUtil.encodeHexStr(bytes2) == it) {
                        if (fileVersion != HexUtil.encodeHexStr(bytes2)) {
                            Log.e(TAG, "--------我找到对象啦---------fileVersion $fileVersion")


                            val intent = Intent(this, OtaActivity::class.java)
                            intent.putExtra("name", scanResult.scanRecord?.deviceName)
                            intent.putExtra("mac", bleDevice.macAddress)

                            isScan = false
                            bn_scan.text = "开始扫描"
                            BleManager.instance.stopScan()
                            dismissLoading()
                            startActivityForResult(intent, 800)


                        } else {
                            Log.e(TAG, "--------升级文件版本跟设备版本一致---------")
                            BleManager.instance.stopScan()
                            Thread.sleep(50)
                            BleManager.instance.scanBle()
                        }
                    }
                }
            }

        }

    }

    override fun showLoading() {
        showWaitingDialog("搜索中")
    }

    override fun dismissLoading() {
        hideWaitingDialog()
    }

    override fun connecting() {
    }

    override fun connectOnSuccess() {
    }

    override fun connectOnError() {
    }

    override fun receiveMessage(dataBean: BleEvent) {
    }

    override fun writeOnSuccess(id: String) {
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
                BleManager.instance.scanBle()
                showLoading()
            }


            //定时轮询 没有设备就取消扫描
//            observable = Observable.intervalRange(0, count, 1, 1, TimeUnit.SECONDS)
//                .map {
//                    return@map it + 1
//                }
//
//                .subscribe({
//                    Log.e(TAG, "count: $it")
//                    if (it==count){
//                        observable?.dispose()
//                        BleManager.instance.stopScan()
//                        Log.e(TAG,"一分钟没扫到设备")
//                    }
//                }, {
//
//                })
        }
    }
}
