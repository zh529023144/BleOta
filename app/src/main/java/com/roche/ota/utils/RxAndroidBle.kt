package com.roche.ota.utils

import com.polidea.rxandroidble2.RxBleConnection
import com.polidea.rxandroidble2.RxBleDevice

/**
 * Returns `true` if connection state is [CONNECTED][RxBleConnection.RxBleConnectionState.CONNECTED].
 */
internal val RxBleDevice.isConnected: Boolean
    get() = connectionState == RxBleConnection.RxBleConnectionState.CONNECTED
internal val RxBleDevice.isDisConnected: Boolean
    get() = connectionState == RxBleConnection.RxBleConnectionState.DISCONNECTED