package com.roche.ota.ui.update.ota

import android.annotation.SuppressLint
import android.app.Activity
import android.util.Log
import android.view.KeyEvent
import android.view.View
import com.polidea.rxandroidble2.scan.ScanResult
import com.roche.ota.R
import com.roche.ota.api.UrlConstant
import com.roche.ota.base.BaseActivity
import com.roche.ota.model.event.BleEvent
import com.roche.ota.model.manager.BleManager
import com.roche.ota.model.request.UpdateRequest
import com.roche.ota.model.response.BleCodeResponse
import com.roche.ota.model.response.BaseResponse
import com.roche.ota.model.response.OldKeyResponse
import com.roche.ota.utils.*
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_ota.*
import kotlinx.android.synthetic.main.base_title_bar.*
import java.util.concurrent.TimeUnit


class OtaActivity : BaseActivity<OtaPresenter>(), IOtaView, BleManager.BleManagerListener {
    override fun onUpdateVersionSucceed(response: BaseResponse) {
    }

    override fun onUpdateVersionError(error: String, errorCode: Int) {
    }


    private var isDevState = 0  // 0  未入库  1 已入库  2 已绑定

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


    var writeThread: Thread? = null


    override val mPresenter: OtaPresenter by lazy {
        OtaPresenter().apply {
            attachView(this@OtaActivity)
        }
    }

    override fun layoutId(): Int = R.layout.activity_ota

    override fun onResume() {
        super.onResume()
        BleManager.instance.addBleManagerListener(this)
    }

