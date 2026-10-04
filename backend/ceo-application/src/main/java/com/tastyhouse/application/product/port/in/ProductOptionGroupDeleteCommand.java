package com.tastyhouse.application.product.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record ProductOptionGroupDeleteCommand(
    Long ceoId,
    Long optionGroupId,
    Long shopId
) {

    public ProductOptionGroupDeleteCommand {
        if (ceoId == null
            || optionGroupId == null
            || shopId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }
}
