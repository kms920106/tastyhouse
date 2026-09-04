package com.tastyhouse.application.auth.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 점주 로그인 커맨드.
 *
 * <p>경계 타입만 싣는다. compact constructor에는 필수값 누락 같은 구조적 가드만 두고, 형식 검증은
 * {@code LoginRequest}의 jakarta.validation에 남겨 400 계약·한국어 메시지를 보존한다
 * (backend/CLAUDE.md Command 가드 규칙).
 *
 * <p>{@code ipAddress}·{@code userAgent}는 <b>가드하지 않는다</b> — 개인정보처리시스템 접속기록에 남길
 * 값이지 인증 입력이 아니고, 프록시 구성이나 클라이언트에 따라 정당하게 비어 있을 수 있다. 여기서
 * 막으면 로그인 자체가 실패해 <b>기록을 남기려다 서비스를 끊는</b> 역전이 일어난다.
 */
public record CeoAuthLoginCommand(
    String username,
    String password,
    boolean rememberMe,
    String ipAddress,
    String userAgent
) {

    public CeoAuthLoginCommand {
        if (username == null || username.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
        if (password == null || password.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static CeoAuthLoginCommand of(
        String username,
        String password,
        boolean rememberMe,
        String ipAddress,
        String userAgent
    ) {
        return new CeoAuthLoginCommand(username, password, rememberMe, ipAddress, userAgent);
    }
}
