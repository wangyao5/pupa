package com.pupa;

import com.pupa.dispatch.vo.RStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/***
 * 统一异常处理
 */
@RestControllerAdvice
public class ExceptionHandlers {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExceptionHandlers.class);

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map serverExceptionHandler(Exception ex) {
        LOGGER.error(ex.getMessage(), ex);
        RStatus rStatus = new RStatus(RStatus.EXCEPTION);
        rStatus.setMessage(ex.getMessage());
        return rStatus.convertMap();
    }
}