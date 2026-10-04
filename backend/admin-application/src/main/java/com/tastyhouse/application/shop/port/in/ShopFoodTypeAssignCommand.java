package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record ShopFoodTypeAssignCommand(
    Long shopId,
    Long foodTypeCategoryId
) {

    public ShopFoodTypeAssignCommand {
        if (shopId == null || foodTypeCategoryId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }
}
