package com.tastyhouse.application.ceo.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record CeoCreateCommand(
    String username,
    String encodedPassword,
    String name
) {
    public CeoCreateCommand {
        if (username == null || encodedPassword == null || name == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static CeoCreateCommand of(String username, String encodedPassword, String name) {
        return new CeoCreateCommand(username, encodedPassword, name);
    }
}
