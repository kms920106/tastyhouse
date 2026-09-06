package com.tastyhouse.application.product.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record ProductOptionManagementCreateCommand(
    Long optionGroupId,
    String name,
    Integer additionalPrice,
    Integer sort,
    Boolean soldOut,
    Boolean visible,
    Integer cupCount,
    Integer personalCupDiscountAmount
) {
    public ProductOptionManagementCreateCommand {
        if (optionGroupId == null || name == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
