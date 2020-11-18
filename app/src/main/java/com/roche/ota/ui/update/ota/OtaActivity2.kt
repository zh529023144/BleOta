package com.roche.ota.ui.update.ota

import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.content.Intent
import android.graphics.Color
import android.util.Log
import android.view.KeyEvent
import android.view.View
import com.roche.ota.R
import com.roche.ota.api.UrlConstant
import com.roche.ota.base.BaseActivity
import com.roche.ota.model.manager.BleConnectionHelper
import com.roche.ota.model.request.UpdateRequest
import com.roche.ota.model.response.BleCodeResponse
import com.roche.ota.model.response.BaseResponse
import com.roche.ota.model.response.OldKeyResponse
import com.roche.ota.utils.*
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_ota.*
import kotlinx.android.synthetic.main.base_title_bar.*
import java.io.File
import java.lang.Exception
import java.util.concurrent.TimeUnit


class OtaActivity2 : BaseActivity<OtaPresenter>(), IOtaView {


    //前16位
    val bytesTo = ByteArray(16)

    //18位 升级码
    val bytes18 = ByteArray(18)

    //升级文件
    val bytesFile = ByteArray(0x40000)

    //起始位
    var index: Long = 0

    //当前位置
    var current: Long = 0

    //发送的份数
    var nBlocks: Long = 0

    //升级文件长度
    var len: Long = 0

    //已用毫秒数
    var iTimeElapsed = 0


    //是否第一次
    var isFirstC2 = true
    var isFirstC1 = true

    val count: Long = 60 * 1

    var observable: Disposable? = null

    var isUpdate = false

    var isUnbind: Boolean? = null

    //连接辅助类
    private lateinit var bleConnectionHelper: BleConnectionHelper

    //写特征值
    lateinit var characteristic: BluetoothGattCharacteristic

    lateinit var characteristic01: BluetoothGattCharacteristic
    lateinit var characteristic02: BluetoothGattCharacteristic


    override val mPresenter: OtaPresenter by lazy {
        OtaPresenter().apply {
            attachView(this@OtaActivity2)
        }
    }

    override fun layoutId(): Int = R.layout.activity_ota


    override fun onDestroy() {
        super.onDestroy()
        observable?.dispose()
    }

