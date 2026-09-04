package com.tastyhouse.adminapplication.shop.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/** 가게 편의시설 지정 command. */
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
