package com.tastyhouse.application.ceo.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record CeoCreateCommand(
    String username,
    String encodedPassword,
    String name
) {

    public CeoCreateCommand {
        if (username == null || encodedPassword == null || name == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }

    public static CeoCreateCommand of(String username, String encodedPassword, String name) {
        return new CeoCreateCommand(username, encodedPassword, name);
    }
}
