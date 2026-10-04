package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record ShopAmenityManagementAssignCommand(
    Long adminId,
    Long shopId,
    Long amenityCategoryId
) {

    public ShopAmenityManagementAssignCommand {
        if (adminId == null || shopId == null || amenityCategoryId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }
}
