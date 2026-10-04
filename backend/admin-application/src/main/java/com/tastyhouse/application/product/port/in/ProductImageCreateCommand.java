package com.tastyhouse.application.product.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record ProductImageCreateCommand(
    Long productId,
    Long imageFileId,
    Integer sort,
    Boolean visible
) {

    public ProductImageCreateCommand {
        if (productId == null || imageFileId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }
}
