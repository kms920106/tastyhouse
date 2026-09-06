package com.tastyhouse.application.ceo.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

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
