package com.roche.ota.model.bean

import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanRecord

class BleDevice(var device: BluetoothDevice, var rssi: Int,
                var scanRecordBytes: ByteArray,var isConnectable:Boolean = true,
                var scanRecord: ScanRecord? = null) {
}