package com.roche.ota.service

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import com.jakewharton.rx.ReplayingShare
import com.polidea.rxandroidble2.RxBleConnection
import com.polidea.rxandroidble2.RxBleDevice
import com.roche.ota.api.UrlConstant
import com.roche.ota.base.App
import com.roche.ota.model.event.BleEvent
import com.roche.ota.utils.isConnected
import com.roche.ota.utils.showToast
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.concurrent.TimeUnit

class BleService : Service() {
    val TAG = "BleService"

    private val rxBleClient = App.rxBleClient
    private lateinit var bleDevice: RxBleDevice
    private val connectionDisposable = CompositeDisposable()
    private lateinit var connectionObservable: Observable<RxBleConnection>
     var mBleState: Boolean = false
    private var rxConnectNum = 0

    private val binder = BleBinder()

    inner class BleBinder : Binder() {
        fun getService(): BleService {
            return this@BleService
        }
    }

    override fun onCreate() {
        super.onCreate()
        initData()

    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
        stopBle()
    }


    override fun onBind(p0: Intent?): IBinder? {
        return binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_NOT_STICKY
    }

    override fun onUnbind(intent: Intent?): Boolean {
        return false
    }

    //初始化
    private fun initData() {
        EventBus.getDefault().register(this)
    }

    fun bleState(): Boolean {
        return bleDevice.isConnected
    }

    private fun prepareConnectionObservable(): Observable<RxBleConnection> =
        bleDevice
            .establishConnection(false)
            .takeUntil(PublishSubject.create<Unit>())
            .compose(ReplayingShare.instance())

    //蓝牙扫描
    fun scanBle() {}

    //蓝牙连接
    fun connectBle() {

        val bleMac = getStr(UrlConstant.blueToothId)
        bleDevice = rxBleClient.getBleDevice(bleMac)
        connectionObservable = prepareConnectionObservable()

        //监听设备状态
        bleDevice.observeConnectionStateChanges()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ state ->
                when (state) {
                    RxBleConnection.RxBleConnectionState.CONNECTING -> {
                        Log.e(TAG, "设备正在连接")
                        EventBus.getDefault().post(BleEvent(-1, "设备正在连接", "", "connect"))
                    }
                    RxBleConnection.RxBleConnectionState.CONNECTED -> {
                        Log.e(TAG, "设备已经连接")
                        mBleState = true
                    }
                    RxBleConnection.RxBleConnectionState.DISCONNECTED -> {
                        Log.e(TAG, "设备断开连接")
                        //重新连接 清除之前的状态
                        mBleState = false
                        EventBus.getDefault().post("1")
                        EventBus.getDefault().post(BleEvent(0, "设备断开连接", "", "connect"))
                    }
                }
            }, {
                Log.e(TAG, it.toString())
            })
            .let { connectionDisposable.add(it) }
        //收到通知
        connectionObservable
            .flatMap { connection ->
                connection.setupNotification(UrlConstant.READ_UUID)
                    .flatMap {
                        // 打开通知成功
                        it
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe({
                                rxConnectNum = 0
                                // 收到通知
                                var data = String(it)
                                Log.e(TAG, "收到通知:$data")
                                EventBus.getDefault().post(BleEvent(1, "设备已经连接", "", "connect"))
                                EventBus.getDefault().post(BleEvent(1, "设备通知", data, "notify"))

                            }, {
                                Log.e(TAG, "通知:${it}")
                            })
                        Observable.just(connection)
                    }
            }
            .doOnError {
                // 打开通知失败
                Log.e(TAG, "通知doOnError:$it")
            }
            .flatMapSingle { it.writeCharacteristic(UrlConstant.WRITE_UUID, getBytes(1)) }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ bytes ->
                val data = String(bytes)
                Log.e(TAG, "写入成功:$data")
            }, { throwable ->
                Log.e(TAG, "写入异常 错误信息:" + throwable + "错误信息：")
                showToast("写入异常")
            })
            .let { connectionDisposable.add(it) }


    }

    //断开蓝牙
    private fun stopBle() {
        if (!connectionDisposable.isDisposed) {
            Log.e(TAG, "已存入监听数量：${connectionDisposable.size()}")//0 是蓝牙未连接 1 是状态 2是+读写 3是+单独写
            connectionDisposable.clear()

        }
    }

    //单独写入
    fun write(i: Int) {
        if (bleDevice.isConnected) {
            connectionObservable
                .subscribeOn(Schedulers.io())
                .flatMapSingle { it.writeCharacteristic(UrlConstant.WRITE_UUID, getBytes(i)) }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ bytes ->
                    val data = String(bytes)
                    Log.e(TAG, "写入成功:$data")
                }, { throwable ->
                    Log.e(TAG, "写入异常:${throwable}")
                })
                .let { connectionDisposable.add(it) }
        }

    }

    fun write(i: ByteArray) {
        if (bleDevice.isConnected) {
            connectionObservable
                .subscribeOn(Schedulers.io())
                .flatMapSingle { it.writeCharacteristic(UrlConstant.WRITE_UUID, i) }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ bytes ->
                    val data = String(bytes)
                    Log.e(TAG, "写入成功:$data")
                }, { throwable ->
                    Log.e(TAG, "写入异常:${throwable}")
                })
                .let { connectionDisposable.add(it) }
        }

    }
    //蓝牙重连

    private fun bleReConnect() {
        showToast("蓝牙重连中")
        Log.e(TAG, "直连重连中")
        //直连 不扫描
        val dis = Observable.timer(1, TimeUnit.SECONDS)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                connectBle()//蓝牙连接
            }, {
                Log.e(TAG, "1s$it")
            })
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun Event(data: String) {
        if (rxConnectNum == 5) {
            Log.e(TAG, "蓝牙已经重连5次了")
            showToast("蓝牙已经重连5次了")
            EventBus.getDefault().post(BleEvent(1, "蓝牙已经重连5次了", data, "reconnect"))
            return
        }
        rxConnectNum++
        Log.e(TAG, "重连了:$rxConnectNum")
        if (bleState()){
            stopBle()
        }
        bleReConnect()
    }


    private fun getBytes(i: Int): ByteArray {
        when (i) {
            1 -> {
                val data = " n750V6 "
                return data.toByteArray()
            }
            2 -> {
                val data = UrlConstant.synKey
                Log.e(TAG, "同步指令：$data")
                return data.toByteArray()
            }
            3 -> {
                val data = " n750V3e23r96ok "
                Log.e(TAG, "一键测试指令：$data")
                return data.toByteArray()
            }
            4 -> {
                val data = " n750V1e23r96ok "
                Log.e(TAG, "测试指令关闭：$data")
                return data.toByteArray()
            }
            else -> return ByteArray(1)
        }
    }

    private fun getStr(str: String): String {
        if (str.length <= 2) {
            return str
        }
        return str.substring(0, 2) + ":" + getStr(str.substring(2))
    }
}