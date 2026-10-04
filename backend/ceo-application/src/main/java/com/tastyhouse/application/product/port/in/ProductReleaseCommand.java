package com.tastyhouse.application.product.port.in;

import java.util.List;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record ProductReleaseCommand(
    Long ceoId,
    Long shopId,
    List<Long> productIds,
    String target
) {

    public ProductReleaseCommand {
        if (ceoId == null
            || shopId == null
            || productIds == null
            || target == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }
}
