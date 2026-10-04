package com.tastyhouse.application.product.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

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
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }
}
