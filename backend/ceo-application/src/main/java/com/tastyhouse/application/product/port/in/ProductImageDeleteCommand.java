package com.tastyhouse.application.product.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record ProductImageDeleteCommand(
    Long ceoId,
    Long shopId,
    Long imageId
) {

    public ProductImageDeleteCommand {
        if (ceoId == null
            || shopId == null
            || imageId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }

    public static ProductImageDeleteCommand of(Long ceoId, Long shopId, Long imageId) {
        return new ProductImageDeleteCommand(ceoId, shopId, imageId);
    }
}
