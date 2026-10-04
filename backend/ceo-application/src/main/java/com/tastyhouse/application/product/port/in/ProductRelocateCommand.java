package com.tastyhouse.application.product.port.in;

import java.util.List;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record ProductRelocateCommand(
    Long ceoId,
    Long shopId,
    Long targetProductCategoryId,
    List<Long> productIds,
    List<Long> targetOrderedProductIds
) {

    public ProductRelocateCommand {
        if (ceoId == null
            || shopId == null
            || targetProductCategoryId == null
            || productIds == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }
}
