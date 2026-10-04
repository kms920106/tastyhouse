package com.tastyhouse.application.product.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record ProductCategoryDeleteCommand(
    Long ceoId,
    Long productCategoryId,
    Long shopId
) {

    public ProductCategoryDeleteCommand {
        if (ceoId == null
            || productCategoryId == null
            || shopId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }
}
