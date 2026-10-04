package com.tastyhouse.application.product.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record ProductDeactivateCommand(Long productId) {

    public ProductDeactivateCommand {
        if (productId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }

    public static ProductDeactivateCommand of(Long productId) {
        return new ProductDeactivateCommand(productId);
    }
}
