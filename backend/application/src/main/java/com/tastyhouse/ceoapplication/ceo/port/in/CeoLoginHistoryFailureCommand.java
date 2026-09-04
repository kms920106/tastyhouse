package com.tastyhouse.ceoapplication.ceo.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 로그인 실패 이력 기록 command. 요청 본문이 없는 연산이므로 호출부가 정적 팩토리로 조립한다.
 *
 * <p>{@code failureReason}은 경계 타입인 문자열로 받고, enum 승격은 서비스가 수행한다.
 */
public record CeoLoginHistoryFailureCommand(
    Long ceoId,
    String failureReason,
    String ipAddress,
    String userAgent
) {
    public CeoLoginHistoryFailureCommand {
        if (ceoId == null || failureReason == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static CeoLoginHistoryFailureCommand of(
        Long ceoId,
        String failureReason,
        String ipAddress,
        String userAgent
    ) {
        return new CeoLoginHistoryFailureCommand(ceoId, failureReason, ipAddress, userAgent);
    }
}
