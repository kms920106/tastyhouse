package com.tastyhouse.application.auth.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

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
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
        if (tempToken == null || tempToken.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
