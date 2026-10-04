package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record ShopAmenityOwnerAssignCommand(
    Long ceoId,
    Long shopId,
    Long amenityCategoryId
) {

    public ShopAmenityOwnerAssignCommand {
        if (ceoId == null || shopId == null || amenityCategoryId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }
}
