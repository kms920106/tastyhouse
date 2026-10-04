package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record ShopFoodTypeUnassignCommand(
    Long shopId,
    Long foodTypeCategoryId
) {

    public ShopFoodTypeUnassignCommand {
        if (shopId == null || foodTypeCategoryId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }

    public static ShopFoodTypeUnassignCommand of(Long shopId, Long foodTypeCategoryId) {
        return new ShopFoodTypeUnassignCommand(shopId, foodTypeCategoryId);
    }
}
