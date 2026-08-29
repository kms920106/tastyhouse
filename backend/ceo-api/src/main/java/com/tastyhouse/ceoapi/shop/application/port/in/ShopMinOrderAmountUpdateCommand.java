package com.tastyhouse.ceoapi.shop.application.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 가게 최소주문금액 변경 command. 0원은 금액이 아니라 미설정을 뜻하는 도메인 규약이다.
 */
public record ShopMinOrderAmountUpdateCommand(
    Long ceoId,
    Long shopId,
    Integer minOrderAmount
) {
    public ShopMinOrderAmountUpdateCommand {
        if (ceoId == null || shopId == null || minOrderAmount == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
