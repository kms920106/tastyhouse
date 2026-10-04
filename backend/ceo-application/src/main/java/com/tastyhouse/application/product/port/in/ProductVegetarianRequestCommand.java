package com.tastyhouse.application.product.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record ProductVegetarianRequestCommand(
    Long ceoId,
    Long shopId,
    Long productId,
    String vegetarianType,
    String ingredients,
    String description
) {

    public ProductVegetarianRequestCommand {
        if (ceoId == null
            || shopId == null
            || productId == null
            || vegetarianType == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }
}
