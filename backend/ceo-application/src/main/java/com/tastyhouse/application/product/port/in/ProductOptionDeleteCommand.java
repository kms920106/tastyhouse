package com.tastyhouse.application.product.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record ProductOptionDeleteCommand(
    Long ceoId,
    Long optionId,
    Long shopId
) {

    public ProductOptionDeleteCommand {
        if (ceoId == null
            || optionId == null
            || shopId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }
}
