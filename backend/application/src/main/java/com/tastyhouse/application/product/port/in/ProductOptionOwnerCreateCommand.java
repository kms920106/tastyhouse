package com.tastyhouse.application.product.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record ProductOptionOwnerCreateCommand(
    Long ceoId,
    Long shopId,
    Long optionGroupId,
    String name,
    Integer additionalPrice,
    Integer cupCount,
    Integer personalCupDiscountAmount
) {
    public ProductOptionOwnerCreateCommand {
        if (ceoId == null
            || shopId == null
            || optionGroupId == null
            || name == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
