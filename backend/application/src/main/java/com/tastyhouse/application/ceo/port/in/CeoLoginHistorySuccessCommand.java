package com.tastyhouse.application.ceo.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 로그인 성공 이력 기록 command. 요청 본문이 없는 연산이므로 호출부가 정적 팩토리로 조립한다.
 */
public record CeoLoginHistorySuccessCommand(
    Long ceoId,
    String ipAddress,
    String userAgent
) {
    public CeoLoginHistorySuccessCommand {
        if (ceoId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static CeoLoginHistorySuccessCommand of(Long ceoId, String ipAddress, String userAgent) {
        return new CeoLoginHistorySuccessCommand(ceoId, ipAddress, userAgent);
    }
}
