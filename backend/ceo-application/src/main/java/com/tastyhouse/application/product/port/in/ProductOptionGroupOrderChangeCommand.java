package com.tastyhouse.application.product.port.in;

import java.util.List;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record ProductOptionGroupOrderChangeCommand(
    Long ceoId,
    Long shopId,
    Long productId,
    List<Long> optionGroupIds
) {

    public ProductOptionGroupOrderChangeCommand {
        if (ceoId == null
            || shopId == null
            || productId == null
            || optionGroupIds == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }
}
