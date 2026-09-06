package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record ShopFoodTypeAssignCommand(
    Long shopId,
    Long foodTypeCategoryId
) {
    public ShopFoodTypeAssignCommand {
        if (shopId == null || foodTypeCategoryId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
