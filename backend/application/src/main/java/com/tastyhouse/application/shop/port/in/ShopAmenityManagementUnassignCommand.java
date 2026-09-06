package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record ShopAmenityManagementUnassignCommand(
    Long adminId,
    Long shopId,
    Long amenityCategoryId
) {
    public ShopAmenityManagementUnassignCommand {
        if (adminId == null || shopId == null || amenityCategoryId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static ShopAmenityManagementUnassignCommand of(Long adminId, Long shopId, Long amenityCategoryId) {
        return new ShopAmenityManagementUnassignCommand(adminId, shopId, amenityCategoryId);
    }
}
