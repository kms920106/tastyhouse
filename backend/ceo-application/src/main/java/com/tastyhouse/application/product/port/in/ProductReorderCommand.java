package com.tastyhouse.application.product.port.in;

import java.util.List;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

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
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }
}
