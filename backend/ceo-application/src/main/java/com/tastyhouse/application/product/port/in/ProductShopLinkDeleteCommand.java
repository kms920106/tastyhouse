package com.tastyhouse.application.product.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record ProductShopLinkDeleteCommand(
    Long ceoId,
    Long productId,
    Long targetShopId
) {

    public ProductShopLinkDeleteCommand {
        if (ceoId == null
            || productId == null
            || targetShopId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }

    public static ProductShopLinkDeleteCommand of(Long ceoId, Long productId, Long targetShopId) {
        return new ProductShopLinkDeleteCommand(ceoId, productId, targetShopId);
    }
}
