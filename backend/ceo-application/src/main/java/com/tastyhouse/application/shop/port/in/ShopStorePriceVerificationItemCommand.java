package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record ShopStorePriceVerificationItemCommand(
    Long productId,
    Long priceId,
    Integer storePrice,
    Boolean applyPickupSamePrice
) {

    public ShopStorePriceVerificationItemCommand {
        if (productId == null || storePrice == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
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
