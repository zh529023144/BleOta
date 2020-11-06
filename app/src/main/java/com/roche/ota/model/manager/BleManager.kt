package com.roche.ota.model.manager

import android.bluetooth.BluetoothGattCharacteristic
import android.os.ParcelUuid
import android.util.Log
import com.jakewharton.rx.ReplayingShare
import com.polidea.rxandroidble2.RxBleConnection
import com.polidea.rxandroidble2.RxBleCustomOperation
import com.polidea.rxandroidble2.RxBleDevice
import com.polidea.rxandroidble2.scan.ScanFilter
import com.polidea.rxandroidble2.scan.ScanResult
import com.polidea.rxandroidble2.scan.ScanSettings
import com.roche.ota.api.UrlConstant
import com.roche.ota.base.App
import com.roche.ota.model.event.BleEvent
import com.roche.ota.utils.HexUtil
import com.roche.ota.utils.isConnected
import com.roche.ota.utils.showToast
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.internal.operators.observable.ObservableCreate
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import java.util.*
import java.util.concurrent.TimeUnit

class BleManager private constructor() {
    val TAG = "BleManager"

    companion object {
        val instance = SingletonHolder.holder
    }

    private object SingletonHolder {
        val holder = BleManager()
    }

    private val rxBleClient = App.rxBleClient
    private lateinit var bleDevice: RxBleDevice
    private val connectionDisposable = CompositeDisposable()
    private lateinit var connectionObservable: Observable<RxBleConnection>
    var mBleState: Boolean = false

    private val mBleManagerListenerList = mutableListOf<BleManagerListener>()
    private val mList = mutableListOf<String>()
    private var scanDisposable: Disposable? = null

    private fun prepareConnectionObservable(): Observable<RxBleConnection> =
        bleDevice
            .establishConnection(false)
            .takeUntil(PublishSubject.create<Unit>())
            .compose(ReplayingShare.instance())


    interface BleManagerListener {
        fun scan(scanResult: ScanResult)
        fun connecting()
        fun connectOnSuccess()
        fun connectOnError()
        fun receiveMessage(dataBean: BleEvent)
        fun writeOnSuccess(id: String)
    }

    fun addBleManagerListener(bleManagerListener: BleManagerListener) {
        mBleManagerListenerList.add(bleManagerListener)
    }

    fun removeBleManagerListener(bleManagerListener: BleManagerListener) {
        mBleManagerListenerList.remove(bleManagerListener)
    }

    fun clearBleManagerListener() {
        mBleManagerListenerList.clear()
    }


    //蓝牙连接状态
    fun bleState(): Boolean {
        return bleDevice.isConnected
    }

    //蓝牙扫描
    fun scanBle() {
        scanBleDevices()
            .observeOn(AndroidSchedulers.mainThread())
            .doFinally {
                scanDisposable = null
            }
            .subscribe({

                val device = it.bleDevice

                Log.e(TAG, "扫描到的蓝牙地址：" + device.macAddress)
                mBleManagerListenerList.forEach { listener ->
                    listener.scan(it)
                }
            }, {
                Log.e(TAG, "扫描到的蓝牙 error：$it")
            })
            .let { scanDisposable = it }
    }

    fun stopScan() {
        scanDisposable?.dispose()
        scanDisposable = null
    }

    //蓝牙连接
    fun connectBle() {
//        val bleMac = getStr(UrlConstant.blueToothId)

        bleDevice = rxBleClient.getBleDevice(UrlConstant.blueToothId)
        connectionObservable = prepareConnectionObservable()

        //监听设备状态
        bleDevice.observeConnectionStateChanges()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ state ->
                when (state) {
                    RxBleConnection.RxBleConnectionState.CONNECTING -> {
                        Log.e(TAG, "设备正在连接")
                        mBleManagerListenerList.forEach { listener ->
                            listener.connecting()
                        }

                    }
                    RxBleConnection.RxBleConnectionState.CONNECTED -> {
                        Log.e(TAG, "设备已经连接")
                        //由于蓝牙连接成功 不一定写入成功 以写入成功为主 判断真的成功了
                    }
                    RxBleConnection.RxBleConnectionState.DISCONNECTED -> {
                        Log.e(TAG, "设备断开连接")
                        //重新连接 清除之前的状态
                        mBleState = false
                        mList.add("1")
                        if (mList.size == 1) {
                            mBleManagerListenerList.forEach {
                                it.connectOnError()
                            }
                            mList.clear()
                        }
                    }
                    else -> {
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
                                mBleState = true
                                // 收到通知
                                val data = String(it)

                                Log.e(TAG, "收到通知:$data    16进制转换后的数据" + HexUtil.encodeHexStr(it))

                                mBleManagerListenerList.forEach { listener ->
                                    listener.connectOnSuccess()
                                    listener.receiveMessage(
                                        BleEvent(
                                            1,
                                            "设备通知",
                                            data,
                                            "fee1",
                                            HexUtil.encodeHexStr(it)
                                        )
                                    )
                                }

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
            .flatMapSingle { it.writeCharacteristic(UrlConstant.WRITE_UUID, getBytes()) }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ bytes ->
                val data = String(bytes)
                Log.e(TAG, "写入成功:$data")
            }, { throwable ->
                Log.e(TAG, "写入异常 错误信息:" + throwable + "错误信息：")
                showToast("蓝牙数据发生错误")
            })
            .let { connectionDisposable.add(it) }


    }

