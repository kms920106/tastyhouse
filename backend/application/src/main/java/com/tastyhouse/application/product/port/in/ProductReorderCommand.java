package com.tastyhouse.application.product.port.in;

import java.util.List;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record ProductReorderCommand(
    Long ceoId,
    Long shopId,
    Long productCategoryId,
    List<Long> productIds
) {
    public ProductReorderCommand {
        if (ceoId == null
            || shopId == null
            || productCategoryId == null
            || productIds == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
