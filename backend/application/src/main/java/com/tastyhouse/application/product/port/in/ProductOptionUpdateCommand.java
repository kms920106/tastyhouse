package com.tastyhouse.application.product.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record ProductOptionUpdateCommand(
    Long ceoId,
    Long optionId,
    Long shopId,
    String name,
    Integer additionalPrice,
    Integer cupCount,
    Integer personalCupDiscountAmount
) {
    public ProductOptionUpdateCommand {
        if (ceoId == null
            || optionId == null
            || shopId == null
            || name == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
