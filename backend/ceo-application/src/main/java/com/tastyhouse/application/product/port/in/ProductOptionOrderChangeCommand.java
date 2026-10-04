package com.tastyhouse.application.product.port.in;

import java.util.List;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record ProductOptionOrderChangeCommand(
    Long ceoId,
    Long shopId,
    Long optionGroupId,
    List<Long> optionIds
) {

    public ProductOptionOrderChangeCommand {
        if (ceoId == null
            || shopId == null
            || optionGroupId == null
            || optionIds == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }
}
