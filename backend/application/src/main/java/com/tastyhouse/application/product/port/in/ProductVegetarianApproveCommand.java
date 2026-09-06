package com.tastyhouse.application.product.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record ProductVegetarianApproveCommand(Long requestId) {
    public ProductVegetarianApproveCommand {
        if (requestId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static ProductVegetarianApproveCommand of(Long requestId) {
        return new ProductVegetarianApproveCommand(requestId);
    }
}
