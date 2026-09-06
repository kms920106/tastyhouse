package com.tastyhouse.application.product.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record ProductImageChangeApproveCommand(Long requestId) {
    public ProductImageChangeApproveCommand {
        if (requestId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static ProductImageChangeApproveCommand of(Long requestId) {
        return new ProductImageChangeApproveCommand(requestId);
    }
}
