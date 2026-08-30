package com.tastyhouse.ceoapplication.shop.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 가게 편의시설 지정 command.
 */
public record ShopAmenityAssignCommand(
    Long ceoId,
    Long shopId,
    Long amenityCategoryId
) {
    public ShopAmenityAssignCommand {
        if (ceoId == null || shopId == null || amenityCategoryId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
