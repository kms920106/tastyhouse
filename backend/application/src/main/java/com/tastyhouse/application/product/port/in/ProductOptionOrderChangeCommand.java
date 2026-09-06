package com.tastyhouse.application.product.port.in;

import java.util.List;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record ProductOptionOrderChangeCommand(
    Long ceoId,
    Long shopId,
    Long optionGroupId,
    List<Long> optionIds
) {
    public ProductOptionOrderChangeCommand {
        if (ceoId == null
            || shopId == null
            || optionGroupId == null
            || optionIds == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
