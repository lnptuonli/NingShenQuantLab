package com.ningshenquantlab.alphaforge_demo1.exception;

// 未授权异常
public class UnauthorizedException extends BaseException {
    public UnauthorizedException(String message) {
        super(401, message);
    }
}
