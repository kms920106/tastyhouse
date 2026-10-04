package com.tastyhouse.application.product.port.in;

import java.util.List;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record ProductCategoryReorderCommand(
    Long ceoId,
    Long shopId,
    List<Long> productCategoryIds
) {

    public ProductCategoryReorderCommand {
        if (ceoId == null
            || shopId == null
            || productCategoryIds == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }
}
