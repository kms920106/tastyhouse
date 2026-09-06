package com.tastyhouse.application.product.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record ProductRepresentativeClearCommand(
    Long ceoId,
    Long shopId,
    Long productId
) {
    public ProductRepresentativeClearCommand {
        if (ceoId == null
            || shopId == null
            || productId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static ProductRepresentativeClearCommand of(Long ceoId, Long shopId, Long productId) {
        return new ProductRepresentativeClearCommand(ceoId, shopId, productId);
    }
}
