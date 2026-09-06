package com.tastyhouse.application.product.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record ProductPriceItemCommand(
    Long priceId,
    String priceName,
    Integer deliveryPrice,
    Integer storePrice,
    Integer pickupPrice,
    Integer sort
) {
    public ProductPriceItemCommand {
        if (priceName == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
