package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record ShopAmenityOwnerUnassignCommand(
    Long ceoId,
    Long shopId,
    Long amenityCategoryId
) {
    public ShopAmenityOwnerUnassignCommand {
        if (ceoId == null || shopId == null || amenityCategoryId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static ShopAmenityOwnerUnassignCommand of(Long ceoId, Long shopId, Long amenityCategoryId) {
        return new ShopAmenityOwnerUnassignCommand(ceoId, shopId, amenityCategoryId);
    }
}
