package com.tastyhouse.application.product.port.in;

import java.util.List;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record ProductOptionHideCommand(
    Long ceoId,
    Long shopId,
    List<ProductOptionTargetCommand> options
) {

    public ProductOptionHideCommand {
        if (ceoId == null
            || shopId == null
            || options == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }
}
