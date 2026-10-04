package com.tastyhouse.application.product.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record ProductRepresentativeClearCommand(
    Long ceoId,
    Long shopId,
    Long productId
) {

    public ProductRepresentativeClearCommand {
        if (ceoId == null
            || shopId == null
            || productId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }

    public static ProductRepresentativeClearCommand of(Long ceoId, Long shopId, Long productId) {
        return new ProductRepresentativeClearCommand(ceoId, shopId, productId);
    }
}
