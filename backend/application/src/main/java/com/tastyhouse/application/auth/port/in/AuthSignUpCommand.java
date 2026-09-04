package com.tastyhouse.application.auth.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 일반 회원가입 커맨드.
 *
 * <p>경계 타입만 싣는다 — 성별은 도메인 enum({@code MemberGender}) 후보이지만 {@code String}으로 받고
 * 승격은 서비스가 담당한다(backend/CLAUDE.md 경계 타입 규칙).
 */
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
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
        if (password == null || password.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
