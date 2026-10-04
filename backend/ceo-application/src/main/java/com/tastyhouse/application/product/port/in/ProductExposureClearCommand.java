package com.tastyhouse.application.product.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record ProductExposureClearCommand(
    Long ceoId,
    Long shopId,
    Long productId
) {

    public ProductExposureClearCommand {
        if (ceoId == null
            || shopId == null
            || productId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }

    public static ProductExposureClearCommand of(Long ceoId, Long shopId, Long productId) {
        return new ProductExposureClearCommand(ceoId, shopId, productId);
    }
}
