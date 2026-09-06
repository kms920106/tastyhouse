package com.tastyhouse.application.product.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record ProductDeactivateCommand(Long productId) {
    public ProductDeactivateCommand {
        if (productId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static ProductDeactivateCommand of(Long productId) {
        return new ProductDeactivateCommand(productId);
    }
}