    override fun initView() {
        base_tv_title.text = "更新设备"
        base_tv_title.setTextColor(Color.BLACK)
        rl_base.setBackgroundResource(R.color.white)
        StatusBarUtil.setWindowStatusBarColor(this, R.color.backgroundColor)
        base_iv_back.setImageResource(R.drawable.icon_back_black)

        val name = intent.extras?.getString("name")
        val mac = intent.extras?.getString("mac")
        isUnbind = intent.extras?.getBoolean("isUnbind")
        val fileUrl = intent.extras?.getString("fileUrl")

//
        val dis = Observable.create<String> {
            AppUtils.getUrlDownByte(fileUrl!!)
            it.onNext("y")
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({

                AppUtils.getFileByte(File(UrlConstant.LOCAL_FILE_UPDATE), bytesFile)
                //复制出来前16个字节
                System.arraycopy(
                    bytesFile,
                    0,
                    bytesTo,
                    0,
                    16
                )//buty源数组,截取起始位置,截取后存放的数组,截取后存放的数组起始位置,截取数组长度

                len = Conversion.buildUint16(bytesFile[7], bytesFile[6])
                nBlocks = len / 4


                Log.e(
                    TAG,
                    "当前  current:$current   nBlocks:$nBlocks       ${Thread.currentThread().name}"
                )
                base_tv_title.text = name
                tv_mac.text = mac

                UrlConstant.bleMac = UrlConstant.blueToothId
                UrlConstant.blueToothId = tv_mac.text.toString()
                initBluetooth()
            }, {

            })
    }

    /**
     * 初始化蓝牙
     */
    private fun initBluetooth() {
        bleConnectionHelper = BleConnectionHelper.getInstance(this)
        bleConnectionHelper.init()
        bleConnectionHelper.setBleConnectionListener(daqiBleConnectionListener())


        try {
            showLoading()
            bleConnectionHelper.connection(UrlConstant.blueToothId)
        } catch (a: Exception) {
            showToast(a.localizedMessage)
            bleConnectionHelper.onDestroy()
            setResult(Activity.RESULT_OK)
            finish()
        }

    }

    override fun initData() {

        base_iv_back.visibility = View.VISIBLE
        base_iv_back.setOnClickListener {
            bleConnectionHelper.onDestroy()
            finish()
        }
        bn_ota.setOnClickListener {
            //先给c1 发前16byte  等C2 的成功
            //升级文件版本跟设备版本一致 不作升级

            if (tv_device_ver.text.toString() == tv_file_ver.text.toString()) {

                showToast("当前升级版本跟设备版本一致")
                return@setOnClickListener
            }
        }


    }

    override fun showLoading() {
        showWaitingDialog(getString(R.string.loading_text))
    }

    override fun dismissLoading() {
        hideWaitingDialog()
    }


    private fun displayStats() {
        var txt: String
        val byteRate: Int
        val sec = iTimeElapsed / 1000
        if (sec > 0) {
            byteRate = (index / sec).toInt()
        } else {
            return
        }
        val timeEstimate: Float
        timeEstimate = (len * 4).toFloat() / index.toFloat() * sec

        txt = String.format("当前进度: %d / %d 总进度", sec, timeEstimate.toInt())
        txt += String.format("    Bytes: %d (%d/进度)", index, byteRate)
        tv_info.text = txt
    }

    //蓝牙指令处理
    override fun onBleKeySucceed(response: BleCodeResponse) {
        val keyStatus = response.result.keyStatus
        when (response.result.instruction) {
            "SCAN" -> {//获取电量

                UrlConstant.version = response.result.origin
                UrlConstant.func = response.result.func
                UrlConstant.patch = response.result.patch
                Log.e(
                    TAG,
                    "电量通知:" + UrlConstant.version + "\n" + UrlConstant.func + "\n" + UrlConstant.patch
                )

//                if (isUpdate) {
//
//                    isUpdate = false
//                    Log.e(TAG, "----------已升级 最新版本号---${UrlConstant.version}---------")
//
//                    mPresenter.upDateVersion(
//                        UrlConstant.deviceId,
//                        UrlConstant.bleMac,
//                        HexUtil.str2HexStr(UrlConstant.version)
//                    )
//                    return
//                }


                tv_version.text = UrlConstant.version

                UrlConstant.voltage = response.result.voltage

                val power = UrlConstant.voltage
                val dy = String.format("%.1f", (power * 0.1)) + "V"
                Log.e(TAG, dy)
                tv_power.text = dy

                if (isUnbind!!) {
                    Log.e(TAG, "设备存在 需要解锁")
                    mPresenter.getSynKey(
                        UrlConstant.bleMac,
                        UrlConstant.deviceId,
                        UrlConstant.bTCode
                    )
                } else {
                    Log.e(TAG, "设备不存在 不用解锁 直接升级")
                    onDevOta()
                }



                if (UrlConstant.isNoFirst) {
                    Log.e(TAG, "----------第一次的电量已经获取 后面不同步------------")
                    return
                }

                UrlConstant.isNoFirst = true


            }
            "RESET_PAY_CODE" -> {//同步秘钥？重置秘钥
                when (keyStatus) {

                    "1" -> {

                        showToast("设备同步秘钥成功")



                        if (isUpdate) {
                            Log.e(TAG, "升级完成后 蓝牙同步密钥成功")
                            isUpdate = false
                            Log.e(TAG, "----------已升级 最新版本号---${UrlConstant.version}---------")


                            //支持密码   就需要调一下同步密码的接口
                            if (UrlConstant.hasPwd == 1) {
                                mPresenter.getFeaturesCode(UrlConstant.bleMac, UrlConstant.deviceId)
                            } else {
                                mPresenter.upDateVersion(
                                    UrlConstant.deviceId,
                                    UrlConstant.bleMac,
                                    HexUtil.str2HexStr(UrlConstant.version)
                                )
                            }
                            return
                        }

                        Log.e(TAG, "第一次  蓝牙同步密钥成功")
                        onDevOta()
                    }
                    else -> {
                        showToast("蓝牙同步密钥异常")
                        bleConnectionHelper.onDestroy()
                        val request = UpdateRequest(
                            UrlConstant.bleMac,
                            UrlConstant.upDateBleVersion,
                            UrlConstant.bleVersion,
                            0, "蓝牙解锁指令异常"
                        )

                        mPresenter.upDateLog(request)
                    }

                }


            }
            "RESET_KEYS" -> {//重置密码
                when (keyStatus) {
                    "1" -> {
                        showToast("设备同步成功")
                        mPresenter.upDateVersion(
                            UrlConstant.deviceId,
                            UrlConstant.bleMac,
                            HexUtil.str2HexStr(UrlConstant.version)
                        )
                    }
                    else -> {
                        showToast("设备同步异常")
                        bleConnectionHelper.onDestroy()
                        val request = UpdateRequest(
                            UrlConstant.bleMac,
                            UrlConstant.upDateBleVersion,
                            UrlConstant.bleVersion,
                            0, "设备密码同步异常"
                        )

                        mPresenter.upDateLog(request)
                    }
                }
            }
            "W" -> {//密码
                when (keyStatus) {

                    "1" -> {
                        showToast("蓝牙解锁成功")
//                        onDevOta()
                    }
                    else -> {
                        showToast("蓝牙解锁指令异常")
                        bleConnectionHelper.onDestroy()
                        val request = UpdateRequest(
                            UrlConstant.bleMac,
                            UrlConstant.upDateBleVersion,
                            UrlConstant.bleVersion,
                            0, "蓝牙解锁指令异常"
                        )

                        mPresenter.upDateLog(request)
                    }
                }
            }
        }


    }

    override fun onBleKeyError(error: String, errorCode: Int) {
        Log.e(TAG, "蓝牙指令管理异常：$error")
        showToast(error)
        bleConnectionHelper.onDestroy()
        val request = UpdateRequest(
            UrlConstant.bleMac,
            UrlConstant.upDateBleVersion,
            UrlConstant.bleVersion,
            0, "蓝牙指令管理异常：$error"
        )

        mPresenter.upDateLog(request)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            bleConnectionHelper.onDestroy()
            finish()
        }
        return super.onKeyDown(keyCode, event)
    }

