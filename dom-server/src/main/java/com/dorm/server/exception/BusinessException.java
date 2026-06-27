package com.dorm.server.exception;

/**
 * 业务异常类
 * 用于封装业务层主动抛出的异常，统一由全局异常处理器捕获返回
 *
 * @author dorm-server
 */
public class BusinessException extends RuntimeException {

    /** 业务错误码 */
    private final Integer code;

    /**
     * 默认500错误码
     *
     * @param message 错误提示信息
     */
    public BusinessException(String message) {
        super(message);
        this.code = 500;
    }

    /**
     * 自定义错误码
     *
     * @param code    错误状态码
     * @param message 错误提示信息
     */
    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    /**
     * 获取错误码
     *
     * @return 错误码
     */
    public Integer getCode() {
        return code;
    }
}
