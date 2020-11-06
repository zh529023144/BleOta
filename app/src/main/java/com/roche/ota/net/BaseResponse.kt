package com.luoqu.commandcenter.net
/**
 * Created by xuhao on 2017/11/16.
 * 封装返回的数据
 */
class BaseResponse<T>(val retCode :String,
                      val msg:String,
                      val data:T)