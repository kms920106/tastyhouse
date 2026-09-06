package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record ShopAmenityManagementAssignCommand(
    Long adminId,
    Long shopId,
    Long amenityCategoryId
) {
    public ShopAmenityManagementAssignCommand {
        if (adminId == null || shopId == null || amenityCategoryId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
