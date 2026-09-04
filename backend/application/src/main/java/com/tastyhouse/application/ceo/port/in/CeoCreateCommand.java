package com.tastyhouse.application.ceo.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 점주 계정 생성 command.
 *
 * <p>{@code encodedPassword}는 이미 인코딩된 값이다 — 호출 측(시드)이 자체 정책에 따라 인코딩 시점을
 * 통제하기 때문이다.
 */
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
