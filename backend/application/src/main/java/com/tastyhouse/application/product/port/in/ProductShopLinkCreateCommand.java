package com.tastyhouse.application.product.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record ProductShopLinkCreateCommand(
    Long ceoId,
    Long productId,
    Long targetShopId,
    Long productCategoryId
) {
    public ProductShopLinkCreateCommand {
        if (ceoId == null
            || productId == null
            || targetShopId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