    override fun onPause() {
        super.onPause()
        BleManager.instance.removeBleManagerListener(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        observable?.dispose()
        writeThread?.interrupt()
    }

    override fun initView() {
        val name = intent.extras?.getString("name")
        val mac = intent.extras?.getString("mac")

        base_tv_title.text = name
        tv_mac.text = mac

        UrlConstant.blueToothId = tv_mac.text.toString()
        UrlConstant.bleMac = base_tv_title.text.toString()

    }

    override fun initData() {
        //自动连接
        BleManager.instance.connectBle()

        base_iv_back.visibility = View.VISIBLE
        base_iv_back.setOnClickListener {
            BleManager.instance.stopBle()
//            setResult(800)
            finish()
        }
        bn_ota.setOnClickListener {
            //先给c1 发前16byte  等C2 的成功
            //升级文件版本跟设备版本一致 不作升级

            if (tv_device_ver.text.toString() == tv_file_ver.text.toString()) {

                showToast("当前升级版本跟设备版本一致")
                return@setOnClickListener
            }


            BleManager.instance.write(bytesTo, UrlConstant.uuid_ota1)
        }


        //读取升级文件
        // N600.H9J_app
        // N600.H9I_app

        AppUtils.getAssetsByte(this, bytesFile)


        //复制出来前16个字节
        System.arraycopy(bytesFile, 0, bytesTo, 0, 16)//buty源数组,截取起始位置,截取后存放的数组,截取后存放的数组起始位置,截取数组长度

        len = Conversion.buildUint16(bytesFile[7], bytesFile[6])
        nBlocks = len / 4


        Log.e(TAG, "当前  current:$current   nBlocks:$nBlocks")

    }

    override fun showLoading() {
        showWaitingDialog(getString(R.string.loading_text))
    }

    override fun dismissLoading() {
        hideWaitingDialog()
    }

    override fun scan(scanResult: ScanResult) {
    }

    override fun connecting() {
        showLoading()
    }

    override fun connectOnSuccess() {
        dismissLoading()
        base_iv_flag.setImageResource(R.mipmap.ble_open)

    }

    override fun connectOnError() {
        base_iv_flag.setImageResource(R.mipmap.ble_close)
        dismissLoading()
        disConnect()
        UrlConstant.isNoFirst = false
        isFirstC2 = true
        isFirstC1 = true
        index = 0
        current = 0
        iTimeElapsed = 0

        observable?.dispose()
    }

    private fun disConnect() {
        if (UrlConstant.rxConnectNum == 5) {
            Log.e(TAG, "蓝牙已经重连5次了")
            showToast("蓝牙已经重连5次了")
            BleManager.instance.stopBle()
            setResult(Activity.RESULT_OK)
            finish()
            return
        }
        UrlConstant.rxConnectNum++
        Log.e(TAG, "重连了:${UrlConstant.rxConnectNum}")
        BleManager.instance.stopBle()
        BleManager.instance.bleReConnect()


        tv_info.text = ""
        progress_bar.progress = 0
    }

    //及时写入的回调
    override fun writeOnSuccess(id: String) {
        if (id == UrlConstant.uuid_ota2.toString()) {
            Log.e(
                TAG,
                "-----uuid_ota2  onNoResponseWriteType 回调成功----${Thread.currentThread().name}-------------------------"
            )

            index += 16
            //当前页数 ++
            current++
            iTimeElapsed += 1000

            otaWrite()
        }

    }


    @SuppressLint("SetTextI18n")
    override fun receiveMessage(dataBean: BleEvent) {
        val type = dataBean.type
        UrlConstant.rxConnectNum = 0
        when (type) {
            "fee1" -> {
                //给到后台接口处理
                Log.e(TAG, "给后台处理的指令：${dataBean.hex}")
                UrlConstant.bTCode = dataBean.hex
                mPresenter.getBleKey(UrlConstant.deviceId, dataBean.hex)

            }
            UrlConstant.uuid_ota1.toString() -> {
                Log.e(TAG, "uuid_ota1通知指令：${dataBean.hex}")
                val result = HexUtil.decodeHex(dataBean.hex)


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
                    BleManager.instance.stopBle()
                    setResult(Activity.RESULT_OK)
                    finish()
                } else {
                    //自动开始升级  这条给c1 发  c2 会受到通知  如果没有 不升级
                    BleManager.instance.write(bytesTo, UrlConstant.uuid_ota1)
                    isFirstC1 = false


                    //定时轮询 没有设备就取消扫描
                    observable = Observable.intervalRange(0, count, 1, 1, TimeUnit.SECONDS)
                        .map {
                            return@map it + 1
                        }

                        .subscribe({
                            Log.e(TAG, "count: $it")
                            tv_time.text = "当前耗时：$it s"
//                            if (it == count) {
//
//                                BleManager.instance.stopScan()
//                                Log.e(TAG, "一分钟没扫到设备")
//                            }
                        }, {

                        })
                }


            }
            UrlConstant.uuid_ota2.toString() -> {
                Log.e(TAG, "uuid_ota2通知指令：${dataBean.hex}  ")
                val result = HexUtil.decodeHex(dataBean.hex)


                if (result[0].toInt() == 0 && result[1].toInt() == 0) {

                    //只处理第一次的 c2通知  因为第一次的通知 是c1 发的
                    if (!isFirstC2) return

                    //开始写入升级文件  第一次写入 c2 写入成功 会没有反应  需要在写入成功那里 继续写入 这里只写第一次
                    isFirstC2 = false
                    otaWrite()


                } else {
                    val blockReq = Conversion.buildUint16(result[1], result[0])
                    current = blockReq
                    index = blockReq * 16

                    Log.e(TAG, "收到失败的数据了-------------------")

                }
            }
        }
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

                tv_version.text = UrlConstant.version

                UrlConstant.voltage = response.result.voltage

                val power = UrlConstant.voltage
                val dy = String.format("%.1f", (power * 0.1)) + "V"
                Log.e(TAG, dy)
                tv_power.text = dy

                //是否入库  已经入库的
//                mPresenter.isDevPut(UrlConstant.bleMac)


                if (UrlConstant.isNoFirst) {
                    Log.e(TAG, "----------第一次的电量已经获取 后面不同步------------")
                    return
                }

                UrlConstant.isNoFirst = true


            }
            "RESET_PAY_CODE" -> {//同步秘钥？重置秘钥
            }
            "RESET_KEYS" -> {//重置密码
                when (keyStatus) {
                    "1" -> {
                        showToast("设备同步成功")
                    }
                    else -> {
                        showToast("设备同步异常")
                    }
                }
            }
            "W" -> {//密码
                when (keyStatus) {

                    "1" -> {
                        showToast("蓝牙解锁成功")
                        onDevOta()
                    }
                    else -> {
                        showToast("蓝牙解锁指令异常")
                        BleManager.instance.stopBle()
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
        BleManager.instance.stopBle()
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
            BleManager.instance.stopBle()
            finish()
        }
        return super.onKeyDown(keyCode, event)
    }


    //蓝牙解锁

    override fun onBleUnlockSucceed(response: OldKeyResponse) {
        val devKey = HexUtil.decodeHex(response.result.oldKey) // 密钥
        Log.e(TAG, "密钥：$devKey")

//        //给蓝牙发密码指令
        Log.e(TAG, "蓝牙开始同步")
        BleManager.instance.write(devKey)
    }

    override fun onBleUnlockError(error: String, errorCode: Int) {
        Log.e(TAG, "蓝牙解锁接口异常：$error")
        showToast(error)
        BleManager.instance.stopBle()
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
        BleManager.instance.notify(UrlConstant.uuid_ota1)
        BleManager.instance.notify(UrlConstant.uuid_ota2)


        //获取设备  给fc1 发 device version  Rom Version
        BleManager.instance.write(byteArrayOf(0), UrlConstant.uuid_ota1)

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
            Log.e(TAG, "---------开始写数据-------${Thread.currentThread().name}--")
            BleManager.instance.write(bytes18, UrlConstant.uuid_ota2)
        } else {
            Log.e(
                TAG,
                "发送完成了 current:$current   nBlocks:$nBlocks     ${Thread.currentThread().name}"
            )

            BleManager.instance.stopBle()
            showToast("--------升级完成--------")
            val request = UpdateRequest(
                UrlConstant.bleMac,
                UrlConstant.upDateBleVersion,
                UrlConstant.bleVersion,
                1, "升级成功"
            )

            mPresenter.upDateLog(request)

        }
        displayStats()
        progress_bar.progress = (current * 100 / nBlocks).toShort().toInt()


    }


    //记录升级日志
    override fun onUpdateLogSucceed(response: BaseResponse) {
        Log.e(TAG, "记录升级日志成功")
        setResult(Activity.RESULT_OK)
        finish()
    }

    override fun onUpdateLogError(error: String, errorCode: Int) {
        Log.e(TAG, "记录升级日志异常：$error")
        showToast(error)
    }
}
