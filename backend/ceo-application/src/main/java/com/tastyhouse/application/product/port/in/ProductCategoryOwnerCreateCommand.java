package com.tastyhouse.application.product.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record ProductCategoryOwnerCreateCommand(
    Long ceoId,
    Long shopId,
    String name,
    String description
) {

    public ProductCategoryOwnerCreateCommand {
        if (ceoId == null
            || shopId == null
            || name == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }
}
