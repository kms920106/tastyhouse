package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record ShopAmenityOwnerUnassignCommand(
    Long ceoId,
    Long shopId,
    Long amenityCategoryId
) {

    public ShopAmenityOwnerUnassignCommand {
        if (ceoId == null || shopId == null || amenityCategoryId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }

    public static ShopAmenityOwnerUnassignCommand of(Long ceoId, Long shopId, Long amenityCategoryId) {
        return new ShopAmenityOwnerUnassignCommand(ceoId, shopId, amenityCategoryId);
    }
}
