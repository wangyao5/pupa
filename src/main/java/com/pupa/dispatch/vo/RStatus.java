package com.pupa.dispatch.vo;

import java.util.HashMap;
import java.util.Map;

/**
 * 返回数据状态
 */
public class RStatus<T> {
    public static final int SUCCESS = 100;
    public static final int FATAL = 200;
    public static final int EXCEPTION = 500;
    private int code;
    private String message;
    private T body;

    public RStatus(int code) {
        this.code = code;
    }

    public static int getSUCCESS() {
        return SUCCESS;
    }

    public static int getFATAL() {
        return FATAL;
    }

    public static int getEXCEPTION() {
        return EXCEPTION;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getBody() {
        return body;
    }

    public void setBody(T body) {
        this.body = body;
    }

    public Map <String, Object> convertMap() {
        Map <String, Object> map = new HashMap <>();
        map.put("code", code);
        if (null != message && !message.isEmpty()) map.put("message", message);
        if (null != body) map.put("body", body);
        return map;
    }
}
