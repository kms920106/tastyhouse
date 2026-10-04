package com.tastyhouse.application.product.port.in;

import java.util.List;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record ProductImageReorderCommand(
    Long ceoId,
    Long shopId,
    Long productId,
    List<Long> imageIds
) {

    public ProductImageReorderCommand {
        if (ceoId == null
            || shopId == null
            || productId == null
            || imageIds == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }
}
