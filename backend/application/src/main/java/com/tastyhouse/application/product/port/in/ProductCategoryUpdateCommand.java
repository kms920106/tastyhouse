package com.tastyhouse.application.product.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record ProductCategoryUpdateCommand(
    Long ceoId,
    Long productCategoryId,
    Long shopId,
    String name,
    String description
) {
    public ProductCategoryUpdateCommand {
        if (ceoId == null
            || productCategoryId == null
            || shopId == null
            || name == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
