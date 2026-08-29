package com.tastyhouse.webapi.order.application.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 주문 상품의 선택 옵션 command.
 *
 * <p>과거 서비스가 {@code OrderProductOptionRequest}(HTTP 요청 record)를 그대로 받던 자리를 대체한다.
 */
public record OrderLineOptionCommand(
    Long groupId,
    Long optionId
) {
    public OrderLineOptionCommand {
        if (groupId == null || optionId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static OrderLineOptionCommand of(Long groupId, Long optionId) {
        return new OrderLineOptionCommand(groupId, optionId);
    }
}
