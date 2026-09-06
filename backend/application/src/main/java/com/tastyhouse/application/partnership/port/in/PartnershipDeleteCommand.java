package com.tastyhouse.application.partnership.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record PartnershipDeleteCommand(Long partnershipRequestId) {
    public PartnershipDeleteCommand {
        if (partnershipRequestId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static PartnershipDeleteCommand of(Long partnershipRequestId) {
        return new PartnershipDeleteCommand(partnershipRequestId);
    }
}