    //断开蓝牙
    fun stopBle() {
        if (!connectionDisposable.isDisposed) {
            Log.e(TAG, "已存入监听数量：${connectionDisposable.size()}")//0 是蓝牙未连接 1 是状态 2是+读写 3是+单独写
            connectionDisposable.clear()
        }
    }

    //单独写入
    fun write(i: ByteArray) {
        if (bleDevice.isConnected) {
            connectionObservable
//                .subscribeOn(Schedulers.io())
                .flatMapSingle { it.writeCharacteristic(UrlConstant.WRITE_UUID, i) }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ bytes ->
                    val data = String(bytes)
                    Log.e(TAG, "写入成功:$data")
                    showToast("发送成功")
                }, { throwable ->
                    Log.e(TAG, "写入异常:${throwable}")
                })
                .let { connectionDisposable.add(it) }
        } else {
            showToast("蓝牙未连接")
        }

    }

    //分服务 单独写入
    fun write(i: ByteArray, uuid: UUID) {
        if (bleDevice.isConnected) {
            connectionObservable
                .flatMapSingle {
                    it.writeCharacteristic(uuid, i)
                }
                .subscribe({ bytes ->
                    val data = HexUtil.encodeHexStr(bytes)
                    Log.e(TAG, "${uuid}写入成功:$data")
                    mBleManagerListenerList.forEach { listener ->
                        listener.writeOnSuccess(uuid.toString())
                    }
                }, { throwable ->
                    Log.e(TAG, "${uuid}写入异常:${throwable}")
                })

                .let { connectionDisposable.add(it) }


        } else {
            showToast("蓝牙未连接")
        }

    }

    //分服务 写入长数据
    fun writeLong(i: ByteArray, uuid: UUID) {
        if (bleDevice.isConnected) {
            connectionObservable
                .flatMap {
                    it.createNewLongWriteBuilder()
                        .setCharacteristicUuid(uuid)
                        .setBytes(i)
                        .setMaxBatchSize(18)
                        .build()
                }
                .subscribe({ bytes ->
                    val data = String(bytes)
                    Log.e(TAG, "写入成功:$data")

                    mBleManagerListenerList.forEach { listener ->
                        listener.writeOnSuccess(uuid.toString())
                    }
                }, { throwable ->
                    Log.e(TAG, "写入异常:${throwable}")
                })
                .let { connectionDisposable.add(it) }

        } else {
            showToast("蓝牙未连接")
        }

    }

    //分服务 单独通知
    fun notify(uuid: UUID) {
        if (bleDevice.isConnected) {
            connectionObservable
                .flatMap { it.setupNotification(uuid) }
                .doOnNext { Log.e(TAG, "Notifications has been set up") }
                // we have to flatmap in order to get the actual notification observable
                // out of the enclosing observable, which only performed notification setup
                .flatMap { it }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    val data = Arrays.toString(it)
//                    Log.e(TAG, "收到通知:$data")

                    Log.e(TAG, "收到通知:$data    16进制转换后的数据" + HexUtil.encodeHexStr(it))

                    mBleManagerListenerList.forEach { listener ->
                        listener.receiveMessage(
                            BleEvent(
                                1,
                                "设备通知",
                                data,
                                uuid.toString(),
                                HexUtil.encodeHexStr(it)
                            )
                        )
                    }

                }, {
                    Log.e(TAG, "写入异常:${it}")
                })
                .let { connectionDisposable.add(it) }
        } else {
            showToast("蓝牙未连接")
        }

    }
    //蓝牙重连

    fun bleReConnect() {
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

    //设置特征值 属性

    fun setBleWriteType(uuid: UUID) {
        connectionObservable.flatMapSingle { it.discoverServices() }
            .flatMapSingle {
                it.getCharacteristic(uuid)
            }
            .doOnNext {
                Log.e(TAG, "BluetoothGattCharacteristic:${it.uuid}")
                it.writeType = BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ characteristic ->
                Log.e(TAG, "characteristic:$characteristic")
            },
                {
                    Log.e(TAG, "characteristic:$it")
                }
            ).let {
                connectionDisposable.add(it)
            }
    }


    private fun getBytes(): ByteArray {

        val data = UrlConstant.DEVICE_POWER
        return data.toByteArray()

    }




    private fun scanBleDevices(): Observable<ScanResult> {
        val scanSettings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
            .build()

        val scanFilter = ScanFilter.Builder()
//            .setDeviceAddress("82:20:99:26:0C:98")
            // add custom filters if needed
            .build()

        return rxBleClient.scanBleDevices(scanSettings, scanFilter)
    }
}