package com.tastyhouse.application.auth.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 소셜 회원가입 커맨드.
 *
 * <p>{@code provider}·{@code gender}는 도메인 enum 후보이지만 경계에서는 {@code String}으로 받고
 * 승격({@code MemberSocialProvider.from}·{@code MemberGender.from})은 서비스가 담당한다.
 */
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
