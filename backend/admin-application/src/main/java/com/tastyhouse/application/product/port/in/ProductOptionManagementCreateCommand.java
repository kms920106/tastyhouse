package com.tastyhouse.application.product.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record ProductOptionManagementCreateCommand(
    Long optionGroupId,
    String name,
    Integer additionalPrice,
    Integer sort,
    Boolean soldOut,
    Boolean visible,
    Integer cupCount,
    Integer personalCupDiscountAmount
) {

    public ProductOptionManagementCreateCommand {
        if (optionGroupId == null || name == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }
}
