package com.dorm.server.entity.common;

import lombok.Data;

/**
 * 统一响应结果类
 * 所有接口返回格式：{"code": 200, "msg": "操作成功", "data": T}
 *
 * @param <T> 响应数据类型
 * @author dorm-server
 */
@Data
public class Result<T> {

    /** 响应状态码 */
    private Integer code;

    /** 响应提示信息 */
    private String msg;

    /** 响应业务数据 */
    private T data;

    /**
     * 操作成功（携带数据）
     *
     * @param data 业务数据
     * @param <T>  数据类型
     * @return 成功结果
     */
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.code = 200;
        result.msg = "操作成功";
        result.data = data;
        return result;
    }

    /**
     * 操作成功（自定义提示 + 携带数据）
     *
     * @param msg  提示信息
     * @param data 业务数据
     * @param <T>  数据类型
     * @return 成功结果
     */
    public static <T> Result<T> success(String msg, T data) {
        Result<T> result = new Result<>();
        result.code = 200;
        result.msg = msg;
        result.data = data;
        return result;
    }

    /**
     * 操作失败（默认500）
     *
     * @param msg 错误提示信息
     * @param <T> 数据类型
     * @return 失败结果
     */
    public static <T> Result<T> error(String msg) {
        Result<T> result = new Result<>();
        result.code = 500;
        result.msg = msg;
        result.data = null;
        return result;
    }

    /**
     * 操作失败（自定义状态码）
     *
     * @param code 错误状态码
     * @param msg  错误提示信息
     * @param <T>  数据类型
     * @return 失败结果
     */
    public static <T> Result<T> error(Integer code, String msg) {
        Result<T> result = new Result<>();
        result.code = code;
        result.msg = msg;
        result.data = null;
        return result;
    }
}
