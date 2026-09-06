package com.tastyhouse.application.partnership.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record PartnershipStatusChangeCommand(
    Long partnershipRequestId,
    String status
) {
    public PartnershipStatusChangeCommand {
        if (partnershipRequestId == null || status == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
