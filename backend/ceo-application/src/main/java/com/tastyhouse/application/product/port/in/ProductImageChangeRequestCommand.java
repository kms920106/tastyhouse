package com.tastyhouse.application.product.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record ProductImageChangeRequestCommand(
    Long ceoId,
    Long shopId,
    Long productId
) {

    public ProductImageChangeRequestCommand {
        if (ceoId == null
            || shopId == null
            || productId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }

    public static ProductImageChangeRequestCommand of(Long ceoId, Long shopId, Long productId) {
        return new ProductImageChangeRequestCommand(ceoId, shopId, productId);
    }
}
