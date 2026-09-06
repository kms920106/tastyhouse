package com.tastyhouse.application.auth.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record CeoAuthLoginCommand(
    String username,
    String password,
    boolean rememberMe,
    String ipAddress,
    String userAgent
) {

    public CeoAuthLoginCommand {
        if (username == null || username.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
        if (password == null || password.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static CeoAuthLoginCommand of(
        String username,
        String password,
        boolean rememberMe,
        String ipAddress,
        String userAgent
    ) {
        return new CeoAuthLoginCommand(username, password, rememberMe, ipAddress, userAgent);
    }
}
