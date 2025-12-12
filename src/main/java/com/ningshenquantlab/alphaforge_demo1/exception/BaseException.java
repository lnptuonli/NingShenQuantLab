package com.ningshenquantlab.alphaforge_demo1.exception;

import lombok.Getter;

@Getter
public class BaseException extends RuntimeException {
    private Integer code;

    public BaseException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    public BaseException(String message) {
        super(message);
        this.code = 500;
    }
}
