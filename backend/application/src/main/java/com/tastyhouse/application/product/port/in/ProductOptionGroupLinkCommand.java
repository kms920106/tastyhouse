package com.tastyhouse.application.product.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record ProductOptionGroupLinkCommand(
    Long ceoId,
    Long shopId,
    Long productId,
    Long optionGroupId
) {
    public ProductOptionGroupLinkCommand {
        if (ceoId == null
            || shopId == null
            || productId == null
            || optionGroupId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
