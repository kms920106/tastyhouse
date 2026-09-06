package com.tastyhouse.application.admin.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record AdminCreateCommand(
    String username,
    String password,
    String name,
    String role
) {
    public AdminCreateCommand {
        if (username == null || password == null || name == null || role == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static AdminCreateCommand of(String username, String password, String name, String role) {
        return new AdminCreateCommand(username, password, name, role);
    }
}
