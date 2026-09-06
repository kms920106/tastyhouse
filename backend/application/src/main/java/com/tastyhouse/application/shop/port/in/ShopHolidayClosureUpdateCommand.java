package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record ShopHolidayClosureUpdateCommand(
    Long ceoId,
    Long shopId,
    Boolean closedOnPublicHolidays
) {
    public ShopHolidayClosureUpdateCommand {
        if (ceoId == null || shopId == null || closedOnPublicHolidays == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
