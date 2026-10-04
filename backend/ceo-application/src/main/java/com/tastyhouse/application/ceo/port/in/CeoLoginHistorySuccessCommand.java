package com.tastyhouse.application.ceo.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record CeoLoginHistorySuccessCommand(
    Long ceoId,
    String ipAddress,
    String userAgent
) {

    public CeoLoginHistorySuccessCommand {
        if (ceoId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }

    public static CeoLoginHistorySuccessCommand of(Long ceoId, String ipAddress, String userAgent) {
        return new CeoLoginHistorySuccessCommand(ceoId, ipAddress, userAgent);
    }
}
