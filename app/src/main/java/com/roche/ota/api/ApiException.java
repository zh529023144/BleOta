package com.roche.ota.api;

public class ApiException extends Exception {

    public static final String CODE_OK = "000";
    public static final String CODE_ANALYZE_ERROR = "001";
    public static final String CODE_PARAMS_ERROR = "002";
    public static final String CODE_SERVER_ERROR = "005";
    public static final String CODE_PROVIDER_UPDATE = "006";
    public static final String CODE_UNKNOW_ERROR = "999";
    public static final String CODE_NOT_FOUND_ERROR = "404";//服务器404
    public static final String CODE_NULL_RESPONSE = "007";//服务器返回的response为null
    public static final String CODE_SEVER_ERROR = "500";//服务器内部错误
    public static final String CODE_ERROR_PARAMS = "601";//token失效
    public static final String CODE_TOKEN_INVALID = "607";//token失效

    public static final String MSG_NULL_RESPONSE = "服务器返回数据为空";//服务器返回的response为null
    public static final String MSG_SEVER_ERROR = "服务器内部错误";//服务器内部错误
    public static final String MSG_ERROR_PARAMS = "请求参数错误";//请求参数错误
    public static final String MSG_TOKEN_INVALID = "token失效了，请重新登录";//token失效
    public static final String MSG_NONE_PERMISSION = "无访问权限";//无访问权限
    public static final String MSG_NOT_FOUND = "服务器404";//服务器404

    public ApiException(String code, String msg) {
        super(msg);
        this.code = code;
    }

    public ApiException(String code) {
        super(getMsg(code));
        this.code = code;
    }

    private static String getMsg(String code) {
        String msg = "UNKNOW";
        switch (code) {
            case CODE_NULL_RESPONSE:
                msg = MSG_NULL_RESPONSE;
                break;
            case CODE_SEVER_ERROR:
                msg = MSG_SEVER_ERROR;
                break;
            case CODE_ERROR_PARAMS:
                msg = MSG_ERROR_PARAMS;
                break;
            case CODE_TOKEN_INVALID:
                msg = MSG_TOKEN_INVALID;
                break;
            case CODE_NOT_FOUND_ERROR:
                msg = MSG_NOT_FOUND;
                break;
        }
        return msg;
    }


    private String code;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
