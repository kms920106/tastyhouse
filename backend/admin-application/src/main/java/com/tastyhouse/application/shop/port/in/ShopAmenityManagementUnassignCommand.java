package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record ShopAmenityManagementUnassignCommand(
    Long adminId,
    Long shopId,
    Long amenityCategoryId
) {

    public ShopAmenityManagementUnassignCommand {
        if (adminId == null || shopId == null || amenityCategoryId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }

    public static ShopAmenityManagementUnassignCommand of(Long adminId, Long shopId, Long amenityCategoryId) {
        return new ShopAmenityManagementUnassignCommand(adminId, shopId, amenityCategoryId);
    }
}