    //蓝牙解锁

//    override fun onBleUnlockSucceed(response: OldKeyResponse) {
//        val devKey = HexUtil.decodeHex(response.result.oldKey) // 密钥
//        Log.e(TAG, "密钥：$devKey")
//
////        //给蓝牙发密码指令
//        Log.e(TAG, "蓝牙开始同步")
//
//        bleConnectionHelper.writeCharacteristic(
//            characteristic,
//            devKey
//        )
//    }
//
//    override fun onBleUnlockError(error: String, errorCode: Int) {
//        Log.e(TAG, "蓝牙解锁接口异常：$error")
//        showToast(error)
//        bleConnectionHelper.onDestroy()
//        val request = UpdateRequest(
//            UrlConstant.bleMac,
//            UrlConstant.upDateBleVersion,
//            UrlConstant.bleVersion,
//            0, "蓝牙解锁接口异常$error"
//        )
//
//        mPresenter.upDateLog(request)
//    }

    override fun onGetSynKeySucceed(response: BaseResponse) {
        val devKey = HexUtil.decodeHex(response.result) // 密钥
        Log.e(TAG, "密钥：$devKey")

//        //给蓝牙发密码指令
        Log.e(TAG, "蓝牙开始同步")

        bleConnectionHelper.writeCharacteristic(
            characteristic,
            devKey
        )

    }

    override fun onGetSynKeyError(error: String, errorCode: Int) {
        Log.e(TAG, "蓝牙解锁接口异常：$error")
        showToast(error)
        bleConnectionHelper.onDestroy()
        val request = UpdateRequest(
            UrlConstant.bleMac,
            UrlConstant.upDateBleVersion,
            UrlConstant.bleVersion,
            0, "蓝牙解锁接口异常$error"
        )

        mPresenter.upDateLog(request)
    }

    //OTA 升级
    private fun onDevOta() {
        Log.e(TAG, "OTA 获取设备 及升级问文件 版本号")

        //监听 c1 c2
        bleConnectionHelper.setCharacteristicNotification(characteristic01)
        bleConnectionHelper.setCharacteristicNotification(characteristic02)
        Log.e(TAG, "设置升级服务通知——————————————————————")
//
//        //获取设备  给fc1 发 device version  Rom Version

        bleConnectionHelper.writeCharacteristic(characteristic01, byteArrayOf(0))
    }

