package com.tastyhouse.application.product.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record ProductRepresentativeRejectCommand(Long requestId, String rejectReason) {
    public ProductRepresentativeRejectCommand {
        if (requestId == null || rejectReason == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
