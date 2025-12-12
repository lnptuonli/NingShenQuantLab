package com.ningshenquantlab.alphaforge_demo1.exception;

// 无权限异常
public class ForbiddenException extends BaseException {
    public ForbiddenException(String message) {
        super(403, message);
    }
}
