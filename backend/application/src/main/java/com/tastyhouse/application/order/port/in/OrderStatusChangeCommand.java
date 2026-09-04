package com.tastyhouse.application.order.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 주문 상태 변경 command. 경로 변수 {@code id}는 컨트롤러가 {@code toCommand(id)}로 주입한다.
 */
public record OrderStatusChangeCommand(
    Long orderId,
    String status
) {
    public OrderStatusChangeCommand {
        if (orderId == null || status == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
