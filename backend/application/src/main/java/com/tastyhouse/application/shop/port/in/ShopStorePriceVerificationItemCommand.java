package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record ShopStorePriceVerificationItemCommand(
    Long productId,
    Long priceId,
    Integer storePrice,
    Boolean applyPickupSamePrice
) {
    public ShopStorePriceVerificationItemCommand {
        if (productId == null || storePrice == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static ShopStorePriceVerificationItemCommand of(
        Long productId,
        Long priceId,
        Integer storePrice,
        Boolean applyPickupSamePrice
    ) {
        return new ShopStorePriceVerificationItemCommand(
            productId,
            priceId,
            storePrice,
            applyPickupSamePrice
        );
    }
}
