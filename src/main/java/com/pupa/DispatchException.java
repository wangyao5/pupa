package com.pupa;

/**
 * 自定义异常
 */
public class DispatchException extends RuntimeException {
    public DispatchException(String message, Throwable cause) {
        super(message, cause);
    }
}