    //OTA 写入升级文件
    private fun otaWrite() {
        //                    //分包发  设备需要18个字节的组装数据
        bytes18[0] = Conversion.loUint16(current)
        bytes18[1] = Conversion.hiUint16(current)


        //复制 升级文件当中的16个字节
        System.arraycopy(bytesFile, index.toInt(), bytes18, 2, 16)


        //当前页数小于 等于总页数就一直发  反之就是发完了   需要关闭蓝牙 将升级信息保存  返回首页
        if (current < nBlocks) {
//            Log.e(
//                TAG,
//                "-----------------发送升级文件（18byte）之前-----------${Thread.currentThread().name}------"
//            )
            bleConnectionHelper.writeCharacteristicForC2(characteristic02, bytes18)
//            if (success) {
//                val data = HexUtil.encodeHexStr(bytes18)
//                Log.e(TAG, "写入成功:$data")
//                Log.e(
//                    TAG,
//                    "-----------------发送升级文件（18byte）完成--------${Thread.currentThread().name}---------"
//                )
//            }
        } else {
            Log.e(TAG, "发送完成了 current:$current   nBlocks:$nBlocks")
            observable?.dispose()
            bleConnectionHelper.closeConnection()

            //回到主线程  等待设备重启
            Thread.sleep(3000)
            runOnUiThread {
                showToast("升级完成")

                val request = UpdateRequest(
                    UrlConstant.bleMac,
                    UrlConstant.upDateBleVersion,
                    UrlConstant.bleVersion,
                    1, "升级成功"
                )
                mPresenter.upDateLog(request)

            }
        }

        runOnUiThread {
            displayStats()
            progress_bar.progress = (current * 100 / nBlocks).toShort().toInt()
        }


    }


    //记录升级日志
    override fun onUpdateLogSucceed(response: BaseResponse) {
        Log.e(TAG, "记录升级日志成功")
        isUpdate = true

        if (isUnbind!!) {
            //等待设备重启  在重新连接设备 获取最新版本号
            bleConnectionHelper.connection(UrlConstant.blueToothId)
        } else {
            bleConnectionHelper.onDestroy()
            setResult(Activity.RESULT_OK)
            finish()
        }


    }

    override fun onUpdateLogError(error: String, errorCode: Int) {
        Log.e(TAG, "记录升级日志异常：$error")
        showToast(error)
    }

    override fun onUpdateVersionSucceed(response: BaseResponse) {
        Log.e(TAG, "更新版本成功")
        bleConnectionHelper.onDestroy()
        setResult(Activity.RESULT_OK)
        finish()
    }

    override fun onUpdateVersionError(error: String, errorCode: Int) {
        Log.e(TAG, "更新版本失败$error")
        showToast(error)
    }


    override fun onGetFeaturesCodeSucceed(response: BaseResponse) {
        val devKey = HexUtil.decodeHex(response.result) // 密钥
        Log.e(TAG, "密钥：$devKey")

//        //给蓝牙发密码指令
        Log.e(TAG, "蓝牙密码设备开始同步")

        bleConnectionHelper.writeCharacteristic(
            characteristic,
            devKey
        )
    }

    override fun onGetFeaturesCodeError(error: String, errorCode: Int) {

    }

