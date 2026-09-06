package com.tastyhouse.application.auth.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record AdminAuthLoginCommand(
    String username,
    String password,
    boolean rememberMe
) {

    public AdminAuthLoginCommand {
        if (username == null || username.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
        if (password == null || password.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static AdminAuthLoginCommand of(String username, String password, boolean rememberMe) {
        return new AdminAuthLoginCommand(username, password, rememberMe);
    }
}
