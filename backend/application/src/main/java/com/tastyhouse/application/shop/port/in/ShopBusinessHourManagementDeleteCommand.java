package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record ShopBusinessHourManagementDeleteCommand(
    Long adminId,
    Long businessHourId
) {
    public ShopBusinessHourManagementDeleteCommand {
        if (adminId == null || businessHourId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static ShopBusinessHourManagementDeleteCommand of(Long adminId, Long businessHourId) {
        return new ShopBusinessHourManagementDeleteCommand(adminId, businessHourId);
    }
}
