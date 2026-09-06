package com.tastyhouse.application.product.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record ProductImageCreateCommand(
    Long productId,
    Long imageFileId,
    Integer sort,
    Boolean visible
) {
    public ProductImageCreateCommand {
        if (productId == null || imageFileId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
