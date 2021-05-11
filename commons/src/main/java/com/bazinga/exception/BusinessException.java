package com.bazinga.exception;

/**
 * 〈业务异常〉<p>
 * 不满足业务执行的条件时，抛出该异常
 *
 * @author yunshan
 * @date 16/11/23
 */
public class BusinessException extends RuntimeException {

    private static final long serialVersionUID = 4188232070213821571L;

    public BusinessException(String message) {
        this(message, null);
    }

    public BusinessException(Throwable cause) {
        this(null, cause);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }

}
