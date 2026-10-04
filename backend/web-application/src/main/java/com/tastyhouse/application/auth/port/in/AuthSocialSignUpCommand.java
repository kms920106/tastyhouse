package com.tastyhouse.application.auth.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record AuthSocialSignUpCommand(
    String provider,
    String tempToken,
    String username,
    String nickname,
    String fullName,
    String gender,
    Integer birthDate,
    String phoneNumber,
    boolean pushNotificationEnabled,
    boolean marketingInfoEnabled,
    boolean eventInfoEnabled,
    String referrerNickname
) {

    public AuthSocialSignUpCommand {
        if (provider == null || provider.isBlank()) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
        if (tempToken == null || tempToken.isBlank()) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }
}
