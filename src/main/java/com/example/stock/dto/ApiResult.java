package com.example.stock.dto;

public class ApiResult<T> {
    private int code;
    private String msg;
    private T data;

    public static <T> ApiResult<T> success(T data) {
        ApiResult<T> r = new ApiResult<>();
        r.code = 0;
        r.msg = "OK";
        r.data = data;
        return r;
    }

    public static <T> ApiResult<T> error(String msg) {
        ApiResult<T> r = new ApiResult<>();
        r.code = -1;
        r.msg = msg;
        return r;
    }

    public int getCode() { return code; }
    public void setCode(int code) { this.code = code; }
    public String getMsg() { return msg; }
    public void setMsg(String msg) { this.msg = msg; }
    public T getData() { return data; }
    public void setData(T data) { this.data = data; }
}
