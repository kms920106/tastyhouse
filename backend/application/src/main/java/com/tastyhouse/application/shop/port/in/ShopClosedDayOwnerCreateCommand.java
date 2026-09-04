package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 가게 정기 휴무 등록 command.
 */
public record ShopClosedDayOwnerCreateCommand(
    Long ceoId,
    Long shopId,
    String closedDayType
) {
    public ShopClosedDayOwnerCreateCommand {
        if (ceoId == null || shopId == null || closedDayType == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
