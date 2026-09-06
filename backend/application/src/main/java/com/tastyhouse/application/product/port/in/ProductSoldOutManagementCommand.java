package com.tastyhouse.application.product.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record ProductSoldOutManagementCommand(Long productId) {
    public ProductSoldOutManagementCommand {
        if (productId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static ProductSoldOutManagementCommand of(Long productId) {
        return new ProductSoldOutManagementCommand(productId);
    }
}