    /**
     * 连接回调监听
     */
    private inner class daqiBleConnectionListener : BleConnectionHelper.BleConnectionListener {


        override fun onConnectionSuccess() {
            Log.e(TAG, "蓝牙连接成功     ${Thread.currentThread().name}")
            dismissLoading()
            base_iv_flag.setImageResource(R.mipmap.ble_open)

        }

        //最后 重连都失败的
        override fun onConnectionFail() {
            dismissLoading()
            Log.e(TAG, "蓝牙连接失败")
            showToast("蓝牙连接失败")
            bleConnectionHelper.onDestroy()
            setResult(Activity.RESULT_OK)
            finish()
        }

        //连接成功过的断开  需要重连
        override fun disConnection() {
            Log.e(TAG, "断开连接")

            dismissLoading()
            UrlConstant.isNoFirst = false
            isFirstC2 = true
            isFirstC1 = true
            index = 0
            current = 0
            iTimeElapsed = 0

            observable?.dispose()
            tv_info.text = ""
            progress_bar.progress = 0

            bleConnectionHelper.tryReConnection()

        }

        override fun discoveredServices(data: ArrayList<BluetoothGattService>) {

            data.forEach { service ->
                if (service.uuid == UrlConstant.old_service) service.characteristics.forEach {
                    if (it.uuid == UrlConstant.WRITE_UUID) {
                        Log.e(TAG, "发现服务特征值---------------${it.uuid}")
                        characteristic = it
                        bleConnectionHelper.writeCharacteristic(
                            it,
                            UrlConstant.DEVICE_POWER.toByteArray()
                        )

                    }
                }
                else if (service.uuid == UrlConstant.uuid_update) service.characteristics.forEach {
                    if (it.uuid == UrlConstant.uuid_ota1) {
                        characteristic01 = it
                        Log.e(TAG, "发现服务特征值---------------${it.uuid}")
                    } else if (it.uuid == UrlConstant.uuid_ota2) {
                        characteristic02 = it
                        Log.e(TAG, "发现服务特征值---------------${it.uuid}")
                    }
                }
            }

        }

        override fun readCharacteristic(data: String) {
            Log.e(TAG, "readCharacteristic:$data")
        }

        //  没有设置 onNoResponseWriteType 单独回调到这里
        override fun writeCharacteristic(data: String, uuid: String) {
//            Log.e(TAG, "-----------------写入成功-----------------")
//            Log.e(TAG, "写入成功的回调:$data")
//            if (uuid == UrlConstant.uuid_ota2.toString()) {
//                Thread.sleep(2)
//                otaWrite()
//
//            }

        }

        //  设置 onNoResponseWriteType 单独回调到这里
        override fun onNoResponseWriteType(data: Boolean, uuid: String) {
            if (data) {
                if (uuid == UrlConstant.uuid_ota2.toString()) {
//                    Log.e(
//                        TAG,
//                        "-----uuid_ota2  onNoResponseWriteType 回调成功----${Thread.currentThread().name}-------------------------"
//                    )

                    index += 16
                    //当前页数 ++
                    current++
                    iTimeElapsed += 1000

                    otaWrite()
                }

            }

        }

        override fun readDescriptor(data: String) {
            Log.e(TAG, "readDescriptor:$data")
        }

        override fun writeDescriptor(data: String) {
            Log.e(TAG, "writeDescriptor:$data")
        }

        @SuppressLint("SetTextI18n")
        override fun characteristicChange(data: String, uuid: String) {
            Log.e(TAG, "通知回来了----------")
            when (uuid) {
                UrlConstant.WRITE_UUID.toString() -> {
                    //给到后台接口处理
                    UrlConstant.bTCode = data
                    Log.e(TAG, "给后台处理的指令：${data}")
                    mPresenter.getBleKey(UrlConstant.deviceId, data)

                }
                UrlConstant.uuid_ota1.toString() -> {
                    Log.e(TAG, "uuid_ota1通知指令：${data}")
                    val result = HexUtil.decodeHex(data)
                    tv_device_ver.text =
                        String.format("%02X", result[1]) + String.format("%02X", result[0])
                    tv_rom_ver.text =
                        String.format("%02X", result[9]) + String.format("%02X", result[8])
                    tv_file_ver.text =
                        String.format("%02X", bytesTo[5]) + String.format("%02X", bytesTo[4])
                    tv_file_row_ver.text =
                        String.format("%02X", bytesTo[15]) + String.format("%02X", bytesTo[14])

                    UrlConstant.bleVersion = tv_device_ver.text.toString()
                    UrlConstant.upDateBleVersion = tv_file_ver.text.toString()

                    if (!isFirstC1) {
                        showToast("该设备不可升级")
                        bleConnectionHelper.onDestroy()
                        setResult(Activity.RESULT_OK)
                        finish()
                    } else {
                        //定时
                        observable = Observable.intervalRange(1, count, 1, 1, TimeUnit.SECONDS)
                            .map {
                                return@map it + 1
                            }
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe({
                                tv_time.text = "当前耗时：$it s"
                            }, {

                            })

                        Log.e(TAG, "开始升级———————给c1发前十六位升级文件———————————————")
                        //自动开始升级  这条给c1 发  c2 会受到通知  如果没有 不升级
                        bleConnectionHelper.writeCharacteristic(characteristic01, bytesTo)
                        isFirstC1 = false
                        Log.e(TAG, "给c1发前十六位升级文件：${HexUtil.encodeHexStr(bytesTo)}")

                    }


                }
                UrlConstant.uuid_ota2.toString() -> {
                    Log.e(TAG, "uuid_ota2通知指令：${data}")
                    val result = HexUtil.decodeHex(data)


                    if (result[0].toInt() == 0 && result[1].toInt() == 0) {

                        //只处理第一次的 c2通知  因为第一次的通知 是c1 发的
                        if (!isFirstC2) return
                        Log.e(TAG, "c2 收到00通知 开始写入数据———————————————")
                        //开始写入升级文件  第一次写入 c2 写入成功 会没有反应  需要在写入成功那里 继续写入 这里只写第一次
                        otaWrite()
                        isFirstC2 = false
                    } else {
                        val blockReq = Conversion.buildUint16(result[1], result[0])
                        current = blockReq
                        index = blockReq * 16

                        Log.e(TAG, "收到失败的数据了-------------------")

                    }
                }
            }
        }
    }

}
