package com.ningshenquantlab.alphaforge_demo1.exception;

// 参数校验异常
public class ValidationException extends BaseException {
    public ValidationException(String message) {
        super(400, message);
    }
}