package com.tastyhouse.application.product.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record ProductRepresentativeApproveCommand(Long requestId) {
    public ProductRepresentativeApproveCommand {
        if (requestId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static ProductRepresentativeApproveCommand of(Long requestId) {
        return new ProductRepresentativeApproveCommand(requestId);
    }
}
