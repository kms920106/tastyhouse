package com.tastyhouse.application.ceo.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

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
