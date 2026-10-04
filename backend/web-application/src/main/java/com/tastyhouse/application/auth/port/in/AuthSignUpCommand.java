package com.tastyhouse.application.auth.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record AuthSignUpCommand(
    String username,
    String password,
    String nickname,
    String fullName,
    String gender,
    Integer birthDate,
    String phoneNumber,
    boolean pushNotificationEnabled,
    boolean marketingInfoEnabled,
    boolean eventInfoEnabled,
    String smsVerifyToken,
    String mailVerifyToken,
    String referrerNickname
) {

    public AuthSignUpCommand {
        if (username == null || username.isBlank()) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
        if (password == null || password.isBlank()) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }
}
