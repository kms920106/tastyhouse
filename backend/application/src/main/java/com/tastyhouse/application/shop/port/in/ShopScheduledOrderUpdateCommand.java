package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 가게 예약주문 운영 여부 변경 command.
 */
public record ShopScheduledOrderUpdateCommand(
    Long ceoId,
    Long shopId,
    Boolean enabled
) {
    public ShopScheduledOrderUpdateCommand {
        if (ceoId == null || shopId == null || enabled == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
