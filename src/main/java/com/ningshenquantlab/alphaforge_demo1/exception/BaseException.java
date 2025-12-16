package com.ningshenquantlab.alphaforge_demo1.exception;

import lombok.Getter;

@Getter
public class BaseException extends RuntimeException {
    private Integer code;
    //定义了可以传入错误码和默认500错误码的两种异常
    public BaseException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    public BaseException(String message) {
        super(message);
        this.code = 500;
    }
}
