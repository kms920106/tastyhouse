package com.tastyhouse.application.auth.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record CeoAuthLoginCommand(
    String username,
    String password,
    boolean rememberMe,
    String ipAddress,
    String userAgent
) {

    public CeoAuthLoginCommand {
        if (username == null || username.isBlank()) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
        if (password == null || password.isBlank()) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
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
