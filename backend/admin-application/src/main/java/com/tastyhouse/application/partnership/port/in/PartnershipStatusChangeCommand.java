package com.tastyhouse.application.partnership.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record PartnershipStatusChangeCommand(
    Long partnershipRequestId,
    String status
) {

    public PartnershipStatusChangeCommand {
        if (partnershipRequestId == null || status == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }
}
