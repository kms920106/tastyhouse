package com.tastyhouse.domain.ceo.model;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 점주 로그인 시도의 결과.
 *
 * <p>{@code from(String)}이 있는 이유는 이 enum이 조회 필터로 HTTP 경계를 넘어오기 때문이다 — 점주가
 * 자기 로그인 이력을 "성공만/실패만"으로 좁혀 볼 수 있다(도메인 enum 경계 규칙).
 */
public enum CeoLoginResult {

    SUCCESS("로그인 성공"),
    FAILURE("로그인 실패");

    private final String description;

    CeoLoginResult(String description) {
        this.description = description;
    }

    public static CeoLoginResult from(String code) {
        try {
            return valueOf(code);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.CEO_LOGIN_RESULT_UNKNOWN,
                ErrorCode.CEO_LOGIN_RESULT_UNKNOWN.getDefaultMessage() + ": " + code);
        }
    }

    public String getDescription() {
        return this.description;
    }
}
