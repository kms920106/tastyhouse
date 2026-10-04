package com.tastyhouse.application.partnership.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record PartnershipDeleteCommand(Long partnershipRequestId) {

    public PartnershipDeleteCommand {
        if (partnershipRequestId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }

    public static PartnershipDeleteCommand of(Long partnershipRequestId) {
        return new PartnershipDeleteCommand(partnershipRequestId);
    }
}
