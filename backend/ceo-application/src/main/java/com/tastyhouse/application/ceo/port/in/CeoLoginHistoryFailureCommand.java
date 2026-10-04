package com.tastyhouse.application.ceo.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record CeoLoginHistoryFailureCommand(
    Long ceoId,
    String failureReason,
    String ipAddress,
    String userAgent
) {

    public CeoLoginHistoryFailureCommand {
        if (ceoId == null || failureReason == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
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
