package com.ningshenquantlab.alphaforge_demo1.exception;

// 资源不存在异常
public class ResourceNotFoundException extends BaseException {
    public ResourceNotFoundException(String message) {
        super(404, message);
    }
}
