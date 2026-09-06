package com.tastyhouse.application.product.port.in;

import java.util.List;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record ProductOptionGroupOrderChangeCommand(
    Long ceoId,
    Long shopId,
    Long productId,
    List<Long> optionGroupIds
) {
    public ProductOptionGroupOrderChangeCommand {
        if (ceoId == null
            || shopId == null
            || productId == null
            || optionGroupIds == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
