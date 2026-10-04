package com.tastyhouse.application.product.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record ProductOptionGroupUnlinkCommand(
    Long ceoId,
    Long shopId,
    Long productId,
    Long optionGroupId
) {

    public ProductOptionGroupUnlinkCommand {
        if (ceoId == null
            || shopId == null
            || productId == null
            || optionGroupId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }
}
