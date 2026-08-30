package com.tastyhouse.adminapplication.shop.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/** 정기 휴무일 등록 command. */
public record ShopClosedDayCreateCommand(
    Long adminId,
    Long shopId,
    String closedDayType
) {
    public ShopClosedDayCreateCommand {
        if (adminId == null || shopId == null || closedDayType == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
