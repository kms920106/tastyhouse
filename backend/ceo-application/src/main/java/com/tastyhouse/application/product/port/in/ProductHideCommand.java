package com.tastyhouse.application.product.port.in;

import java.util.List;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record ProductHideCommand(
    Long ceoId,
    Long shopId,
    List<Long> productIds
) {

    public ProductHideCommand {
        if (ceoId == null
            || shopId == null
            || productIds == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }
}
