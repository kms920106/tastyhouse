package com.tastyhouse.application.product.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record ProductImageChangeRequestCommand(
    Long ceoId,
    Long shopId,
    Long productId
) {
    public ProductImageChangeRequestCommand {
        if (ceoId == null
            || shopId == null
            || productId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static ProductImageChangeRequestCommand of(Long ceoId, Long shopId, Long productId) {
        return new ProductImageChangeRequestCommand(ceoId, shopId, productId);
    }
}
