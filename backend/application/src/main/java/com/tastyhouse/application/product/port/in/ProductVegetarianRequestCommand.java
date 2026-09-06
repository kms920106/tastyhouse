package com.tastyhouse.application.product.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

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
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
