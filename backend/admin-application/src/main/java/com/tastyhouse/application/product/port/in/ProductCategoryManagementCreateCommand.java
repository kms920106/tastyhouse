package com.tastyhouse.application.product.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record ProductCategoryManagementCreateCommand(
    Long shopId,
    String name,
    Integer sort,
    Boolean visible
) {

    public ProductCategoryManagementCreateCommand {
        if (shopId == null || name == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }
}
