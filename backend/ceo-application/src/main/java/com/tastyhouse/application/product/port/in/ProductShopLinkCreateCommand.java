package com.tastyhouse.application.product.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record ProductShopLinkCreateCommand(
    Long ceoId,
    Long productId,
    Long targetShopId,
    Long productCategoryId
) {

    public ProductShopLinkCreateCommand {
        if (ceoId == null
            || productId == null
            || targetShopId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }
}
