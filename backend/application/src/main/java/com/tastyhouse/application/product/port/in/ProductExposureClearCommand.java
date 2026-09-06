package com.tastyhouse.application.product.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record ProductExposureClearCommand(
    Long ceoId,
    Long shopId,
    Long productId
) {
    public ProductExposureClearCommand {
        if (ceoId == null
            || shopId == null
            || productId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static ProductExposureClearCommand of(Long ceoId, Long shopId, Long productId) {
        return new ProductExposureClearCommand(ceoId, shopId, productId);
    }
}
