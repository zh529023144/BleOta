package com.roche.ota.base

import com.google.gson.Gson
import okhttp3.MediaType
import okhttp3.RequestBody
import org.json.JSONObject

/**
 * 创建日期：2020/1/13 on 15:11
 * 描述:
 * 作者:张伦欢
 */
open class BaseModel {

    fun getRequestBody(obj: Any): RequestBody {
        val route = Gson().toJson(obj)
        return RequestBody.create(MediaType.parse("application/json; charset=utf-8"), route)
    }

    fun json2Body(obj: JSONObject): RequestBody {
        return RequestBody.create(MediaType.parse("application/json; charset=utf-8"), obj.toString())
    }
}