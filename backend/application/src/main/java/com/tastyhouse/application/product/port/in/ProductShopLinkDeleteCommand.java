package com.tastyhouse.application.product.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record ProductShopLinkDeleteCommand(
    Long ceoId,
    Long productId,
    Long targetShopId
) {
    public ProductShopLinkDeleteCommand {
        if (ceoId == null
            || productId == null
            || targetShopId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static ProductShopLinkDeleteCommand of(Long ceoId, Long productId, Long targetShopId) {
        return new ProductShopLinkDeleteCommand(ceoId, productId, targetShopId);
    }
}
