package com.oa_server.common.exception;

import com.oa_server.common.result.ResultCode;
import lombok.Getter;

/**
 * 业务异常
 *
 * @author Alu
 * @date 2026-09-09
 */
@Getter
public class BusinessException extends RuntimeException {
    private final Integer code;

    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
    }

    public BusinessException(ResultCode resultCode, String message) {
        super(message);
        this.code = resultCode.getCode();
    }

    public BusinessException(String message) {
        super(message);
        this.code = ResultCode.FAIL.getCode();
    }

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }
}
