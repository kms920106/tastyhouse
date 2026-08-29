package com.tastyhouse.ceoapi.shop.application.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 공휴일 추가 배달팁 설정 command. 0원은 설정 삭제를 뜻한다.
 */
public record ShopDeliveryTipHolidayUpdateCommand(
    Long ceoId,
    Long shopId,
    Integer tipAmount
) {
    public ShopDeliveryTipHolidayUpdateCommand {
        if (ceoId == null || shopId == null || tipAmount == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
