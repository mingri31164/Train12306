package com.mingri.train12306.framework.starter.convention.exception;

import com.mingri.train12306.framework.starter.convention.errorcode.IErrorCode;
import org.springframework.util.StringUtils;
import lombok.Getter;

import java.util.Optional;

/**
 * 抽象异常
 */
@Getter
public abstract class AbstractException extends RuntimeException {

    public final String errorCode;

    public final String errorMessage;

    public AbstractException(String message, Throwable throwable, IErrorCode errorCode) {
        super(message, throwable);
        this.errorCode = errorCode.code();
        this.errorMessage = Optional.ofNullable(StringUtils.hasLength(message) ? message : null).orElse(errorCode.message());
    }
}
