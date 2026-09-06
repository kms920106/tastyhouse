package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record ShopFoodTypeUnassignCommand(
    Long shopId,
    Long foodTypeCategoryId
) {
    public ShopFoodTypeUnassignCommand {
        if (shopId == null || foodTypeCategoryId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static ShopFoodTypeUnassignCommand of(Long shopId, Long foodTypeCategoryId) {
        return new ShopFoodTypeUnassignCommand(shopId, foodTypeCategoryId);
    }
}
