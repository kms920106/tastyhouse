package com.tastyhouse.application.product.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record ProductSoldOutManagementCommand(Long productId) {

    public ProductSoldOutManagementCommand {
        if (productId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }

    public static ProductSoldOutManagementCommand of(Long productId) {
        return new ProductSoldOutManagementCommand(productId);
    }
}
