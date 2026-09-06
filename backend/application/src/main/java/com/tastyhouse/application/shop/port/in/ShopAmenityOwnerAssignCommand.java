package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record ShopAmenityOwnerAssignCommand(
    Long ceoId,
    Long shopId,
    Long amenityCategoryId
) {
    public ShopAmenityOwnerAssignCommand {
        if (ceoId == null || shopId == null || amenityCategoryId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
