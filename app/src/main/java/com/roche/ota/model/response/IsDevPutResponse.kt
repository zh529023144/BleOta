package com.roche.ota.model.response

 data class IsDevPutResponse(
    val code: Int,
    val message: String,
    val result: DevPutResult,
    val success: Boolean,
    val timestamp: Long,
    var type:Int
)

data class DevPutResult(

    val dev: Dev,
    val keyStatus: String
)

data class Dev(
    val battery: Any,
    val blueToothId: String,
    val cellNum: Int,
    val couldModifySln: Any,
    val createBy: String,
    val createTime: String,
    val deadTime: String,
    val delFlag: Int,
    val devCellList: Any,
    val devId: String,
    val divide: Int,
    val expire: Boolean,
    val funcName: Any,
    val hasBoundInvestor: Int,
    val hotelId: Any,
    val hotelName: Any,
    val iccId: Any,
    val id: String,
    val imei: Any,
    val imsi: Any,
    val inPayRecord: Int,
    val initBTCode: Any,
    val investCompany: Any,
    val investManage: Int,
    val investor: Any,
    val isBind: Int,
    val isBindPartner: Any,
    val isSell: Int,
    val isTrue: Int,
    val ivMode: Any,
    val lastDeadTime: Any,
    val lastPartner: String,
    val location: Any,
    val mallAppId: Any,
    val model: String,
    val needPay: Int,
    val noItemCells: List<Any>,
    val offTime: Any,
    val onTime: Any,
    val operatePartner: Any,
    val ordinated: Int,
    val ordinator: Any,
    val partner: String,
    val patch: Any,
    val power: Any,
    val probability: Any,
    val qrCode: String,
    val qrImage: String,
    val regTime: Any,
    val remarks: Any,
    val rentMode: Int,
    val rentModePrice: Any,
    val replenishmentTime: Any,
    val roomCode: String,
    val scanAfterTime: Any,
    val scanInterval: Any,
    val scanNum: Int,
    val schemeId: Any,
    val schemeName: Any,
    val shopImgUrl: Any,
    val shopPath: Any,
    val slnId: Any,
    val softVersion: Any,
    val standbyVoltage: Any,
    val status: Int,
    val supervisor: Any,
    val updateBy: String,
    val updateTime: String,
    val useSpProfit: Any,
    val version: Any,
    val voltage: Any,
    val voltageUpdateTime: Any
)