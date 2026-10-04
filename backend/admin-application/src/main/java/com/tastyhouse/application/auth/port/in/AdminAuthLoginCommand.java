package com.tastyhouse.application.auth.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record AdminAuthLoginCommand(
    String username,
    String password,
    boolean rememberMe
) {

    public AdminAuthLoginCommand {
        if (username == null || username.isBlank()) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
        if (password == null || password.isBlank()) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }

    public static AdminAuthLoginCommand of(String username, String password, boolean rememberMe) {
        return new AdminAuthLoginCommand(username, password, rememberMe);
    }
}
