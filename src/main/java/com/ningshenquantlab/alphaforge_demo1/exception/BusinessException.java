package com.ningshenquantlab.alphaforge_demo1.exception;

// 业务异常
public class BusinessException extends BaseException {
    public BusinessException(String message) {
        super(400, message);
    }

    public BusinessException(Integer code, String message) {
        super(code, message);
    }
}
