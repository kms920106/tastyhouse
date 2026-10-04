package com.tastyhouse.application.product.port.in;

import java.util.List;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record ProductOptionReleaseCommand(
    Long ceoId,
    Long shopId,
    List<ProductOptionTargetCommand> options,
    String target
) {

    public ProductOptionReleaseCommand {
        if (ceoId == null
            || shopId == null
            || options == null
            || target == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }
}
