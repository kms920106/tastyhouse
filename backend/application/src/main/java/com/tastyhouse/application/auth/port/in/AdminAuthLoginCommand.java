package com.tastyhouse.application.auth.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 관리자 로그인 커맨드.
 *
 * <p>경계 타입만 싣는다. compact constructor에는 필수값 누락 같은 구조적 가드만 두고,
 * 형식 검증은 {@code LoginRequest}의 jakarta.validation에 남겨 400 계약·한국어 메시지를 보존한다
 * (backend/CLAUDE.md Command 가드 규칙).
 */
public record AdminAuthLoginCommand(
    String username,
    String password,
    boolean rememberMe
) {

    public AdminAuthLoginCommand {
        if (username == null || username.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
        if (password == null || password.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static AdminAuthLoginCommand of(String username, String password, boolean rememberMe) {
        return new AdminAuthLoginCommand(username, password, rememberMe);
    }
}
