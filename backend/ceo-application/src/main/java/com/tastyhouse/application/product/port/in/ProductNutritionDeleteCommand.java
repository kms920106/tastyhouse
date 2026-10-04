package com.tastyhouse.application.product.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record ProductNutritionDeleteCommand(
    Long ceoId,
    Long shopId,
    Long productId
) {

    public ProductNutritionDeleteCommand {
        if (ceoId == null
            || shopId == null
            || productId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }

    public static ProductNutritionDeleteCommand of(Long ceoId, Long shopId, Long productId) {
        return new ProductNutritionDeleteCommand(ceoId, shopId, productId);
    }
}
