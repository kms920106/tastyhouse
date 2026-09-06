package com.tastyhouse.application.product.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record ProductCategoryManagementCreateCommand(
    Long shopId,
    String name,
    Integer sort,
    Boolean visible
) {
    public ProductCategoryManagementCreateCommand {
        if (shopId == null || name == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
