package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 가게 공휴일 휴무 설정 변경 command.
 */
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
