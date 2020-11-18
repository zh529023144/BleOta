package com.roche.ota.api

import android.os.Environment
import java.io.File
import java.util.*

/**
 *
 */
object UrlConstant {
//    const val BASE_URL = "http://192.168.0.142:8083/"
    const val BASE_URL = "https://test.nofetel.com/"
//    const val BASE_URL = "https://1.nofetel.com/"

    const val DEVICE_BLE_NAME = "0035FF0BF7D0"
    const val DEVICE_POWER = " n750V6 " //获取电量指令

    val old_service = UUID.fromString("0000fee0-0000-1000-8000-00805f9b34fb")
    val WRITE_UUID = UUID.fromString("0000fee1-0000-1000-8000-00805f9b34fb")
    val READ_UUID = UUID.fromString("0000fee1-0000-1000-8000-00805f9b34fb")

    val uuid_update = UUID.fromString("f000ffc0-0451-4000-b000-000000000000")
    val uuid_ota1 = UUID.fromString("f000ffc1-0451-4000-b000-000000000000")
    val uuid_ota2 = UUID.fromString("f000ffc2-0451-4000-b000-000000000000")

    //参数值

    //设备 Id
    var deviceId = ""
    //二维码信息
    var qrCode = ""
    //设备机型
    var model = ""
    //imei码
    var imei = ""
    //蓝牙id
    var blueToothId = ""
    //合伙人
    var user = ""
    //硬件版本
    var version = ""

    var func = ""

    var patch = ""

    //同步秘钥
    var synKey = ""

    var bTCode = ""
    //电压
    var voltage = 0

    //密码
    var hasPwd = 0

    //充电款
    var charge = 0

    //格子数
    var cellNum = 0
    var rxConnectNum = 0
    var isNoFirst = false

    //合伙人绑定的机型
    var model_user = ""

    //合伙人名称
    var contacts = ""

    var bleMac = ""

    //设备版本号
    var bleVersion = ""
    //升级版本号
    var upDateBleVersion = ""

    //类型
    const val close_door = "CLOSE_DOOR" //关门
    const val open_door = "OPEN_DOOR" //开盖
    const val one_key_test = "ONE_KEY_TEST" //一键测试
    const val one_key_to_right = "ONE_KEY_TO_RIGHT" //一键矫正柜门
    const val set_default_charge_time = "SET_DEFAULT_CHARGE_TIME" //设置充电时间
    const val open_lid = "OPEN_LID" //开指定柜门
    const val stop_to_lid = "STOP_TO_LID" //设置停靠柜门


    //语音值
    const val BLUE_CONNECT = 0x10  //蓝牙正在连接
    const val BLUE_CONNECT_1 = 0x11  //蓝牙连接成功
    const val BLUE_CONNECT_0 = 0x12  //蓝牙断开连接

    const val USB_CONNECT_1 = 0x13  //USB设备已连接
    const val USB_CONNECT_0 = 0x14  //USB设备已断开

    const val BLUE_POWER_1 = 0x15  //获取电量成功
    const val BLUE_POWER_0 = 0x16  //获取电量失败

    const val DEVICE_PUT = 0x17  //开始入库
    const val DEVICE_PUT_1 = 0x18  //入库成功
    const val DEVICE_PUT_0 = 0x19  //入库失败

    const val DEVICE_SYN_1 = 0x20  //同步设备成功
    const val DEVICE_SYN_0 = 0x21  //同步设备失败

    const val USB_CODE = 0x22  //收到usb指令

    const val DEVICE_001 = 0x23  //1.设备已生产完毕，请及时处理
    const val DEVICE_002 = 0x24  //2.设备蓝牙无法连接，请及时处理
    const val DEVICE_004 = 0x25  //4.设备入库成功
    const val DEVICE_005 = 0x26  //5.设备同步密钥成功
    const val DEVICE_006 = 0x27  //6.报警声
    const val DEVICE_007 = 0x28  //7.设备已经入库


    var LOCAL_FILE_UPDATE = Environment.getExternalStorageDirectory().absolutePath + "/ble_update.bin"
}