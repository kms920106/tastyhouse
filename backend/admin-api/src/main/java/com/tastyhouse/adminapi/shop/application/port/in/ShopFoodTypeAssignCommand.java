package com.tastyhouse.adminapi.shop.application.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/** 가게 음식종류 지정 command. */
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
